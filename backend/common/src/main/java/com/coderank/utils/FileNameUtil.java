package com.coderank.utils;

import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件名称工具类。
 */
public final class FileNameUtil {

    /**
     * 文件名使用时、分、秒和毫秒。
     */
    private static final DateTimeFormatter FILE_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("HHmmssSSS");

    private FileNameUtil() {
    }

    /**
     * 根据原始文件名生成按时间分目录的相对路径。
     *
     * @param originalFileName 原始文件名
     * @return 文件相对路径，例如 2026/08/26/143012123-550e8400e29b41d4a7164461f4d3b2a6.png
     */
    public static String generateFileName(String originalFileName) {
        return generateFileName(originalFileName, LocalDateTime.now());
    }

    /**
     * 获取文件后缀，不包含点号。
     *
     * @param originalFileName 原始文件名
     * @return 文件后缀；没有后缀时返回空字符串
     */
    public static String getExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原始文件名不能为空");
        }

        int separatorIndex = Math.max(
                originalFileName.lastIndexOf('/'),
                originalFileName.lastIndexOf('\\')
        );
        String fileName = originalFileName.substring(separatorIndex + 1);
        int extensionIndex = fileName.lastIndexOf('.');

        // 隐藏文件和以点号结尾的文件都视为没有后缀。
        if (extensionIndex <= 0 || extensionIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(extensionIndex + 1);
    }

    /**
     * 使用指定时间生成文件名称，供内部测试使用。
     */
    static String generateFileName(String originalFileName, LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "生成文件名称的时间不能为空");
        }

        String extension = getExtension(originalFileName);
        String suffix = extension.isEmpty() ? "" : "." + extension;

        return String.format(
                Locale.ROOT,
                "%04d/%02d/%02d/%s-%s%s",
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.format(FILE_NAME_FORMATTER),
                UUID.randomUUID(),
                suffix
        );
    }
}
