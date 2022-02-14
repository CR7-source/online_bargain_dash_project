package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @Author:CR7-source
 * @Date: 2022/02/08/ 13:57
 * @Description
 */
@ControllerAdvice
public class GrobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Exception ex){
        ex.printStackTrace();
        HashMap<String, Object> respondData = new HashMap<>();
        if(ex instanceof BusinessException){
            BusinessException businessException = (BusinessException) ex;
            respondData.put("errCode",businessException.getErrCode());
            respondData.put("errMsg",businessException.getErrMsg());
            return CommonReturnType.create(respondData,"fail");
        }
        else if (ex instanceof ServletRequestBindingException){
            respondData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            respondData.put("errMsg","url绑定问题");
        }
        else if(ex instanceof NoHandlerFoundException){
            respondData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            respondData.put("errMsg","没有找到相应的访问路径");
        }
        else{
            respondData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            respondData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(respondData,"fail");

    }
}
