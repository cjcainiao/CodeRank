package com.coderank.service.backend;

import com.coderank.entity.dto.QuestionTestDataCreateDTO;
import com.coderank.entity.dto.QuestionTestDataUpdateDTO;
import com.coderank.entity.vo.QuestionTestDataVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 后台题目评测数据管理业务。
 */
public interface QuestionTestDataService {

    // 查询题目下的全部评测数据
    List<QuestionTestDataVO> listByQuestionId(Long questionId);

    // 按 id 查询评测数据
    QuestionTestDataVO getTestDataById(Long id);

    // 新增评测数据
    Long createTestData(QuestionTestDataCreateDTO createDTO);

    // 上传指定题目的评测数据
    Long uploadTestData(Long questionId, MultipartFile inputFile, MultipartFile outputFile, Integer visible);

    // 修改评测数据
    void updateTestData(QuestionTestDataUpdateDTO updateDTO);

    // 删除评测数据
    void deleteTestData(Long id);
}
