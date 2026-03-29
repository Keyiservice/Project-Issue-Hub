package com.company.opl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.common.Result;
import com.company.opl.entity.Issue;
import com.company.opl.enums.IssuePriorityEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.vo.stats.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private static final List<String> CLOSED_STATUSES = List.of(
            IssueStatusEnum.CLOSED.getCode(),
            IssueStatusEnum.CANCELED.getCode()
    );

    private final IssueMapper issueMapper;

    public StatsController(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "仪表盘统计")
    public Result<DashboardStatsVO> dashboard(@RequestParam(required = false) Long projectId) {
        int totalIssues = count(baseWrapper(projectId));
        int openIssues = count(baseWrapper(projectId)
                .notIn(Issue::getStatus, CLOSED_STATUSES));
        int highPriorityIssues = count(baseWrapper(projectId)
                .in(Issue::getPriority, List.of(IssuePriorityEnum.HIGH.name(), IssuePriorityEnum.CRITICAL.name()))
                .notIn(Issue::getStatus, CLOSED_STATUSES));
        int overdueIssues = count(baseWrapper(projectId)
                .lt(Issue::getDueAt, LocalDateTime.now())
                .notIn(Issue::getStatus, CLOSED_STATUSES));

        List<String> trendDates = new ArrayList<>();
        List<Integer> createdTrend = new ArrayList<>();
        List<Integer> closedTrend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            trendDates.add(date.toString());
            createdTrend.add(count(baseWrapper(projectId)
                    .ge(Issue::getCreatedAt, start)
                    .lt(Issue::getCreatedAt, end)));
            closedTrend.add(count(baseWrapper(projectId)
                    .eq(Issue::getStatus, IssueStatusEnum.CLOSED.getCode())
                    .ge(Issue::getClosedAt, start)
                    .lt(Issue::getClosedAt, end)));
        }

        return Result.success(DashboardStatsVO.builder()
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .highPriorityIssues(highPriorityIssues)
                .overdueIssues(overdueIssues)
                .trendDates(trendDates)
                .createdTrend(createdTrend)
                .closedTrend(closedTrend)
                .build());
    }

    private LambdaQueryWrapper<Issue> baseWrapper(Long projectId) {
        return new LambdaQueryWrapper<Issue>()
                .eq(projectId != null, Issue::getProjectId, projectId);
    }

    private int count(LambdaQueryWrapper<Issue> wrapper) {
        return Math.toIntExact(issueMapper.selectCount(wrapper));
    }
}
