package com.coderank.codebox;

import com.coderank.entity.*;
import com.coderank.enums.LanguageEnum;
import com.coderank.mapper.QuestionMapper;
import com.coderank.utils.FileUtils;
import com.coderank.utils.TimeLogParserUtils;
import com.coderank.mapper.QuestionSubmitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 代码沙箱公共执行流程
 */
@Component
@RequiredArgsConstructor
public abstract class AbstractCodeBox implements CodeBox {

    public final QuestionMapper questionMapper;
    public final QuestionSubmitMapper questionSubmitMapper;


    @Value("${workspace.baseDir}")
    public String baseDir = "D:/task";

    @Value("${workspace.dataDir}")
    public String dataDir = "D:/data";

    /**
     * 测试任务执行流程
     *
     * @param task
     */
    @Override
    public void test(Task task) {
        TaskContext taskContext = null;
        try {
            taskContext = new TaskContext();
            //构建任务上下文
            create_context(taskContext, task);
            //1、检测危险代码
            check(taskContext);
            //2、组装代码
            assembly(taskContext);
            //3、创建任务
            create_task(taskContext);
            //4、编译代码
            compile(taskContext);
            //5、运行代码
            runTest(taskContext);
            //6、获取输出结果
            result(taskContext);
        } catch (Exception e) {
            System.out.println("出现异常");
        } finally {
            //7、清理文件
            deleteDirectory(new File(taskContext.getTaskPath()));
        }
    }

    /**
     * 评测任务执行流程
     *
     * @param task
     */
    public void execute(Task task) {
        TaskContext taskContext = null;
        try {
            taskContext = new TaskContext();
            //构建任务上下文
            create_context(taskContext, task);
            //1、检测危险代码
            check(taskContext);
            //2、组装代码
            assembly(taskContext);
            //3、创建任务
            create_task(taskContext);
            //4、编译代码
            compile(taskContext);
            //5、运行代码
            run(taskContext);
            //6、判题
            judge(taskContext);

        } catch (Exception e) {
            System.out.println("出现异常");
        } finally {
            //7、清理文件
            deleteDirectory(new File(taskContext.getTaskPath()));
        }
    }

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

    private void result(TaskContext taskContext){
        File resultDir = new File(taskContext.getTaskPath(), "result");
        if (!resultDir.exists() || !resultDir.isDirectory()) {
            return; // result目录不存在
        }
        //读取错误输出
        String errorMsg = FileUtils.readFile(resultDir.getAbsolutePath(), "error.txt");
        System.out.println("错误输出为："+ errorMsg);

        //读出正确输出结果
        String successMsg = FileUtils.readFile(resultDir.getAbsolutePath(), "success.txt");
        System.out.println("输出为："+ successMsg);
    }

    private void judge(TaskContext taskContext) {
        File resultDir = new File(taskContext.getTaskPath(), "result");
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
                submit.setId(taskContext.getTask().getId());
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
            File answerFile = new File(new File(dataDir + File.separator + taskContext.getTask().getQuestionId(), String.valueOf(index)), "output.txt");
            String answer = FileUtils.readFile(answerFile.getParent(), answerFile.getName());

            // 4. 判题
            if (!answer.trim().equals(successMsg.trim())) {
                QuestionSubmit submit = new QuestionSubmit();
                submit.setId(taskContext.getTask().getId());
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
            submit.setId(taskContext.getTask().getId());
            submit.setScore(100);
            submit.setMemoryUsed(memory);
            submit.setTimeUsed(time);
            submit.setStatus(1);
            questionSubmitMapper.updateById(submit);
        }
    }

    private void create_task(TaskContext taskContext) {
        String taskDirPath = baseDir + File.separator + taskContext.getTask().getId();
        File taskDir = new File(taskDirPath);

        if (!taskDir.exists()) {
            taskDir.mkdirs();
        }

        // 4. 从 submitTask 中获取用户提交的代码
        String userCode = taskContext.getTask().getCode();

        String fileName = "Main." + LanguageEnum.getSuffixByLanguage(taskContext.getTask().getLanguage());

        //写入代码文件
        File codeFile = new File(taskDir, fileName);
        try (FileWriter writer = new FileWriter(codeFile)) {
            writer.write(userCode);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("写入代码文件失败", e);
        }
        //写入数据文件
        if (taskContext.getTask().getInput() == null || !taskContext.getTask().getInput().isEmpty()) {
            String inputData = "input.txt";
            File inputFile = new File(taskDir, inputData);
            try (FileWriter writer = new FileWriter(inputFile)) {
                writer.write(taskContext.getTask().getInput());
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("写入代码文件失败", e);
            }
        }
        //返回任务的绝对目录
        taskContext.setTaskPath(taskDir.getAbsolutePath());
    }

    private void assembly(TaskContext taskContext) {
    }

    private void check(TaskContext taskContext) {
    }

    private void create_context(TaskContext taskContext, Task task) {
        Question question = questionMapper.selectById(task.getQuestionId());
        taskContext.setTask(task);
        taskContext.setQuestion(question);
    }

    protected abstract void compile(TaskContext taskContext);

    protected abstract void run(TaskContext taskContext);

    protected abstract void runTest(TaskContext taskContext);
}
