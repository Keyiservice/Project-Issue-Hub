INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '机械', 'MECHANICAL', '#2563eb', 10, 0, 'ENABLED', '机械类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '电气', 'ELECTRICAL', '#0f766e', 20, 0, 'ENABLED', '电气类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '自动化', 'AUTOMATION', '#7c3aed', 30, 0, 'ENABLED', '自动化类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '工艺', 'PROCESS', '#d97706', 40, 0, 'ENABLED', '工艺类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '质量', 'QUALITY', '#dc2626', 50, 0, 'ENABLED', '质量类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '交付', 'DELIVERY', '#059669', 60, 0, 'ENABLED', '交付类问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '其他', 'OTHER', '#6b7280', 70, 1, 'ENABLED', '其他问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_CATEGORY';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '现场检查', 'SITE_CHECK', '#2563eb', 10, 1, 'ENABLED', '现场检查发现'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_SOURCE';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '客户反馈', 'CUSTOMER_FEEDBACK', '#dc2626', 20, 0, 'ENABLED', '客户反馈问题'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_SOURCE';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '联调发现', 'COMMISSIONING', '#0f766e', 30, 0, 'ENABLED', '联调阶段发现'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_SOURCE';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '内部评审', 'INTERNAL_REVIEW', '#7c3aed', 40, 0, 'ENABLED', '内部评审发现'
FROM `dict_type`
WHERE `dict_code` = 'ISSUE_SOURCE';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '机械部', 'MECH', '#2563eb', 10, 0, 'ENABLED', '机械部门'
FROM `dict_type`
WHERE `dict_code` = 'RESP_DEPARTMENT';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '电气部', 'ELEC', '#0f766e', 20, 0, 'ENABLED', '电气部门'
FROM `dict_type`
WHERE `dict_code` = 'RESP_DEPARTMENT';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '自动化部', 'AUTO', '#7c3aed', 30, 0, 'ENABLED', '自动化部门'
FROM `dict_type`
WHERE `dict_code` = 'RESP_DEPARTMENT';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '采购部', 'PROCUREMENT', '#d97706', 40, 0, 'ENABLED', '采购部门'
FROM `dict_type`
WHERE `dict_code` = 'RESP_DEPARTMENT';

INSERT IGNORE INTO `dict_item` (`dict_type_id`, `item_label`, `item_value`, `item_color`, `sort_no`, `is_default`, `status`, `remark`)
SELECT `id`, '制造部', 'MFG', '#dc2626', 50, 0, 'ENABLED', '制造部门'
FROM `dict_type`
WHERE `dict_code` = 'RESP_DEPARTMENT';
