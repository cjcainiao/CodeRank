package com.coderank.codeBox;

import com.coderank.entity.QuestionSubmit;
import com.coderank.entity.Task;
import com.coderank.enums.LanguageEnum;
import com.coderank.mapper.QuestionSubmitMapper;
import com.coderank.utils.FileUtils;
import com.coderank.utils.TimeLogParserUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 代码沙箱公共执行流程
 */
@Component
public abstract class AbstractCodeBox implements CodeBox {

    private final QuestionSubmitMapper questionSubmitMapper;

    public String baseDir = "D:/task";

    public String dataDir = "D:/data";

    protected AbstractCodeBox(QuestionSubmitMapper questionSubmitMapper) {
        this.questionSubmitMapper = questionSubmitMapper;
    }

    /**
     * 通用执行流程
     *
     * @param task
     */
    public void execute(Task task) {
        // 校验危险命令
        check(task.getCode());

        // 创建任务
        String taskPath = create_task(task);

        // 编译代码
        compile(taskPath);

        // 运行代码
        run(taskPath, task);

        //判题
        judge(taskPath, dataDir + File.separator + task.getQuestionId(), task);
    }

    private void judge(String taskPath, String dataPath, Task task) {
        File resultDir = new File(taskPath, "result");
        if (!resultDir.exists() || !resultDir.isDirectory()) {
            return; // result目录不存在
        }

        File[] subDirs = resultDir.listFiles(File::isDirectory);
        if (subDirs == null) return;

        int index = 0;
        double time = 0;
        double memory = 0;
        boolean flgt = true;

        // 开始循环判题
        for (File subDir : subDirs) {
            // 1. 读取错误信息
            String errorMsg = FileUtils.readFile(subDir.getAbsolutePath(), "error.txt");

            // 持久化错误信息
            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                QuestionSubmit submit = new QuestionSubmit();
                submit.setId(task.getId());
                submit.setStatus(5);
                submit.setErrorMsg(errorMsg);
                questionSubmitMapper.updateById(submit);
                flgt = false;
                break;
            }

            // 2. 读取程序输出答案信息
            String successMsg = FileUtils.readFile(subDir.getAbsolutePath(), "success.txt");

            // 3. 读取正确答案信息
            index++;
            File answerFile = new File(new File(dataPath, String.valueOf(index)), "output.txt");
            String answer = FileUtils.readFile(answerFile.getParent(), answerFile.getName());

            // 4. 判题
            if (!answer.trim().equals(successMsg.trim())) {
                QuestionSubmit submit = new QuestionSubmit();
                submit.setId(task.getId());
                submit.setStatus(5);
                questionSubmitMapper.updateById(submit);
                flgt = false;
                break;
            }

            // 封装返回信息
            double[] doubles = TimeLogParserUtils.parseTimeLog(subDir.getAbsolutePath() + File.separator + "time_log.txt");
            time += doubles[0];
            memory += doubles[1];

        }

        // 答案正确
        if (flgt) {
            QuestionSubmit submit = new QuestionSubmit();
            submit.setId(task.getId());
            submit.setScore(100);
            submit.setMemoryUsed(memory);
            submit.setTimeUsed(time);
            submit.setStatus(1);
            questionSubmitMapper.updateById(submit);
        }

        // 删除任务目录
        // 6. 删除任务目录及所有文件
        deleteDirectory(new File(taskPath));
    }

    /**
     * 递归删除目录及其所有文件
     */
    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // 递归删除
                }
            }
        }
        dir.delete(); // 删除文件或空目录
    }


    private String create_task(Task task) {

        String taskDirPath = baseDir + File.separator + task.getId();
        File taskDir = new File(taskDirPath);

        if (!taskDir.exists()) {
            taskDir.mkdirs();
        }

        // 4. 从 submitTask 中获取用户提交的代码
        String userCode = task.getCode();

        String fileName = "Main." + LanguageEnum.getSuffixByLanguage(task.getLanguage());
        File codeFile = new File(taskDir, fileName);
        try (FileWriter writer = new FileWriter(codeFile)) {
            writer.write(userCode);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("写入代码文件失败", e);
        }
        //返回任务的绝对目录
        return taskDir.getAbsolutePath();
    }


    // todo 后面实现危险检测
    private void check(String code) {

    }


    protected abstract void run(String taskPath, Task task);

    protected abstract void compile(String taskPath);

}
