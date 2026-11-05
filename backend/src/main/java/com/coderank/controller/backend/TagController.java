package com.coderank.controller.backend;

import com.coderank.common.result.PageResult;
import com.coderank.common.result.Result;
import com.coderank.entity.dto.TagCreateDTO;
import com.coderank.entity.dto.TagQueryDTO;
import com.coderank.entity.dto.TagUpdateDTO;
import com.coderank.entity.vo.TagVO;
import com.coderank.service.backend.TagService;
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
 * 后台标签管理接口。
 */
@RestController
@RequestMapping("/backend/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // 分页查询标签
    @GetMapping
    public Result<PageResult<TagVO>> pageTags(TagQueryDTO queryDTO) {
        return Result.success(tagService.pageTags(queryDTO));
    }

    // 按 id 查询标签
    @GetMapping("/{id}")
    public Result<TagVO> getTagById(@PathVariable Long id) {
        return Result.success(tagService.getTagById(id));
    }

    // 新增标签，主键转字符串避免前端精度丢失
    @PostMapping
    public Result<String> createTag(@RequestBody @Valid TagCreateDTO createDTO) {
        return Result.success(String.valueOf(tagService.createTag(createDTO)));
    }

    // 修改标签
    @PutMapping
    public Result<Void> updateTag(@RequestBody @Valid TagUpdateDTO updateDTO) {
        tagService.updateTag(updateDTO);
        return Result.success();
    }

    // 删除标签
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
