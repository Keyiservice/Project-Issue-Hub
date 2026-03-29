package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    @TableId
    private Long id;
    private String userNo;
    private String username;
    private String passwordHash;
    private String realName;
    private String mobile;
    private String email;
    private String wechatOpenid;
    private String wechatUnionid;
    private LocalDateTime wechatBoundAt;
    private Integer passwordChangeRequired;
    private LocalDateTime passwordChangedAt;
    private String departmentCode;
    private String departmentName;
    private String status;
    private LocalDateTime lastLoginAt;
}
