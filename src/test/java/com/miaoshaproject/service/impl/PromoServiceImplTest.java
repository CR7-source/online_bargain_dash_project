package com.miaoshaproject.service.impl;

import com.miaoshaproject.App;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class PromoServiceImplTest{
    @Autowired
    PromoService promoService;
    @Test
    public void test(){
        System.out.println("hello");
        PromoModel promoByItemId = promoService.getPromoByItemId(3);
        System.out.println("hello2");

        if(promoByItemId!=null)
            System.out.println(promoByItemId.toString());
    }

}