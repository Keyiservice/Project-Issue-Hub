package com.company.opl.vo.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsVO {
    private Integer totalIssues;
    private Integer openIssues;
    private Integer highPriorityIssues;
    private Integer overdueIssues;
    private List<String> trendDates;
    private List<Integer> createdTrend;
    private List<Integer> closedTrend;
}

