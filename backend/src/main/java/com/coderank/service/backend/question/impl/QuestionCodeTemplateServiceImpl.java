package com.coderank.service.backend.question.impl;

import com.coderank.common.enums.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.entity.dto.question.QuestionCodeTemplateCreateDTO;
import com.coderank.entity.dto.question.QuestionCodeTemplateUpdateDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionCodeTemplate;
import com.coderank.entity.vo.question.QuestionCodeTemplateVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.question.QuestionCodeTemplateMapper;
import com.coderank.mapper.backend.question.QuestionMapper;
import com.coderank.service.backend.question.QuestionCodeTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台题目代码模板管理业务实现。
 */
@Service
public class QuestionCodeTemplateServiceImpl implements QuestionCodeTemplateService {

    private final QuestionCodeTemplateMapper templateMapper;

    private final QuestionMapper questionMapper;

    public QuestionCodeTemplateServiceImpl(QuestionCodeTemplateMapper templateMapper,
                                           QuestionMapper questionMapper) {
        this.templateMapper = templateMapper;
        this.questionMapper = questionMapper;
    }

    // 查询题目下的全部代码模板，按语言编号升序
    @Override
    public List<QuestionCodeTemplateVO> listByQuestionId(Long questionId) {
        if (questionMapper.selectById(questionId) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }
        List<QuestionCodeTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<QuestionCodeTemplate>()
                        .eq(QuestionCodeTemplate::getQuestionId, questionId)
                        .orderByAsc(QuestionCodeTemplate::getLanguage));
        return templates.stream().map(this::toVO).toList();
    }

    // 按 id 查询代码模板
    @Override
    public QuestionCodeTemplateVO getTemplateById(Long id) {
        QuestionCodeTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "代码模板不存在");
        }
        return toVO(template);
    }

    // 新增代码模板
    @Override
    public Long createTemplate(QuestionCodeTemplateCreateDTO createDTO) {
        if (questionMapper.selectById(createDTO.getQuestionId()) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "题目不存在");
        }
        if (existsLanguage(createDTO.getQuestionId(), createDTO.getLanguage(), null)) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "该语言的代码模板已存在");
        }

        QuestionCodeTemplate template = QuestionCodeTemplate.builder()
                .questionId(createDTO.getQuestionId())
                .language(createDTO.getLanguage())
                .template(createDTO.getTemplate())
                .stubTemplate(createDTO.getStubTemplate())
                .build();
        templateMapper.insert(template);
        return template.getId();
    }

    // 修改代码模板
    @Override
    public void updateTemplate(QuestionCodeTemplateUpdateDTO updateDTO) {
        QuestionCodeTemplate existing = templateMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "代码模板不存在");
        }
        if (updateDTO.getLanguage() != null
                && existsLanguage(existing.getQuestionId(), updateDTO.getLanguage(), updateDTO.getId())) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "该语言的代码模板已存在");
        }
        if (updateDTO.getTemplate() != null && !StringUtils.hasText(updateDTO.getTemplate())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "初始代码不能为空");
        }

        QuestionCodeTemplate template = QuestionCodeTemplate.builder()
                .id(updateDTO.getId())
                .language(updateDTO.getLanguage())
                .template(updateDTO.getTemplate())
                .stubTemplate(updateDTO.getStubTemplate())
                .build();
        templateMapper.updateById(template);
    }

    // 删除代码模板
    @Override
    public void deleteTemplate(Long id) {
        if (templateMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "代码模板不存在");
        }
        templateMapper.deleteById(id);
    }

    // 同一题目下某语言是否已有模板，excludeId 用于修改时排除自身
    private boolean existsLanguage(Long questionId, Integer language, Long excludeId) {
        LambdaQueryWrapper<QuestionCodeTemplate> wrapper = new LambdaQueryWrapper<QuestionCodeTemplate>()
                .eq(QuestionCodeTemplate::getQuestionId, questionId)
                .eq(QuestionCodeTemplate::getLanguage, language);
        if (excludeId != null) {
            wrapper.ne(QuestionCodeTemplate::getId, excludeId);
        }
        return templateMapper.exists(wrapper);
    }

    // 实体转视图对象
    private QuestionCodeTemplateVO toVO(QuestionCodeTemplate template) {
        return QuestionCodeTemplateVO.builder()
                .id(template.getId())
                .questionId(template.getQuestionId())
                .language(template.getLanguage())
                .template(template.getTemplate())
                .stubTemplate(template.getStubTemplate())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
                .build();
    }
}
