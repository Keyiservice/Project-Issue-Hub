package com.company.opl.service;

import com.company.opl.common.PageResult;
import com.company.opl.dto.user.UserBatchImportDTO;
import com.company.opl.dto.user.UserCreateDTO;
import com.company.opl.dto.user.UserUpdateDTO;
import com.company.opl.query.user.UserQuery;
import com.company.opl.vo.SimpleUserVO;
import com.company.opl.vo.user.RoleOptionVO;
import com.company.opl.vo.user.UserAdminVO;
import com.company.opl.vo.user.UserImportResultVO;
import com.company.opl.vo.user.UserProjectParticipationVO;

import java.util.List;

public interface UserService {

    List<String> getUserRoleCodes(Long userId);

    List<SimpleUserVO> listEnabledUsers();

    PageResult<UserAdminVO> pageUsers(UserQuery query);

    List<RoleOptionVO> listRoleOptions();

    UserAdminVO createUser(UserCreateDTO request);

    UserAdminVO updateUser(Long userId, UserUpdateDTO request);

    UserImportResultVO importUsers(UserBatchImportDTO request);

    List<UserProjectParticipationVO> listUserProjects(Long userId);

    void resetPassword(Long userId);

    void unbindWechat(Long userId);
}
