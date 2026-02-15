package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")  //指定bean的名称，避免和admin模块的ShopController冲突
@RequestMapping("/user/shop")
@Api(tags = "商户相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STAUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询店铺的营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺的营业状态")
    public Result getStatus() {
        log.info("查询店铺的营业状态");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        if (status == null) {
            status = 1; // 默认营业中
        }
        log.info("店铺的营业状态：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
