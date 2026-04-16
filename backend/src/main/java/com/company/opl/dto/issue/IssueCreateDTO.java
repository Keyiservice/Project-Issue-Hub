package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    @NotNull(message = "项目不能为空")
    private Long projectId;
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    private String deviceName;
    private String moduleName;
    @NotBlank(message = "闂灞炴€т笉鑳戒负绌?")
    private String issueFunctionCode;
    @NotBlank(message = "问题分类不能为空")
    private String categoryCode;
    @NotBlank(message = "问题来源不能为空")
    private String sourceCode;
    @NotBlank(message = "优先级不能为空")
    private String priority;
    @NotBlank(message = "影响等级不能为空")
    private String impactLevel;
    private Boolean affectShipment = Boolean.FALSE;
    private Boolean affectCommissioning = Boolean.FALSE;
    private Boolean needShutdown = Boolean.FALSE;
    private Long ownerId;
    private String ownerName;
    private String ownerDepartmentCode;
    private String ownerDepartmentName;
    private LocalDateTime occurredAt;
    private LocalDateTime dueAt;
    private List<IssueAttachmentDTO> attachments;
}
