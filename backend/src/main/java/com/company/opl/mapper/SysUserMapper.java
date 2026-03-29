package com.company.opl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.opl.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("""
            SELECT id, user_no, username, password_hash, real_name, mobile, email,
                   wechat_openid, wechat_unionid, wechat_bound_at,
                   password_change_required, password_changed_at,
                   department_code, department_name, status, last_login_at,
                   created_by, updated_by, created_at, updated_at, deleted
            FROM sys_user
            WHERE username = #{username}
            LIMIT 1
            """)
    SysUser selectAnyByUsername(@Param("username") String username);

    @Select("""
            SELECT id, user_no, username, password_hash, real_name, mobile, email,
                   wechat_openid, wechat_unionid, wechat_bound_at,
                   password_change_required, password_changed_at,
                   department_code, department_name, status, last_login_at,
                   created_by, updated_by, created_at, updated_at, deleted
            FROM sys_user
            WHERE user_no = #{userNo}
            LIMIT 1
            """)
    SysUser selectAnyByUserNo(@Param("userNo") String userNo);

    @Update("""
            UPDATE sys_user
            SET user_no = #{user.userNo},
                username = #{user.username},
                password_hash = #{user.passwordHash},
                real_name = #{user.realName},
                department_code = #{user.departmentCode},
                department_name = #{user.departmentName},
                status = #{user.status},
                password_change_required = #{user.passwordChangeRequired},
                deleted = 0
            WHERE id = #{user.id}
            """)
    int restoreSeedUser(@Param("user") SysUser user);
}
