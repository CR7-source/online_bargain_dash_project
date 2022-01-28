package com.miaoshaproject.error;

/**
 * @Author {CR7-source}
 * @Date: 2022/01/28/ 15:46
 * @Description
 */
public interface CommonError {
    public int getErrCode();

    public String getErrMsg();

    public CommonError setErrMsg(String errMs);
}
