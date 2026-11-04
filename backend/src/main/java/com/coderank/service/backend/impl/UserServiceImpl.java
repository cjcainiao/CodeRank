package com.coderank.service.backend.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.UserCreateDTO;
import com.coderank.entity.dto.UserQueryDTO;
import com.coderank.entity.dto.UserUpdateDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.UserVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.UserMapper;
import com.coderank.service.backend.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台用户管理业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：创建时间 */
    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    /** 排序字段：更新时间 */
    private static final String SORT_FIELD_UPDATE_TIME = "updateTime";

    /** 账号状态：正常 */
    private static final int STATUS_NORMAL = 0;

    /** 角色：普通用户 */
    private static final int ROLE_USER = 0;

    /** 性别：未知 */
    private static final int GENDER_UNKNOWN = 0;

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 分页查询用户
    @Override
    public PageResult<UserVO> pageUsers(UserQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
                .like(StringUtils.hasText(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
                .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getRole() != null, User::getRole, queryDTO.getRole());

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, this::toVO);
    }

    // 按白名单字段排序，未知字段回退到 id
    private void applySort(LambdaQueryWrapper<User> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_CREATE_TIME -> wrapper.orderBy(true, ascending, User::getCreateTime);
            case SORT_FIELD_UPDATE_TIME -> wrapper.orderBy(true, ascending, User::getUpdateTime);
            default -> wrapper.orderBy(true, ascending, User::getId);
        }
    }

    // 按 id 查询用户
    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toVO(user);
    }

    // 新增用户
    @Override
    public Long createUser(UserCreateDTO createDTO) {
        if (existsUsername(createDTO.getUsername())) {
            throw new BusinessException(409, "登录账号已存在");
        }
        if (StringUtils.hasText(createDTO.getEmail()) && existsEmail(createDTO.getEmail())) {
            throw new BusinessException(409, "邮箱已被使用");
        }

        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setGender(createDTO.getGender() == null ? GENDER_UNKNOWN : createDTO.getGender());
        user.setRole(createDTO.getRole() == null ? ROLE_USER : createDTO.getRole());
        user.setStatus(STATUS_NORMAL);

        userMapper.insert(user);
        return user.getId();
    }

    // 修改用户
    @Override
    public void updateUser(UserUpdateDTO updateDTO) {
        User existing = userMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (StringUtils.hasText(updateDTO.getEmail()) && existsEmailExceptId(updateDTO.getEmail(), updateDTO.getId())) {
            throw new BusinessException(409, "邮箱已被使用");
        }

        User user = new User();
        BeanUtils.copyProperties(updateDTO, user);
        userMapper.updateById(user);
    }

    // 删除用户
    @Override
    public void deleteUser(Long id) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.deleteById(id);
    }

    // 登录账号是否已存在
    private boolean existsUsername(String username) {
        return userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    // 邮箱是否已被使用
    private boolean existsEmail(String email) {
        return userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    // 邮箱是否被除指定用户外的其他人使用
    private boolean existsEmailExceptId(String email, Long id) {
        return userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .ne(User::getId, id));
    }

    // 实体转视图对象，剔除密码
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
