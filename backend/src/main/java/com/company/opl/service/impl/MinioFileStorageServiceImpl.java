package com.company.opl.service.impl;

import com.company.opl.config.MinioProperties;
import com.company.opl.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    private static final int PREVIEW_URL_EXPIRE_MINUTES = 120;

    private final MinioClient minioClient;
    private final MinioClient previewMinioClient;
    private final MinioProperties minioProperties;

    public MinioFileStorageServiceImpl(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.previewMinioClient = MinioClient.builder()
                .endpoint(resolvePreviewEndpoint(minioProperties))
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Override
    public StoredFileInfo upload(MultipartFile file, String bizFolder) {
        try {
            ensureBucketExists();
            String objectKey = buildObjectKey(file.getOriginalFilename(), bizFolder);
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
            String fileUrl = getPreviewUrl(minioProperties.getBucket(), objectKey);
            return new StoredFileInfo(
                    file.getOriginalFilename(),
                    objectKey,
                    fileUrl,
                    file.getSize(),
                    file.getContentType()
            );
        } catch (Exception ex) {
            log.error("涓婁紶鏂囦欢澶辫触", ex);
            throw new IllegalStateException("涓婁紶鏂囦欢澶辫触: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getPreviewUrl(String bucketName, String objectKey) {
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(objectKey)) {
            return null;
        }
        try {
            return previewMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(PREVIEW_URL_EXPIRE_MINUTES, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception ex) {
            log.error("鐢熸垚闄勪欢棰勮鍦板潃澶辫触, bucket={}, objectKey={}", bucketName, objectKey, ex);
            return null;
        }
    }

    @Override
    public StoredFileContent load(String bucketName, String objectKey) {
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("bucketName 鍜?objectKey 涓嶈兘涓虹┖");
        }
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return new StoredFileContent(outputStream.toByteArray(), null);
        } catch (Exception ex) {
            log.error("璇诲彇闄勪欢鍐呭澶辫触, bucket={}, objectKey={}", bucketName, objectKey, ex);
            return null;
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }

    private String buildObjectKey(String originalFileName, String bizFolder) {
        String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                : "";
        return bizFolder + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private String resolvePreviewEndpoint(MinioProperties properties) {
        String envEndpoint = System.getenv("MINIO_PUBLIC_ENDPOINT");
        if (StringUtils.hasText(envEndpoint)) {
            return envEndpoint;
        }
        return StringUtils.hasText(properties.getPublicEndpoint())
                ? properties.getPublicEndpoint()
                : properties.getEndpoint();
    }
}

