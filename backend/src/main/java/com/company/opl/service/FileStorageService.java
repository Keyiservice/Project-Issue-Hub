package com.company.opl.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFileInfo upload(MultipartFile file, String bizFolder);

    String getPreviewUrl(String bucketName, String objectKey);

    StoredFileContent load(String bucketName, String objectKey);

    record StoredFileInfo(String fileName, String objectKey, String fileUrl, Long fileSize, String contentType) {
    }

    record StoredFileContent(byte[] content, String contentType) {
    }
}
