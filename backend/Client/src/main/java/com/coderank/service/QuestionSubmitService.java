package com.coderank.service;

import com.coderank.entity.dto.TaskDTO;
import com.coderank.entity.pojo.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coderank.entity.vo.QuestionSubmitVO;
import com.coderank.entity.vo.TaskVO;
import com.coderank.utils.ResponseResult;

/**
 * 评测任务相关接口
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 评测任务接口
     * @param taskDTO
     * @return
     */
    ResponseResult<QuestionSubmitVO> task(TaskDTO taskDTO);

    /**
     * 查询任务接口
     * @param id
     * @return
     */
    ResponseResult<TaskVO> findTask(Long id);
}
