package com.sky.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || userLoginDTO.getCode() == null || userLoginDTO.getCode().trim().isEmpty()) {
            log.error("微信登录失败，code为空，userLoginDTO：{}", userLoginDTO);
            throw new RuntimeException(MessageConstant.LOGIN_FAILED);
        }
        // 1. 调用微信接口服务，获取微信用户的openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 2. 判断openid是否为空，如果为空，则登录失败，抛出业务异常
        if (openid == null) {
            log.error("微信登录失败，无法获取openid，userLoginDTO：{}", userLoginDTO);
            throw new RuntimeException(MessageConstant.LOGIN_FAILED);
        }

        // 3. 如果openid不为空，则根据openid查询用户信息，判断当前用户是否是新用户
        User user = userMapper.getByOpenid(openid);

        // 4. 如果是新用户，则自动注册用户信息，并保存到数据库中
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        // 5. 返回用户信息
        return user;
    }

    /**
     * 更新用户信息（头像、昵称）
     * @param user
     */
    @Override
    public void updateUserInfo(User user) {
        user.setId(BaseContext.getCurrentId());
        userMapper.update(user);
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL,map);

        if (json == null || json.trim().isEmpty()) {
            log.error("微信登录失败，接口无响应，code：{}", code);
            return null;
        }

        // WeChat may return errcode/errmsg when code is invalid or appid/secret is wrong.
        JSONObject jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            log.error("微信登录失败，返回数据无法解析，code：{}，raw：{}", code, json);
            return null;
        }
        Integer errcode = jsonObject.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            log.error("微信登录失败，errcode：{}，errmsg：{}，raw：{}", errcode, jsonObject.getString("errmsg"), json);
            return null;
        }
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
