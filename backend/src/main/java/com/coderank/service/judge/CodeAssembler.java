package com.coderank.service.judge;

import com.coderank.common.enums.ResultCodeEnum;
import com.coderank.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 桩程序拼接，把用户代码填入桩程序的占位符得到完整程序。
 */
@Component
public class CodeAssembler {

    /** 桩程序里的用户代码占位符 */
    public static final String CODE_PLACEHOLDER = "{{code}}";

    // 组装完整程序，没有桩程序说明提交的就是完整程序，原样返回
    public String assemble(String sourceCode, String stubTemplate) {
        String code = sourceCode == null ? "" : sourceCode;
        if (!StringUtils.hasText(stubTemplate)) {
            return code;
        }

        if (!stubTemplate.contains(CODE_PLACEHOLDER)) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "桩程序缺少 " + CODE_PLACEHOLDER + " 占位符");
        }

        // String.replace 是字面量替换，用户代码里的特殊字符不会被当作正则处理
        return stubTemplate.replace(CODE_PLACEHOLDER, code);
    }
}
