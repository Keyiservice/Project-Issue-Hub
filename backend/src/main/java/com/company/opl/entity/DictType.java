package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("dict_type")
@EqualsAndHashCode(callSuper = true)
public class DictType extends BaseEntity {

    @TableId
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private String remark;
}
