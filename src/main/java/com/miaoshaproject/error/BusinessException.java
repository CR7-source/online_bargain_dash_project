package com.miaoshaproject.error;

/**
 * @Author:CR7-source
 * @Date: 2022/01/28/ 16:17
 * @Description
 */
//包装器业务异常实现
public class BusinessException extends Exception implements CommonError {
    private CommonError commonError;
    public BusinessException(CommonError commonError){
        super();
        this.commonError=commonError;
    }
    public BusinessException(CommonError commonError,String errorMsg){
        super();
        this.commonError=commonError;
        this.commonError.setErrMsg(errorMsg);
    }
    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();

    }

    @Override
    public CommonError setErrMsg(String errMs) {
        this.commonError.setErrMsg(errMs);
        return this;
    }
}
