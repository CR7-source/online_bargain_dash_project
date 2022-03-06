package com.miaoshaproject.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.common.util.concurrent.RateLimiter;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.util.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.awt.font.ImageGraphicAttribute;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Author:CR7-source
 * @Date: 2022/02/07/ 14:08
 * @Description
 */
@Slf4j
@Controller
@RequestMapping("/order")
//跨域请求中，不能做到session共享
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;
    private ExecutorService executorService;
    private RateLimiter orderCreateRateLimiter;
    @PostConstruct
    public void init(){
        executorService= Executors.newFixedThreadPool(20);
        orderCreateRateLimiter=RateLimiter.create(300);
    }
    @RequestMapping(value = "/generateverifycode", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public void generateverifycode(HttpServletResponse response
                                               ) throws BusinessException, IOException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能生成验证码");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        Map<String,Object> map= CodeUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("verify_code_"+userModel.getId(),map.get("code"),10,TimeUnit.MINUTES);
        ImageIO.write((RenderedImage)map.get("codePic"),"jpeg",response.getOutputStream());
        System.out.println("验证码的值为： "+map.get("code"));
    }
    //产生秒杀令牌
    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatetoken(@RequestParam(name = "itemId") Integer itemId,
                                          @RequestParam(name = "promoId") Integer promoId,
                                          @RequestParam(name = "verifyCode")String verifyCode
    ) throws BusinessException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        //获取用户的登陆信息
        String redisVerifyCode =(String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法");
        }
        if(!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法,验证码错误");
        }
        String promoToken = promoService.generateSecondKillToken(userModel.getId(), itemId, promoId);
        if(promoToken==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        return CommonReturnType.create(promoToken);

    }

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken
    ) throws BusinessException {
       if(!orderCreateRateLimiter.tryAcquire()){
           throw new BusinessException(EmBusinessError.RATE_LIMIT);
       }
        //使用token来判断用户是否登陆
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        //
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");

        }
        //以秒杀入口进来的商品：验证秒杀令牌是否存在
        if(promoId!=null){
            String inRedisPromoToken = (String)redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken==null){
                throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀校验令牌失败");
            }
            if(!StringUtils.equals(promoToken,inRedisPromoToken)){
                throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀校验令牌失败");
            }
        }

        //同步掉采用线程池的submit
        //拥塞窗口为20的等待队列
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = itemService.initStockLog(itemId, amount);
                //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId,promoId, amount);
                //
                if (!mqProducer.transactionAsyncReduceStock(stockLogId, userModel.getId(), promoId, itemId, amount)) {
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
                }
                return null;
            }
        });
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败");
        }
        return CommonReturnType.create(null);
    }

}
