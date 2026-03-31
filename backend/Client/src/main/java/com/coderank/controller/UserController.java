package com.coderank.controller;

import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.dto.RegisterDTO;
import com.coderank.entity.vo.RegisterVO;
import com.coderank.entity.vo.UserInfoVO;
import com.coderank.service.UserService;
import com.coderank.utils.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关操作接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @ApiOperation("统一登录接口")
    public ResponseResult<UserInfoVO> login(@Validated @RequestBody LoginDTO loginDTO){
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    @ApiOperation("统一注册接口")
    public ResponseResult<RegisterVO> register(@Validated @RequestBody RegisterDTO registerDTO){
        return userService.register(registerDTO);
    }


    @PostMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public ResponseResult<UserInfoVO> getUserInfo(){
        return userService.getUserInfo();
    }

    @GetMapping("/logout")
    @ApiOperation("用户注销")
    public ResponseResult<Boolean> logout(){
        return userService.logout();
    }


    @PostMapping("/flushToken")
    @ApiOperation("刷新token")
    public ResponseResult<Boolean> flushToken(@RequestParam("refreshToken")
                                              @NotBlank(message = "刷新token不能为空")
                                              String refreshToken) {
        return userService.flushToken(refreshToken);
    }

}
