package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.MD5Encoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
@Slf4j
@Controller
@RequestMapping("user")
public class UserController extends BaseController{
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;//prototype,可以多线程处理
    @ResponseBody
    @RequestMapping(value="/register",method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    //用户注册接口
    public CommonReturnType register(@RequestParam("telphone") String telphone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name") String name,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("age") Integer age,
                                     @RequestParam("password") String password
                                     ) throws BusinessException, NoSuchAlgorithmException {
        //验证手机号和otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(inSessionOtpCode,otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender.byteValue());
        userModel.setTelphone(telphone);
        userModel.setEncrptPassword(EncodeByMD5(password));
        userModel.setRegisitMode("byphone");
        userService.register(userModel);
        return CommonReturnType.create("注册成功");

    }
    public String EncodeByMD5(String str) throws NoSuchAlgorithmException {
        //确定计算方法
        //MessageDigest是消息摘要算法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String newstr = base64Encoder.encode(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return newstr;
    }
    //获取otp短信接口
    @RequestMapping(value="/getotp",method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone") String telphone){
        //按照一定的规则生成otp验证码
        Random random = new Random();
        int randomNum = random.nextInt(99999);
        randomNum+=10000;
        String otpCode = String.valueOf(randomNum);
        //将otp验证码对应用户手机号关联,使用httpsession
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        System.out.println("telephone");
        log.info("telephone{},otpCode{}",telphone,otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //调用service层服务获取对应id的对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //若获取的用户信息不存在
        if(userModel==null) throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        UserVO userVO = convertFromUserModel(userModel);
        return CommonReturnType.create(userVO);
    }
    private UserVO convertFromUserModel(UserModel userModel){
        if(userModel==null)return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
