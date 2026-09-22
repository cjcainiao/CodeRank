package com.coderank.judge;

import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 评测器管理器。
 */
@Component
@RequiredArgsConstructor
public class JudgeManager {

    /** Spring 中所有 Judge 实现，key 为 Bean 名称。 */
    private final Map<String, Judge> judgeMap;

    /** 根据编程语言获取对应的评测器。 */
    public Judge getJudge(String language) {
        if (!StringUtils.hasText(language)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "编程语言不能为空");
        }

        return judgeMap.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(language.trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.BEAN_NOT_FOUND,
                        "暂不支持该编程语言：" + language
                ));
    }
}
