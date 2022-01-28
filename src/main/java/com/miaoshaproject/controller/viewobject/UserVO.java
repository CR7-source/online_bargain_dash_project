package com.miaoshaproject.controller.viewobject;

import lombok.Data;

/**
 * @Author:CR7-source
 * @Date: 2022/01/28/ 15:22
 * @Description
 */
//前端用户需要的展示的对象
@Data
public class UserVO {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;

}
