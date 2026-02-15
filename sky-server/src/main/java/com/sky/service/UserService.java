package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 更新用户信息（头像、昵称）
     * @param user
     */
    void updateUserInfo(User user);
}
