package com.miaoshaproject.controller;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.CacheService;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author:CR7-source
 * @Date: 2022/02/05/ 16:16
 * @Description
 */
@Controller
@RequestMapping("/item")
//跨域请求中，不能做到session共享
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PromoService promoService;
    //创建商品的controller
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);

    }

    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if(itemModel.getPromoModel()!=null){
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().
                    toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));

        }
        return itemVO;
    }
    @RequestMapping(value = "/publishpromo", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType publishpromo(@RequestParam(name="id")Integer id){
        promoService.publishPromo(id);
        return CommonReturnType.create(null);
    }
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        //先取本地service
        ItemModel itemModel =(ItemModel)cacheService.getCommonCache("item_" + id);
        if(itemModel==null){
            //根据商品的id到redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            if(itemModel==null){
                itemModel = itemService.getItemById(id);
                //设置itemModel到redis内
                redisTemplate.opsForValue().set("item_" + id,itemModel);
                redisTemplate.expire("item_" + id,10, TimeUnit.MINUTES);
            }
            cacheService.setCommonCache("item_" + id,itemModel);

        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }
    //商品列表页面浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }


}

