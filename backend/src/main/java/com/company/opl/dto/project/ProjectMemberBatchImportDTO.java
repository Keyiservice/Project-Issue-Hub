package com.company.opl.dto.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProjectMemberBatchImportDTO {

    @Valid
    @NotEmpty(message = "导入项不能为空")
    private List<ProjectMemberImportItemDTO> items;
}
