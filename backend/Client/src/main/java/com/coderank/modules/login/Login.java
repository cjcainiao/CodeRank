package com.coderank.modules.login;

import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.vo.LoginVO;

/**
 * 登录接口
 */
public interface Login {

    /**
     * 登录逻辑接口
     * @param loginDTO
     * @return
     */
    LoginVO doLogin(LoginDTO loginDTO);

}
