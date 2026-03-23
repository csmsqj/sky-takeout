package com.sky.mapper;


import com.sky.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User selectByOpenid(String openid);

 //插入数据，并且要获取主键值。因为前端返回需要用到，所以必须要使用动态XML。
    void insert(User user);

}
