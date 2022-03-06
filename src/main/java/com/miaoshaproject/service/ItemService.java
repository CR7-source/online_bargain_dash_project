package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * @Author:CR7-source
 * @Date: 2022/02/05/ 15:14
 * @Description
 */
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();
    //item及promo mode缓存模型
    ItemModel getItemByIDInCache(Integer id);
    //

    //商品详情浏览
    ItemModel getItemById(Integer id);
    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId,Integer amount);
    boolean decreaseStock(Integer itemId,Integer amount)throws BusinessException;
    boolean increaseStock(Integer itemId,Integer amount)throws BusinessException;

    void increaseSales(Integer itemId,Integer amount)throws BusinessException;
    String initStockLog(Integer itemId, Integer amount);
}


