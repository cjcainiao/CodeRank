package com.coderank.judge;

import com.coderank.entity.Question;
import com.coderank.entity.SubmissionTask;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 评测上下文
 */
@Data
public class JudgeContext {

    /** 当前任务id。 */
    private Long taskId;

    /** 评测任务。 */
    private SubmissionTask task;

    /** 当前评测题目。 */
    private Question question;

    /** 组装后的代码。 */
    private String assembledCode;

    /** go-judge 缓存的可执行文件id。 */
    private String executableFileId;

    /** 各测试点运行结果集合。 */
    private List<JsonNode> runResultList = new ArrayList<>();

    /** 评测数据集合。 */
    private List<JudgeData> judgeDataList = new ArrayList<>();

    /** 当前正在运行的测试点数据。 */
    private JudgeData currentJudgeData;

    /**
     * 单个测试点数据。
     */
    @Data
    public static class JudgeData {

        /** MinIO 存储桶名称。 */
        private String bucketName;

        /** 输入数据文件在 MinIO 中的存放路径。 */
        private String inputFilePath;

        /** 正确答案文件在 MinIO 中的存放路径。 */
        private String outputFilePath;
    }

}
