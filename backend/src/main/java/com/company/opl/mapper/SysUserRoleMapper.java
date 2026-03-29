package com.company.opl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.opl.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int hardDeleteByUserId(@Param("userId") Long userId);
}
