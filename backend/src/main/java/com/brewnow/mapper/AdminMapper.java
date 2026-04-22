package com.brewnow.mapper;

import com.brewnow.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理员数据访问接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据ID查询管理员
     */
    @Select("SELECT * FROM admins WHERE admin_id = #{adminId}")
    @Results({
            @Result(column = "admin_id", property = "adminId"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "mobile_phone", property = "mobilePhone"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_login_time", property = "lastLoginTime")
    })
    Admin selectById(Integer adminId);

    /**
     * 根据用户名查询管理员
     */
    @Select("SELECT * FROM admins WHERE username = #{username}")
    @Results({
            @Result(column = "admin_id", property = "adminId"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "mobile_phone", property = "mobilePhone"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_login_time", property = "lastLoginTime")
    })
    Admin selectByUsername(String username);

    /**
     * 查询所有管理员
     */
    @Select("SELECT * FROM admins WHERE status = 'ACTIVE' ORDER BY create_time")
    @Results({
            @Result(column = "admin_id", property = "adminId"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "mobile_phone", property = "mobilePhone"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_login_time", property = "lastLoginTime")
    })
    List<Admin> selectAll();

    /**
     * 更新管理员信息
     */
    @Update("UPDATE admins SET real_name = #{realName}, mobile_phone = #{mobilePhone}, " +
            "email = #{email} WHERE admin_id = #{adminId}")
    int updateById(Admin admin);

    /**
     * 更新管理员密码
     */
    @Update("UPDATE admins SET password = #{password} WHERE admin_id = #{adminId}")
    int updatePassword(@Param("adminId") Integer adminId, @Param("password") String password);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE admins SET last_login_time = NOW() WHERE admin_id = #{adminId}")
    int updateLastLoginTime(Integer adminId);
}