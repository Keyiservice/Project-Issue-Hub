package com.company.opl.vo.dict;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictItemVO {
    private Long id;
    private Long dictTypeId;
    private String itemLabel;
    private String itemValue;
    private String itemColor;
    private Integer sortNo;
    private Boolean isDefault;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
