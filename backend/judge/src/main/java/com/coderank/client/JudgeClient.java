package com.coderank.client;

import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 远程评测服务客户端。
 */
@Component
@RequiredArgsConstructor
public class JudgeClient {

    /** 评测服务 HTTP 客户端。 */
    private final RestClient judgeRestClient;

    /**
     * 提交代码执行请求。
     */
    public JsonNode run(Object request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评测请求不能为空");
        }

        try {
            JsonNode response = judgeRestClient.post()
                    .uri("/run")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.REMOTE_ERROR, "评测服务未返回结果");
            }
            return response;
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.REMOTE_ERROR, "调用评测服务失败", e);
        }
    }

    /** 删除 go-judge 中缓存的文件。 */
    public void deleteFile(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return;
        }

        try {
            judgeRestClient.delete()
                    .uri("/file/{fileId}", fileId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.REMOTE_ERROR, "删除评测缓存文件失败", e);
        }
    }
}
