package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.opl.common.PageResult;
import com.company.opl.dto.user.UserBatchImportDTO;
import com.company.opl.dto.user.UserCreateDTO;
import com.company.opl.dto.user.UserImportItemDTO;
import com.company.opl.dto.user.UserUpdateDTO;
import com.company.opl.entity.Project;
import com.company.opl.entity.ProjectMember;
import com.company.opl.entity.SysRole;
import com.company.opl.entity.SysUser;
import com.company.opl.entity.SysUserRole;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.ProjectMapper;
import com.company.opl.mapper.ProjectMemberMapper;
import com.company.opl.mapper.SysRoleMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.mapper.SysUserRoleMapper;
import com.company.opl.query.user.UserQuery;
import com.company.opl.service.UserService;
import com.company.opl.util.UserNoGenerator;
import com.company.opl.vo.SimpleUserVO;
import com.company.opl.vo.user.RoleOptionVO;
import com.company.opl.vo.user.UserAdminVO;
import com.company.opl.vo.user.UserImportResultVO;
import com.company.opl.vo.user.UserProjectParticipationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final Set<String> ACCESS_ROLE_CODES = Set.of(
            "SITE_USER",
            "RESP_ENGINEER",
            "PROJECT_MANAGER",
            "MANAGEMENT",
            "ADMIN"
    );

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           SysRoleMapper sysRoleMapper,
                           ProjectMemberMapper projectMemberMapper,
                           ProjectMapper projectMapper,
                           PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.projectMapper = projectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .toList();
    }

    @Override
    public List<SimpleUserVO> listEnabledUsers() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, UserStatusEnum.ENABLED.name())
                .orderByAsc(SysUser::getUsername));
        return users.stream().map(user -> {
            SimpleUserVO vo = new SimpleUserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setId(user.getId());
            return vo;
        }).toList();
    }

    @Override
    public PageResult<UserAdminVO> pageUsers(UserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getRealName, query.getKeyword())
                    .or()
                    .like(SysUser::getDepartmentName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getDepartmentCode())) {
            wrapper.eq(SysUser::getDepartmentCode, query.getDepartmentCode());
        }

        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        List<SysUser> users = page.getRecords();
        Map<Long, List<SysRole>> roleMap = buildUserRoleMap(users);
        Map<Long, Integer> projectCountMap = buildUserProjectCountMap(users);
        List<UserAdminVO> records = users.stream()
                .map(user -> toAdminVO(user,
                        roleMap.getOrDefault(user.getId(), Collections.emptyList()),
                        projectCountMap.getOrDefault(user.getId(), 0)))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public List<RoleOptionVO> listRoleOptions() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, UserStatusEnum.ENABLED.name())
                        .orderByAsc(SysRole::getSortNo))
                .stream()
                .map(role -> {
                    RoleOptionVO vo = new RoleOptionVO();
                    vo.setId(role.getId());
                    vo.setRoleCode(role.getRoleCode());
                    vo.setRoleName(role.getRoleName());
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAdminVO createUser(UserCreateDTO request) {
        ensureUsernameAvailable(request.getUsername(), null);
        List<SysRole> roles = resolveRoles(request.getRoleCodes());

        SysUser user = new SysUser();
        user.setUserNo(generateUserNo());
        user.setUsername(request.getUsername().trim());
        user.setRealName(request.getRealName().trim());
        user.setMobile(trimToNull(request.getMobile()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setDepartmentCode(trimToNull(request.getDepartmentCode()));
        user.setDepartmentName(trimToNull(request.getDepartmentName()));
        user.setStatus(UserStatusEnum.ENABLED.name());
        user.setPasswordHash(passwordEncoder.encode(resolveInitialPassword(request.getInitialPassword())));
        user.setPasswordChangeRequired(1);
        sysUserMapper.insert(user);

        saveUserRoles(user.getId(), roles);
        return toAdminVO(user, roles, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAdminVO updateUser(Long userId, UserUpdateDTO request) {
        SysUser user = getUserById(userId);
        ensureUsernameAvailable(request.getUsername(), userId);
        List<SysRole> roles = resolveRoles(request.getRoleCodes());

        user.setUsername(request.getUsername().trim());
        user.setRealName(request.getRealName().trim());
        user.setMobile(trimToNull(request.getMobile()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setDepartmentCode(trimToNull(request.getDepartmentCode()));
        user.setDepartmentName(trimToNull(request.getDepartmentName()));
        sysUserMapper.updateById(user);

        sysUserRoleMapper.hardDeleteByUserId(userId);
        saveUserRoles(userId, roles);
        int projectCount = Math.toIntExact(projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getUserId, userId)));
        return toAdminVO(user, roles, projectCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserImportResultVO importUsers(UserBatchImportDTO request) {
        int successCount = 0;
        List<String> failedMessages = new ArrayList<>();
        String initialPassword = resolveInitialPassword(request.getInitialPassword());

        for (int index = 0; index < request.getItems().size(); index++) {
            UserImportItemDTO item = request.getItems().get(index);
            try {
                importSingleUser(item, initialPassword);
                successCount++;
            } catch (Exception ex) {
                failedMessages.add("第 " + (index + 1) + " 行: " + ex.getMessage());
            }
        }

        return UserImportResultVO.builder()
                .totalCount(request.getItems().size())
                .successCount(successCount)
                .failedCount(failedMessages.size())
                .failedMessages(failedMessages)
                .build();
    }

    @Override
    public List<UserProjectParticipationVO> listUserProjects(Long userId) {
        getUserById(userId);
        List<ProjectMember> members = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getUserId, userId)
                .orderByDesc(ProjectMember::getIsProjectManager)
                .orderByAsc(ProjectMember::getSortNo)
                .orderByAsc(ProjectMember::getCreatedAt));
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> projectIds = members.stream().map(ProjectMember::getProjectId).distinct().toList();
        Map<Long, Project> projectMap = projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, item -> item));
        return members.stream().map(member -> {
            Project project = projectMap.get(member.getProjectId());
            UserProjectParticipationVO vo = new UserProjectParticipationVO();
            vo.setProjectId(member.getProjectId());
            vo.setProjectRoleCode(member.getProjectRoleCode());
            vo.setProjectRoleName(member.getProjectRoleName());
            vo.setProjectManager(member.getIsProjectManager() != null && member.getIsProjectManager() == 1);
            vo.setCanAssignIssue(member.getCanAssignIssue() != null && member.getCanAssignIssue() == 1);
            vo.setCanVerifyIssue(member.getCanVerifyIssue() != null && member.getCanVerifyIssue() == 1);
            vo.setCanCloseIssue(member.getCanCloseIssue() != null && member.getCanCloseIssue() == 1);
            vo.setRemark(member.getRemark());
            if (project != null) {
                vo.setProjectNo(project.getProjectNo());
                vo.setProjectName(project.getProjectName());
                vo.setCustomerName(project.getCustomerName());
                vo.setProjectStatus(project.getStatus());
                vo.setProjectManagerName(project.getProjectManagerName());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        SysUser user = getUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setPasswordChangeRequired(1);
        user.setPasswordChangedAt(null);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindWechat(Long userId) {
        getUserById(userId);
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getWechatOpenid, null)
                .set(SysUser::getWechatUnionid, null)
                .set(SysUser::getWechatBoundAt, null));
    }

    private void importSingleUser(UserImportItemDTO item, String initialPassword) {
        ensureUsernameAvailable(item.getUsername(), null);
        List<String> roleCodes = item.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            roleCodes = List.of("SITE_USER");
        }
        List<SysRole> roles = resolveRoles(roleCodes);

        SysUser user = new SysUser();
        user.setUserNo(generateUserNo());
        user.setUsername(item.getUsername().trim());
        user.setRealName(item.getRealName().trim());
        user.setMobile(trimToNull(item.getMobile()));
        user.setEmail(trimToNull(item.getEmail()));
        user.setDepartmentCode(trimToNull(item.getDepartmentCode()));
        user.setDepartmentName(trimToNull(item.getDepartmentName()));
        user.setStatus(UserStatusEnum.ENABLED.name());
        user.setPasswordHash(passwordEncoder.encode(initialPassword));
        user.setPasswordChangeRequired(1);
        sysUserMapper.insert(user);
        saveUserRoles(user.getId(), roles);
    }

    private SysUser getUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim());
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        long count = sysUserMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("账号已存在: " + username);
        }
    }

    private String resolveInitialPassword(String initialPassword) {
        return StringUtils.hasText(initialPassword) ? initialPassword.trim() : DEFAULT_PASSWORD;
    }

    private String generateUserNo() {
        for (int i = 0; i < 10; i++) {
            String userNo = UserNoGenerator.generate();
            long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserNo, userNo));
            if (count == 0) {
                return userNo;
            }
        }
        throw new BusinessException("生成用户编号失败，请重试");
    }

    private List<SysRole> resolveRoles(List<String> roleCodes) {
        Set<String> normalizedCodes = roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalizedCodes.isEmpty()) {
            throw new BusinessException("至少选择一个角色");
        }
        normalizedCodes = normalizeRoleCodes(normalizedCodes);
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, normalizedCodes)
                .eq(SysRole::getStatus, UserStatusEnum.ENABLED.name()));
        if (roles.size() != normalizedCodes.size()) {
            Set<String> existingCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toSet());
            String missing = normalizedCodes.stream()
                    .filter(code -> !existingCodes.contains(code))
                    .collect(Collectors.joining(", "));
            throw new BusinessException("角色不存在或不可用: " + missing);
        }
        return roles;
    }

    private Set<String> normalizeRoleCodes(Set<String> normalizedCodes) {
        Set<String> intersection = new HashSet<>(normalizedCodes);
        intersection.retainAll(ACCESS_ROLE_CODES);
        if (intersection.isEmpty()) {
            normalizedCodes.add("SITE_USER");
        }
        return normalizedCodes;
    }

    private void saveUserRoles(Long userId, List<SysRole> roles) {
        for (SysRole role : roles) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    private Map<Long, List<SysRole>> buildUserRoleMap(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        Map<Long, SysRole> roleById = sysRoleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, role -> role));
        Map<Long, List<SysRole>> roleMap = new LinkedHashMap<>();
        for (SysUserRole userRole : userRoles) {
            SysRole role = roleById.get(userRole.getRoleId());
            if (role == null) {
                continue;
            }
            roleMap.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>()).add(role);
        }
        return roleMap;
    }

    private Map<Long, Integer> buildUserProjectCountMap(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        Map<Long, Integer> projectCounts = new LinkedHashMap<>();
        projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .in(ProjectMember::getUserId, userIds))
                .forEach(item -> projectCounts.merge(item.getUserId(), 1, Integer::sum));
        return projectCounts;
    }

    private UserAdminVO toAdminVO(SysUser user, List<SysRole> roles, Integer projectCount) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setUserNo(user.getUserNo());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setMobile(user.getMobile());
        vo.setEmail(user.getEmail());
        vo.setDepartmentCode(user.getDepartmentCode());
        vo.setDepartmentName(user.getDepartmentName());
        vo.setStatus(user.getStatus());
        vo.setWechatBound(StringUtils.hasText(user.getWechatOpenid()));
        vo.setWechatOpenid(user.getWechatOpenid());
        vo.setWechatBoundAt(user.getWechatBoundAt());
        vo.setPasswordChangeRequired(user.getPasswordChangeRequired() != null && user.getPasswordChangeRequired() == 1);
        vo.setPasswordChangedAt(user.getPasswordChangedAt());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setProjectCount(projectCount);
        vo.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
        vo.setRoleNames(roles.stream().map(SysRole::getRoleName).toList());
        return vo;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
