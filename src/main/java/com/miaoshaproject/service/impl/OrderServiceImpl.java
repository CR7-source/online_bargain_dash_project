package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author:CR7-source
 * @Date: 2022/02/07/ 12:38
 * @Description
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //1.校验下单状态，下单商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }

        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }

        //2.落单减库存(支付减库存会导致超卖)
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));
        //生成订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        itemService.increaseSales(itemId,amount);

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)//防止调用回滚导致订单号重复
    public String generateOrderNo(){
        //订单号16
        StringBuilder stringBuilder = new StringBuilder();
        //前8位数时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        //中间6位数自增序列
        int sequence=0;
        SequenceDO sequenceDo = sequenceDOMapper.getSequenceByName("order_info");
        sequence=sequenceDo.getCurrentValue()+sequenceDo.getStep();
        sequenceDo.setCurrentValue(sequenceDo.getCurrentValue()+sequenceDo.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDo);
        String sequeceStr=String.valueOf(sequence);
        //可能订单号超过6位
        for(int i=0;i<6-sequeceStr.length();i++){
            stringBuilder.append("0");
        }
        stringBuilder.append(sequeceStr);
        //最后2位位分库分表位
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null) return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        return orderDO;
    }
}
