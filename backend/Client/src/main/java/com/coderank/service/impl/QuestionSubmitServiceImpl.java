package com.coderank.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coderank.entity.bo.QuestionSubmitBO;
import com.coderank.entity.dto.TaskDTO;
import com.coderank.entity.pojo.Question;
import com.coderank.entity.pojo.QuestionSubmit;
import com.coderank.entity.vo.QuestionSubmitVO;
import com.coderank.entity.vo.TaskVO;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.QuestionMapper;
import com.coderank.service.QuestionSubmitService;
import com.coderank.mapper.QuestionSubmitMapper;
import com.coderank.utils.ResponseResult;
import com.coderank.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 评测任务相关实现
 */
@Service
@RequiredArgsConstructor
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit> implements QuestionSubmitService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final QuestionMapper questionMapper;

    /**
     * 评测任务
     *
     * @param taskDTO
     * @return
     */
    public ResponseResult<QuestionSubmitVO> task(TaskDTO taskDTO) {
        // 校验题目id是否存在
        boolean exists = questionMapper.exists(new LambdaQueryWrapper<Question>().eq(Question::getQuestionId, taskDTO.getQuestionId()));
        if (!exists) {
            throw new BusinessException(ResultCode.QUESTION_NOT_EXISTS.getCode(), ResultCode.QUESTION_NOT_EXISTS.getMsg());
        }
        // 创建任务（写入数据库）
        QuestionSubmit taskSubmit = BeanUtil.copyProperties(taskDTO, QuestionSubmit.class);
        Long UserId = UserContextUtils.getUser();
        taskSubmit.setUserId(UserId);
        boolean b = this.save(taskSubmit);
        if (!b) {
            throw new BusinessException(ResultCode.TASK_CREATEFAILURE.getCode(), ResultCode.TASK_CREATEFAILURE.getMsg());
        }
        //封装任务
        QuestionSubmitBO taskBO = BeanUtil.copyProperties(taskSubmit, QuestionSubmitBO.class);
        String taskStr = JSONUtil.toJsonStr(taskBO);
        // 发送任务到kafka
        kafkaTemplate.send("task", taskSubmit.getId().toString(), taskStr);
        //封装返回结果
        QuestionSubmitVO taskVO = new QuestionSubmitVO();
        taskVO.setId(taskSubmit.getId());
        return ResponseResult.success(taskVO);
    }


    /**
     * 查询任务
     * @param id
     * @return
     */
    public ResponseResult<TaskVO> findTask(Long id) {
        QuestionSubmit task = this.getById(id);

        TaskVO taskVO = BeanUtil.copyProperties(task, TaskVO.class);

        return ResponseResult.success(taskVO);
    }
}




