package com.company.opl.vo.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserImportResultVO {
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private List<String> failedMessages;
}
