package com.coderank.service.problem;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.problem.SubmissionQueryDTO;
import com.coderank.entity.vo.problem.SubmissionVO;

/**
 * 后台提交记录业务。
 */
public interface SubmissionQueryService {

    // 分页查询提交记录
    PageResult<SubmissionVO> pageSubmissions(SubmissionQueryDTO queryDTO);
}
