package com.coderank.client;

import com.coderank.config.properties.MinioProperties;
import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * MinIO 文件操作客户端。
 */
@Component
@RequiredArgsConstructor
public class MinioFileClient {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /** 将文本内容上传到指定文件。 */
    public void putFileContent(String bucketName, String objectName, String content) {
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(objectName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MinIO 存储桶和文件路径不能为空");
        }

        byte[] data = (content == null ? "" : content).getBytes(StandardCharsets.UTF_8);
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, data.length, -1)
                                .contentType("text/plain; charset=utf-8")
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.FILE_UPLOAD_FAILED,
                    "上传 MinIO 文件失败：" + objectName,
                    exception
            );
        }
    }

    /** 使用默认存储桶读取指定文件内容。 */
    public String getFileContent(String objectName) {
        return getFileContent(properties.getBucketName(), objectName);
    }

    /** 读取指定存储桶中的文件内容。 */
    public String getFileContent(String bucketName, String objectName) {
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(objectName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MinIO 存储桶和文件路径不能为空");
        }

        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.FILE_DOWNLOAD_FAILED,
                    "读取 MinIO 文件失败：" + objectName,
                    exception
            );
        }
    }
}
