package com.coderank.service.backend.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.TagCreateDTO;
import com.coderank.entity.dto.TagQueryDTO;
import com.coderank.entity.dto.TagUpdateDTO;
import com.coderank.entity.pojo.Tag;
import com.coderank.entity.vo.TagVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.TagMapper;
import com.coderank.service.backend.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台标签管理业务实现。
 */
@Service
public class TagServiceImpl implements TagService {

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：排序值 */
    private static final String SORT_FIELD_SORT_ORDER = "sortOrder";

    /** 排序字段：创建时间 */
    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    /** 排序字段：更新时间 */
    private static final String SORT_FIELD_UPDATE_TIME = "updateTime";

    /** 默认排序值 */
    private static final int DEFAULT_SORT_ORDER = 0;

    private final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    // 分页查询标签
    @Override
    public PageResult<TagVO> pageTags(TagQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .like(StringUtils.hasText(queryDTO.getName()), Tag::getName, queryDTO.getName());

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<Tag> page = tagMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, this::toVO);
    }

    // 按白名单字段排序，未指定字段时按排序值升序，便于标签固定展示顺序
    private void applySort(LambdaQueryWrapper<Tag> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_SORT_ORDER -> wrapper.orderBy(true, ascending, Tag::getSortOrder);
            case SORT_FIELD_CREATE_TIME -> wrapper.orderBy(true, ascending, Tag::getCreateTime);
            case SORT_FIELD_UPDATE_TIME -> wrapper.orderBy(true, ascending, Tag::getUpdateTime);
            default -> wrapper.orderBy(true, true, Tag::getSortOrder).orderByAsc(Tag::getId);
        }
    }

    // 按 id 查询标签
    @Override
    public TagVO getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }
        return toVO(tag);
    }

    // 新增标签
    @Override
    public Long createTag(TagCreateDTO createDTO) {
        if (existsName(createDTO.getName())) {
            throw new BusinessException(409, "标签名已存在");
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(createDTO, tag);
        tag.setSortOrder(createDTO.getSortOrder() == null ? DEFAULT_SORT_ORDER : createDTO.getSortOrder());

        tagMapper.insert(tag);
        return tag.getId();
    }

    // 修改标签
    @Override
    public void updateTag(TagUpdateDTO updateDTO) {
        Tag existing = tagMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "标签不存在");
        }
        if (StringUtils.hasText(updateDTO.getName()) && existsNameExceptId(updateDTO.getName(), updateDTO.getId())) {
            throw new BusinessException(409, "标签名已存在");
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(updateDTO, tag);
        tagMapper.updateById(tag);
    }

    // 删除标签
    @Override
    public void deleteTag(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException(404, "标签不存在");
        }
        tagMapper.deleteById(id);
    }

    // 标签名是否已存在，比较时忽略大小写与首尾空格
    private boolean existsName(String name) {
        return tagMapper.exists(new LambdaQueryWrapper<Tag>()
                .apply("lower(trim(name)) = lower(trim({0}))", name));
    }

    // 标签名是否被除指定标签外的其他人使用
    private boolean existsNameExceptId(String name, Long id) {
        return tagMapper.exists(new LambdaQueryWrapper<Tag>()
                .apply("lower(trim(name)) = lower(trim({0}))", name)
                .ne(Tag::getId, id));
    }

    // 实体转视图对象
    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
