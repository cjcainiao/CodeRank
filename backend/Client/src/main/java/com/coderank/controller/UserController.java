package com.coderank.controller;

import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.vo.LoginVO;
import com.coderank.service.UserService;
import com.coderank.utils.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关操作接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @ApiOperation("统一登录接口")
    public ResponseResult<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO){
        return userService.login(loginDTO);
    }
}
