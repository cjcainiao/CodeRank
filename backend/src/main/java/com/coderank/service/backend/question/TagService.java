package com.coderank.service.backend.question;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.question.TagCreateDTO;
import com.coderank.entity.dto.question.TagQueryDTO;
import com.coderank.entity.dto.question.TagUpdateDTO;
import com.coderank.entity.vo.question.TagVO;

/**
 * 后台标签管理业务。
 */
public interface TagService {

    // 分页查询标签
    PageResult<TagVO> pageTags(TagQueryDTO queryDTO);

    // 按 id 查询标签
    TagVO getTagById(Long id);

    // 新增标签
    Long createTag(TagCreateDTO createDTO);

    // 修改标签
    void updateTag(TagUpdateDTO updateDTO);

    // 删除标签
    void deleteTag(Long id);
}
