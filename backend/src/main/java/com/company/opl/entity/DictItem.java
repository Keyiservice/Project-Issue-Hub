package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("dict_item")
@EqualsAndHashCode(callSuper = true)
public class DictItem extends BaseEntity {

    @TableId
    private Long id;
    private Long dictTypeId;
    private String itemLabel;
    private String itemValue;
    private String itemColor;
    private Integer sortNo;
    private Integer isDefault;
    private String status;
    private String remark;
}
