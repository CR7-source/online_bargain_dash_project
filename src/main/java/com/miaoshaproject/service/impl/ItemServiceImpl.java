package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dao.StockLogDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.dataobject.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author:CR7-source
 * @Date: 2022/02/05/ 15:18
 * @Description
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private PromoService promoService;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private StockLogDOMapper stockLogDOMapper;
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {

        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //转化itemmodel->dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());

    }

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());

        return itemStockDO;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();

        //使用Java8的stream API
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemByIDInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validte_" + id);
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validte_" + id, itemModel);
            redisTemplate.expire("item_validte_" + id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将dataobject-> Model
        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);
        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        if(!mqResult){
            redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() );
        }
        return mqResult;
    }
/*更新redis的库存*/
    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
       // int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);

        if (result > 0) {
            //事务快结束时（订单完成）再减库存
                /*boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
                if(!mqResult){
                    redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() );
                }*/
                return true;
        }
        else if(result==0){
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");
            return true;
        }
        else {
            increaseStock(itemId,amount);
            return false;
        }
    }
    /*更新redis的库存*/
    @Override
    public boolean increaseStock(Integer itemId, Integer amount) throws BusinessException {
        // int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() );

        return true;
    }

    @Override
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }
/*初始化库存流水状态*/
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);
        stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
