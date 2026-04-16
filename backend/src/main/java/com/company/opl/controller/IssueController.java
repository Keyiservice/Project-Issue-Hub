package com.company.opl.controller;

import com.company.opl.common.PageResult;
import com.company.opl.common.Result;
import com.company.opl.dto.issue.IssueAssignDTO;
import com.company.opl.dto.issue.IssueAttachmentAppendDTO;
import com.company.opl.dto.issue.IssueCommentCreateDTO;
import com.company.opl.dto.issue.IssueCreateDTO;
import com.company.opl.dto.issue.IssueFunctionUpdateDTO;
import com.company.opl.dto.issue.IssuePriorityUpdateDTO;
import com.company.opl.dto.issue.IssueStatusChangeDTO;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.service.IssueCommentService;
import com.company.opl.service.IssueFlowService;
import com.company.opl.service.IssueService;
import com.company.opl.vo.issue.IssueDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final IssueFlowService issueFlowService;
    private final IssueCommentService issueCommentService;

    public IssueController(IssueService issueService,
                           IssueFlowService issueFlowService,
                           IssueCommentService issueCommentService) {
        this.issueService = issueService;
        this.issueFlowService = issueFlowService;
        this.issueCommentService = issueCommentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "创建问题")
    public Result<Long> create(@Valid @RequestBody IssueCreateDTO dto) {
        return Result.success(issueService.createIssue(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "问题分页查询")
    public Result<PageResult<IssueDetailVO>> page(IssueQuery query) {
        return Result.success(issueService.pageIssues(query));
    }

    @GetMapping("/{issueId}")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "问题详情")
    public Result<IssueDetailVO> detail(@PathVariable Long issueId) {
        return Result.success(issueService.getIssueDetail(issueId));
    }

    @PutMapping("/{issueId}/status")
    @PreAuthorize("hasAnyRole('RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "变更问题状态")
    public Result<Void> changeStatus(@PathVariable Long issueId, @Valid @RequestBody IssueStatusChangeDTO dto) {
        issueFlowService.changeStatus(issueId, dto);
        return Result.success("状态更新成功", null);
    }

    @PostMapping("/{issueId}/comments")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "新增问题评论")
    public Result<Long> createComment(@PathVariable Long issueId, @Valid @RequestBody IssueCommentCreateDTO dto) {
        return Result.success(issueCommentService.createComment(issueId, dto));
    }

    @PutMapping("/{issueId}/assign")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "分派责任人")
    public Result<Void> assign(@PathVariable Long issueId, @Valid @RequestBody IssueAssignDTO dto) {
        issueService.assignIssue(issueId, dto);
        return Result.success("分派成功", null);
    }

    @PutMapping("/{issueId}/priority")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "调整优先级")
    public Result<Void> updatePriority(@PathVariable Long issueId, @Valid @RequestBody IssuePriorityUpdateDTO dto) {
        issueService.updatePriority(issueId, dto);
        return Result.success("优先级更新成功", null);
    }

    @PutMapping("/{issueId}/function")
    @PreAuthorize("hasAnyRole('RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "更新问题属性")
    public Result<Void> updateFunction(@PathVariable Long issueId, @Valid @RequestBody IssueFunctionUpdateDTO dto) {
        issueService.updateIssueFunction(issueId, dto);
        return Result.success("问题属性更新成功", null);
    }

    @PostMapping("/{issueId}/attachments")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "追加问题附件")
    public Result<Void> appendAttachments(@PathVariable Long issueId, @Valid @RequestBody IssueAttachmentAppendDTO dto) {
        issueService.appendAttachments(issueId, dto);
        return Result.success("附件补充成功", null);
    }

    @DeleteMapping("/{issueId}/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "删除问题主附件")
    public Result<Void> deleteAttachment(@PathVariable Long issueId, @PathVariable Long attachmentId) {
        issueService.deleteAttachment(issueId, attachmentId);
        return Result.success("附件已删除", null);
    }

    @DeleteMapping("/{issueId}")
    @PreAuthorize("hasAnyRole('SITE_USER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "删除误录问题")
    public Result<Void> delete(@PathVariable Long issueId) {
        issueService.deleteIssue(issueId);
        return Result.success("删除成功", null);
    }
}
