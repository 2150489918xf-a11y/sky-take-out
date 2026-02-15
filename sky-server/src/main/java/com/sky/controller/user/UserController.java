package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController{

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * C端用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录，userLoginDTO：{}", userLoginDTO.getCode());

        //微信登录
        User user = userService.wxLogin(userLoginDTO);

        //为微信用户登录jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(), claims);

        //返回登录结果
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 更新用户信息（头像、昵称）
     * @param user
     * @return
     */
    @PutMapping("/update")
    @ApiOperation("更新用户信息")
    public Result update(@RequestBody User user) {
        log.info("更新用户信息：{}", user);
        userService.updateUserInfo(user);
        return Result.success();
    }
}
//微信登录流程
//1. 微信用户在小程序端点击登录按钮，触发登录事件
//2. 小程序端调用微信登录接口，获取code
//3. 小程序端将code发送给后端接口/user/user/login
//4. 后端接口/user/user/login接收code，调用微信接口服务，获取openid
//5. 后端接口/user/user/login根据openid查询用户信息，判断当前用户是否是新用户
//6. 如果是新用户，则自动注册用户信息，并保存到数据库中
//7. 后端接口/user/user/login为微信用户登录jwt令牌，并将令牌返回给小程序端
//8. 小程序端接收登录结果，保存jwt令牌，并将令牌添加到后续请求的请求头中，完成登录流程