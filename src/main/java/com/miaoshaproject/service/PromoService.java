package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * @Author:CR7-source
 * @Date: 2022/02/07/ 15:42
 * @Description
 */
public interface PromoService {
    //根据itemID获取即将进行或正在进行的itemId
    PromoModel getPromoByItemId(Integer itemId);

    public void publishPromo(Integer promoId);
/*生成秒杀用到令牌*/
    public String generateSecondKillToken(Integer userId,Integer itemId,Integer promoId);

}
