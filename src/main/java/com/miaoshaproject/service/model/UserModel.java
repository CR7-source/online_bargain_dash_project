package com.miaoshaproject.service.model;

import lombok.Data;

/**
 * @Author:CR7-source
 * @Date: 2022/01/28/ 14:34
 * @Description
 */
@Data
public class UserModel {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
    private String regisitMode;
    private Integer thirdPartyId;
    private String encrptPassword;


}
