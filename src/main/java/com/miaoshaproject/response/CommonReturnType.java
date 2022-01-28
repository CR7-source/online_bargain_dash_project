package com.miaoshaproject.response;

import lombok.Data;

/**
 * @Author:CR7-source
 * @Date: 2022/01/28/ 15:37
 * @Description
 */
@Data
public class CommonReturnType {
    //表明对应请求的返回处理结果“success”或“fail”
    private String status;

    //若status=success，则data内返回前端需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;
    public static CommonReturnType create(Object result){
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus("success");
        return type;
    }
    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus(status);
        return type;
    }

}
