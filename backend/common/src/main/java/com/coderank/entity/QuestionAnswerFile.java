package com.coderank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 题目答案文件存放表
 * </p>
 *
 * @author cainiao
 */
@Data
@Builder
@TableName("question_answer_file")
public class QuestionAnswerFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 答案文件id
     */
    @TableId(value = "answer_file_id", type = IdType.AUTO)
    private Long answerFileId;

    /**
     * 所属题目id
     */
    private Long questionId;

    /**
     * 测试点序号
     */
    private Integer caseNo;

    /**
     * MinIO存储桶名称
     */
    private String bucketName;

    /**
     * 输入数据文件存放路径
     */
    private String inputFilePath;

    /**
     * 正确答案文件存放路径
     */
    private String outputFilePath;

    /**
     * 输入文件内容哈希
     */
    private String inputFileHash;

    /**
     * 正确答案文件内容哈希
     */
    private String outputFileHash;

    /**
     * 文件状态：0停用，1启用
     */
    private Byte status;

    /**
     * 上传人用户id
     */
    private Long uploaderId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
