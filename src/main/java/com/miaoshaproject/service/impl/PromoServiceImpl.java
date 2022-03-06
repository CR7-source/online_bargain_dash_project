package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author:CR7-source
 * @Date: 2022/02/07/ 15:44
 * @Description
 */
@Service("PromoService")
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;


    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoDO==null) {
            return null;
        }
        //判断当前时间是否是秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);//未开始
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);//已结束
        } else {
            promoModel.setStatus(2);//正在进行
        }
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId()==null||promoDO.getItemId().intValue()==0){
            return ;
        }
        ItemModel itemModel=itemService.getItemById(promoDO.getItemId());
        //将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());
        //将大闸的限制数字设到redis内
        redisTemplate.opsForValue().set("promo_door_count_"+promoId,itemModel.getStock().intValue()*5);

    }

    @Override
    public String generateSecondKillToken(Integer userId, Integer itemId, Integer promoId) {
        //判断是否商品已经售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            return null;
        }
        //1.校验下单状态，下单商品是否存在，用户是否合法，购买数量是否正确
        //在获取itemModel同时也会获取相应未开始或者正在进行中的秒杀活动
        ItemModel itemModel = itemService.getItemByIDInCache(itemId);
        if(itemModel==null){
            return null;
        }
        UserModel userModel = userService.getUserByIDInCache(userId);
        if (userModel == null) {
            return null;
        }
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoDO==null) {
            return null;
        }
        //判断当前时间是否是秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);//未开始
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);//已结束
        } else {
            promoModel.setStatus(2);//正在进行
        }
        if(promoModel.getStatus()!=2){
            return null;
        }
        //获取秒杀大闸的count数量
        Long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if(result<0){
            return null;
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,token,5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
