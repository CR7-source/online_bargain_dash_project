package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService  {
//    根据用户id获取用户对象
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
}
