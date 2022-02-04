package com.miaoshaproject.validator;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:CR7-source
 * @Date: 2022/02/04/ 16:52
 * @Description
 */
@Data
public class ValidationResult {
    private boolean hasErrors=false;
    //存放错误信息的map
    private Map<String,String> errMsgMap=new HashMap<>();
    //实现统一的通过格式化获取错误信息
    public String getErrMsg(){
        return StringUtils.join(errMsgMap.values().toArray(),',');
    }


}
