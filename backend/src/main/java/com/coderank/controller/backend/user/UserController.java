package com.coderank.controller.backend.user;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.user.UserCreateDTO;
import com.coderank.entity.dto.user.UserQueryDTO;
import com.coderank.entity.dto.user.UserUpdateDTO;
import com.coderank.entity.vo.user.UserVO;
import com.coderank.service.backend.user.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理接口。
 */
@RestController
@RequestMapping("/backend/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 分页查询用户
    @GetMapping
    public Result<PageResult<UserVO>> pageUsers(UserQueryDTO queryDTO) {
        return Result.success(userService.pageUsers(queryDTO));
    }

    // 按 id 查询用户
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    // 新增用户，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createUser(@RequestBody @Valid UserCreateDTO createDTO) {
        return Result.success(String.valueOf(userService.createUser(createDTO)));
    }

    // 修改用户
    @PutMapping
    public Result<Void> updateUser(@RequestBody @Valid UserUpdateDTO updateDTO) {
        userService.updateUser(updateDTO);
        return Result.success();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
