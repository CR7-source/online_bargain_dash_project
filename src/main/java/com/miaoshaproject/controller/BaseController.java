package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;

/**
 * @Author:CR7-source
 * @Date: 2022/01/28/ 16:50
 * @Description
 */
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//允许跨域请求
public class BaseController {
    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody

    public Object handleException(Exception ex){
        HashMap<String, Object> respondData = new HashMap<>();
        if(ex instanceof BusinessException){
            BusinessException businessException = (BusinessException) ex;
            respondData.put("errCode",businessException.getErrCode());
            respondData.put("errMsg",businessException.getErrMsg());
            return CommonReturnType.create(respondData,"fail");
        }
        else{
            respondData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            respondData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(respondData,"fail");

    }
}
