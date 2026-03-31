package com.coderank.modules.register;

import com.coderank.entity.dto.RegisterDTO;
import com.coderank.entity.vo.RegisterVO;

/**
 * 注册接口
 */
public interface Register {

    /**
     * 注册逻辑接口
     * @param registerDTO
     * @return
     */
    RegisterVO doRegister(RegisterDTO registerDTO);
}
