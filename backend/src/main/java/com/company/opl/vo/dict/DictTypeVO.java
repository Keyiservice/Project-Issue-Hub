package com.company.opl.vo.dict;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictTypeVO {
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private String remark;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
