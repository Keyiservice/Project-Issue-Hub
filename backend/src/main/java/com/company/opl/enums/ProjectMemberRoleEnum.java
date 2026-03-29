package com.company.opl.enums;

import com.company.opl.exception.BusinessException;

public enum ProjectMemberRoleEnum {
    PROJECT_MANAGER("PROJECT_MANAGER", "项目经理"),
    TEAM_MEMBER("TEAM_MEMBER", "项目成员"),
    MECH_ENGINEER("MECH_ENGINEER", "机械工程师"),
    ELECTRICAL_TECH("ELECTRICAL_TECH", "电气技术员"),
    AUTOMATION_ENGINEER("AUTOMATION_ENGINEER", "自动化工程师"),
    INSTALL_TECH("INSTALL_TECH", "装机技术员"),
    MANUFACTURING_MANAGER("MANUFACTURING_MANAGER", "制造经理"),
    DESIGN_MANAGER("DESIGN_MANAGER", "设计经理"),
    QUALITY_ENGINEER("QUALITY_ENGINEER", "质量工程师"),
    PROCESS_ENGINEER("PROCESS_ENGINEER", "工艺工程师");

    private final String code;
    private final String label;

    ProjectMemberRoleEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ProjectMemberRoleEnum fromCode(String code) {
        for (ProjectMemberRoleEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new BusinessException("无效的项目岗位: " + code);
    }
}
