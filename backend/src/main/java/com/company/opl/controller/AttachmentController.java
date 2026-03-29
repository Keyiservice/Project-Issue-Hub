package com.company.opl.controller;

import com.company.opl.common.Result;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueAttachmentMapper;
import com.company.opl.service.FileStorageService;
import com.company.opl.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final FileStorageService fileStorageService;
    private final IssueAttachmentMapper issueAttachmentMapper;

    public AttachmentController(FileStorageService fileStorageService, IssueAttachmentMapper issueAttachmentMapper) {
        this.fileStorageService = fileStorageService;
        this.issueAttachmentMapper = issueAttachmentMapper;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "上传附件")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "bizFolder", defaultValue = "issue") String bizFolder) {
        validateFile(file);
        FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.upload(file, bizFolder);
        FileUploadVO vo = FileUploadVO.builder()
                .fileName(storedFileInfo.fileName())
                .objectKey(storedFileInfo.objectKey())
                .fileUrl(storedFileInfo.fileUrl())
                .fileSize(storedFileInfo.fileSize())
                .contentType(storedFileInfo.contentType())
                .build();
        return Result.success(vo);
    }

    @GetMapping("/{attachmentId}/content")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取附件内容")
    public ResponseEntity<ByteArrayResource> getContent(@PathVariable("attachmentId") Long attachmentId) {
        IssueAttachment attachment = issueAttachmentMapper.selectById(attachmentId);
        if (attachment == null || attachment.getDeleted() == 1) {
            throw new BusinessException("附件不存在");
        }
        FileStorageService.StoredFileContent storedFileContent =
                fileStorageService.load(attachment.getBucketName(), attachment.getObjectKey());
        MediaType mediaType = resolveMediaType(attachment.getContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFileContent.content().length)
                .cacheControl(CacheControl.noCache())
                .body(new ByteArrayResource(storedFileContent.content()));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片或视频");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("单个附件不能超过 5MB");
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean image = contentType != null && contentType.startsWith("image/");
        boolean video = contentType != null && contentType.startsWith("video/");
        boolean imageByExt = fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".webp");
        boolean videoByExt = fileName.endsWith(".mp4")
                || fileName.endsWith(".mov")
                || fileName.endsWith(".m4v");

        if (!(image || video || imageByExt || videoByExt)) {
            throw new BusinessException("当前只支持图片和视频");
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
