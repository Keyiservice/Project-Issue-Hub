package com.company.opl.vo.dict;

import lombok.Data;

import java.util.List;

@Data
public class DictTypeDetailVO {
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private String remark;
    private List<DictItemVO> items;
}
