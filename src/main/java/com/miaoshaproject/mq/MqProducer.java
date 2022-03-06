package com.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import com.miaoshaproject.dao.StockLogDOMapper;
import com.miaoshaproject.dataobject.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:CR7-source
 * @Date: 2022/03/03/ 23:04
 * @Description
 */
@Component
public class MqProducer {
    private TransactionMQProducer transactionMQProducer;
    private DefaultMQProducer producer;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StockLogDOMapper stockLogDOMapper;
    @Value("${mq.nameserver.addr}")
    private String nameAddr;
    @Value("${mq.topicname}")
    private String topicName;
    @PostConstruct
    public void init() throws MQClientException {
        //mq producer初始化
        producer=new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        //producer.start();
        transactionMQProducer=new TransactionMQProducer("producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object args) {
                //真正要做的事 创建订单
                Integer itemId=(Integer)((Map)args).get("itemId");
                Integer promoId=(Integer)((Map)args).get("promoId");
                Integer userId=(Integer)((Map)args).get("userId");
                Integer amount=(Integer)((Map)args).get("amount");
                String stockLogId=(String)((Map)args).get("stockLogId");
                try {

                    OrderModel orderModel = orderService.createOrder(stockLogId,userId, itemId, promoId,amount);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                //根据是否扣减库存成功，来判断要返回 commit，rollback还是unknown
                 String jsonString = new String(messageExt.getBody());
                Map<String,Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String) map.get("stockLogId");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if(stockLogDO==null|| stockLogDO.getStatus()==1){
                    return LocalTransactionState.UNKNOW;
                }
                if(stockLogDO.getStatus()==2){
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
        transactionMQProducer.start();

    }
    //事务型同步库存扣减
    public boolean transactionAsyncReduceStock(String stockLogId,Integer userId,Integer promoId,Integer itemId,Integer amount){
        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        bodyMap.put("stockLogId",stockLogId);

        Map<String,Object> argsMap=new HashMap<>();
        argsMap.put("itemId",itemId);
        argsMap.put("amount",amount);
        argsMap.put("userId",userId);
        argsMap.put("promoId",promoId);
        argsMap.put("stockLogId",stockLogId);
        Message message = new Message(topicName,  "increase", JSON.toJSON(argsMap).toString().getBytes(StandardCharsets.UTF_8));
        TransactionSendResult sendResult=null;
        try {
             sendResult = transactionMQProducer.sendMessageInTransaction(message,argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if(sendResult.getLocalTransactionState()==LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }
        if(sendResult.getLocalTransactionState()==LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }
        return false;
    }
    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId,Integer amount)  {
        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        Message message = new Message(topicName,  "increase", JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

        try {
            SendResult result = producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;

        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
