package com.company.opl.util;

import com.company.opl.enums.FileTypeEnum;

public final class FileTypeUtils {

    private FileTypeUtils() {
    }

    public static String resolveFileType(String contentType, String fileName) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return FileTypeEnum.IMAGE.name();
            }
            if (contentType.startsWith("video/")) {
                return FileTypeEnum.VIDEO.name();
            }
            if (contentType.startsWith("audio/")) {
                return FileTypeEnum.AUDIO.name();
            }
        }
        if (fileName != null && fileName.contains(".")) {
            String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if ("jpg".equals(suffix) || "jpeg".equals(suffix) || "png".equals(suffix) || "gif".equals(suffix) || "webp".equals(suffix)) {
                return FileTypeEnum.IMAGE.name();
            }
            if ("mp4".equals(suffix) || "mov".equals(suffix) || "avi".equals(suffix) || "m4v".equals(suffix)) {
                return FileTypeEnum.VIDEO.name();
            }
        }
        return FileTypeEnum.FILE.name();
    }

    public static String resolveFileExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}

