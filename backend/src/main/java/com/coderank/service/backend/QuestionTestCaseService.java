package com.coderank.service.backend;

import com.coderank.entity.dto.QuestionTestCaseCreateDTO;
import com.coderank.entity.dto.QuestionTestCaseUpdateDTO;
import com.coderank.entity.vo.QuestionTestCaseVO;

import java.util.List;

/**
 * 后台题目测试用例管理业务。
 */
public interface QuestionTestCaseService {

    // 查询题目下的全部测试用例
    List<QuestionTestCaseVO> listByQuestionId(Long questionId);

    // 按 id 查询测试用例
    QuestionTestCaseVO getTestCaseById(Long id);

    // 新增测试用例
    Long createTestCase(QuestionTestCaseCreateDTO createDTO);

    // 修改测试用例
    void updateTestCase(QuestionTestCaseUpdateDTO updateDTO);

    // 删除测试用例
    void deleteTestCase(Long id);
}
