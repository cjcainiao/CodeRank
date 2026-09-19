package com.coderank.utils;

import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 文件指纹计算工具类。
 */
public final class FileHashUtil {

    public static final String MD5 = "MD5";
    public static final String SHA_1 = "SHA-1";
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";

    private static final int BUFFER_SIZE = 8192;

    private FileHashUtil() {
    }

    /**
     * 根据输入流计算文件指纹，计算完成后自动关闭输入流。
     *
     * @param inputStream 文件输入流
     * @param algorithm   指纹算法，例如 MD5、SHA-256
     * @return 小写十六进制文件指纹
     */
    public static String calculate(InputStream inputStream, String algorithm) {
        if (inputStream == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件输入流不能为空");
        }

        try (InputStream stream = inputStream) {
            MessageDigest messageDigest = createMessageDigest(algorithm);
            byte[] buffer = new byte[BUFFER_SIZE];
            int readLength;

            while ((readLength = stream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, readLength);
            }
            return HexFormat.of().formatHex(messageDigest.digest());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_HASH_CALCULATION_FAILED, e);
        }
    }

    /**
     * 根据字节内容计算文件指纹。
     *
     * @param content   文件内容
     * @param algorithm 指纹算法
     * @return 小写十六进制文件指纹
     */
    public static String calculate(byte[] content, String algorithm) {
        if (content == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件内容不能为空");
        }

        MessageDigest messageDigest = createMessageDigest(algorithm);
        return HexFormat.of().formatHex(messageDigest.digest(content));
    }

    /**
     * 根据本地文件路径计算文件指纹。
     *
     * @param filePath  文件路径
     * @param algorithm 指纹算法
     * @return 小写十六进制文件指纹
     */
    public static String calculate(Path filePath, String algorithm) {
        if (filePath == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件路径不能为空");
        }

        try {
            return calculate(Files.newInputStream(filePath), algorithm);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_HASH_CALCULATION_FAILED, e);
        }
    }

    private static MessageDigest createMessageDigest(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件指纹算法不能为空");
        }

        String normalizedAlgorithm = normalizeAlgorithm(algorithm);
        try {
            return MessageDigest.getInstance(normalizedAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "不支持的文件指纹算法：" + algorithm,
                    e
            );
        }
    }

    private static String normalizeAlgorithm(String algorithm) {
        String normalized = algorithm.trim()
                .toUpperCase(Locale.ROOT)
                .replace("_", "-")
                .replace(" ", "");

        return switch (normalized) {
            case "SHA1" -> SHA_1;
            case "SHA256" -> SHA_256;
            case "SHA512" -> SHA_512;
            default -> normalized;
        };
    }
}
