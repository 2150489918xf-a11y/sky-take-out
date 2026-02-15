package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;


@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户信息
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户信息
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 更新用户信息
     * @param user
     */
    @Update("update user set name = #{name}, avatar = #{avatar} where id = #{id}")
    void update(User user);

    /**
     * 根据动态条件统计用户数量
     * @param map
     */
    Integer countByMap(Map map);
}
