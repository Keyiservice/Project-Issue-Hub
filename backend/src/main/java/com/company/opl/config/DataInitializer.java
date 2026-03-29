package com.company.opl.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.entity.SysRole;
import com.company.opl.entity.SysUser;
import com.company.opl.entity.SysUserRole;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.mapper.SysRoleMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.mapper.SysUserRoleMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initDefaultData(SysRoleMapper sysRoleMapper,
                                             SysUserMapper sysUserMapper,
                                             SysUserRoleMapper sysUserRoleMapper,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            ensureRoles(sysRoleMapper);
            ensureUser(sysUserMapper, sysUserRoleMapper, passwordEncoder,
                    "U0001", "admin", "System Admin", "IT", "IT", 5L);
            ensureUser(sysUserMapper, sysUserRoleMapper, passwordEncoder,
                    "U0002", "pm01", "Project Manager", "PM", "Project Management", 3L);
        };
    }

    private void ensureRoles(SysRoleMapper sysRoleMapper) {
        List<RoleSeed> roles = List.of(
                new RoleSeed(1L, "SITE_USER", "现场人员", "现场录入与跟进", 10),
                new RoleSeed(2L, "RESP_ENGINEER", "责任工程师", "问题处理与反馈", 20),
                new RoleSeed(3L, "PROJECT_MANAGER", "项目经理", "项目问题协调", 30),
                new RoleSeed(4L, "MANAGEMENT", "管理层", "统计与查看", 40),
                new RoleSeed(5L, "ADMIN", "管理员", "系统管理", 50),
                new RoleSeed(6L, "MFG_MANAGER", "制造经理", "业务岗位角色", 110),
                new RoleSeed(7L, "PLANT_MANAGER", "厂长", "业务岗位角色", 120),
                new RoleSeed(8L, "MECH_DESIGN_SUPERVISOR", "机械设计主管", "业务岗位角色", 130),
                new RoleSeed(9L, "DESIGN_MANAGER", "设计经理", "业务岗位角色", 140),
                new RoleSeed(10L, "AUTOMATION_DESIGN_SUPERVISOR", "自动化设计主管", "业务岗位角色", 150),
                new RoleSeed(11L, "AUTOMATION_ENGINEER", "自动化工程师", "业务岗位角色", 160),
                new RoleSeed(12L, "MECHANICAL_ENGINEER", "机械工程师", "业务岗位角色", 170),
                new RoleSeed(13L, "ELECTRICAL_TECHNICIAN", "电气技术员", "业务岗位角色", 180),
                new RoleSeed(14L, "MECHANICAL_TECHNICIAN", "机械技术员", "业务岗位角色", 190),
                new RoleSeed(15L, "ME", "ME", "业务岗位角色", 200),
                new RoleSeed(16L, "MDA", "MDA", "业务岗位角色", 210),
                new RoleSeed(17L, "AE", "AE", "业务岗位角色", 220),
                new RoleSeed(18L, "MD_MANAGER", "MD经理", "业务岗位角色", 230),
                new RoleSeed(19L, "IE", "IE", "业务岗位角色", 240),
                new RoleSeed(20L, "LOGISTICS_SUPERVISOR", "物流主管", "业务岗位角色", 250),
                new RoleSeed(21L, "PROCUREMENT_MANAGER", "采购经理", "业务岗位角色", 260),
                new RoleSeed(22L, "FINANCE_MANAGER", "财务经理", "业务岗位角色", 270),
                new RoleSeed(23L, "HR_MANAGER", "人事经理", "业务岗位角色", 280),
                new RoleSeed(24L, "QUALITY_MANAGER", "质量经理", "业务岗位角色", 290),
                new RoleSeed(25L, "QUALITY_ENGINEER", "质量工程师", "业务岗位角色", 300),
                new RoleSeed(26L, "PLANT_REPRESENTATIVE", "工厂代表", "业务岗位角色", 310)
        );

        for (RoleSeed seed : roles) {
            SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, seed.roleCode())
                    .last("limit 1"));
            if (role == null) {
                role = new SysRole();
                role.setId(seed.id());
                role.setRoleCode(seed.roleCode());
                role.setRoleName(seed.roleName());
                role.setDescription(seed.description());
                role.setStatus(UserStatusEnum.ENABLED.name());
                role.setSortNo(seed.sortNo());
                sysRoleMapper.insert(role);
                continue;
            }

            role.setRoleName(seed.roleName());
            role.setDescription(seed.description());
            role.setStatus(UserStatusEnum.ENABLED.name());
            role.setSortNo(seed.sortNo());
            sysRoleMapper.updateById(role);
        }
    }

    private void ensureUser(SysUserMapper sysUserMapper,
                            SysUserRoleMapper sysUserRoleMapper,
                            PasswordEncoder passwordEncoder,
                            String userNo,
                            String username,
                            String realName,
                            String departmentCode,
                            String departmentName,
                            Long roleId) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            user = sysUserMapper.selectAnyByUsername(username);
        }
        if (user == null) {
            user = sysUserMapper.selectAnyByUserNo(userNo);
        }
        if (user == null) {
            user = new SysUser();
            user.setUserNo(userNo);
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setRealName(realName);
            user.setDepartmentCode(departmentCode);
            user.setDepartmentName(departmentName);
            user.setStatus(UserStatusEnum.ENABLED.name());
            user.setPasswordChangeRequired(1);
            sysUserMapper.insert(user);
        } else if (user.getDeleted() != null && user.getDeleted() == 1) {
            user.setUserNo(userNo);
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setRealName(realName);
            user.setDepartmentCode(departmentCode);
            user.setDepartmentName(departmentName);
            user.setStatus(UserStatusEnum.ENABLED.name());
            user.setPasswordChangeRequired(1);
            sysUserMapper.restoreSeedUser(user);
        }

        Long userId = user.getId();
        SysUserRole existingRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId)
                .last("limit 1"));
        if (existingRole == null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private record RoleSeed(Long id, String roleCode, String roleName, String description, Integer sortNo) {
    }
}
