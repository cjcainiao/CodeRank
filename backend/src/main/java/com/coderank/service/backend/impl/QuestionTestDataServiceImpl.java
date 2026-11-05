package com.coderank.service.backend.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.entity.dto.QuestionTestDataCreateDTO;
import com.coderank.entity.dto.QuestionTestDataUpdateDTO;
import com.coderank.entity.pojo.QuestionTestData;
import com.coderank.entity.vo.QuestionTestDataVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.QuestionMapper;
import com.coderank.mapper.backend.QuestionTestDataMapper;
import com.coderank.properties.FileUploadProperties;
import com.coderank.service.backend.QuestionTestDataService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

/**
 * 后台题目评测数据管理业务实现。
 */
@Service
public class QuestionTestDataServiceImpl implements QuestionTestDataService {

    /** 默认不展示 */
    private static final int VISIBLE_HIDDEN = 0;

    /** 输入文件扩展名 */
    private static final String EXTENSION_IN = "in";

    /** 输出文件扩展名 */
    private static final String EXTENSION_OUT = "out";

    private final QuestionTestDataMapper testDataMapper;

    private final QuestionMapper questionMapper;

    private final FileUploadProperties fileUploadProperties;

    public QuestionTestDataServiceImpl(QuestionTestDataMapper testDataMapper,
                                       QuestionMapper questionMapper,
                                       FileUploadProperties fileUploadProperties) {
        this.testDataMapper = testDataMapper;
        this.questionMapper = questionMapper;
        this.fileUploadProperties = fileUploadProperties;
    }

    // 查询题目下的全部评测数据，按测试点编号升序
    @Override
    public List<QuestionTestDataVO> listByQuestionId(Long questionId) {
        if (questionMapper.selectById(questionId) == null) {
            throw new BusinessException(404, "题目不存在");
        }
        List<QuestionTestData> testDataList = testDataMapper.selectList(
                new LambdaQueryWrapper<QuestionTestData>()
                        .eq(QuestionTestData::getQuestionId, questionId)
                        .orderByAsc(QuestionTestData::getCaseNo));
        return testDataList.stream().map(this::toVO).toList();
    }

    // 按 id 查询评测数据
    @Override
    public QuestionTestDataVO getTestDataById(Long id) {
        QuestionTestData testData = testDataMapper.selectById(id);
        if (testData == null) {
            throw new BusinessException(404, "评测数据不存在");
        }
        return toVO(testData);
    }

    // 新增评测数据，编号不传时取当前最大值加一
    @Override
    public Long createTestData(QuestionTestDataCreateDTO createDTO) {
        if (questionMapper.selectById(createDTO.getQuestionId()) == null) {
            throw new BusinessException(404, "题目不存在");
        }

        int caseNo = createDTO.getCaseNo() == null
                ? nextCaseNo(createDTO.getQuestionId()) : createDTO.getCaseNo();
        if (existsCaseNo(createDTO.getQuestionId(), caseNo, null)) {
            throw new BusinessException(409, "该测试点编号已存在");
        }

        QuestionTestData testData = new QuestionTestData();
        BeanUtils.copyProperties(createDTO, testData);
        testData.setCaseNo(caseNo);
        testData.setInputSize(createDTO.getInputSize() == null ? 0L : createDTO.getInputSize());
        testData.setOutputSize(createDTO.getOutputSize() == null ? 0L : createDTO.getOutputSize());
        testData.setVisible(createDTO.getVisible() == null ? VISIBLE_HIDDEN : createDTO.getVisible());

        testDataMapper.insert(testData);
        return testData.getId();
    }

    // 上传指定题目的评测数据，先落文件再写库，写库失败时清理已写入的文件
    @Override
    public Long uploadTestData(Long questionId, MultipartFile inputFile, MultipartFile outputFile,
                               Integer visible) {
        if (questionMapper.selectById(questionId) == null) {
            throw new BusinessException(404, "题目不存在");
        }
        validateFile(inputFile, EXTENSION_IN);
        validateFile(outputFile, EXTENSION_OUT);

        int caseNo = nextCaseNo(questionId);
        Path directory = resolveDirectory(questionId);
        Path inputPath = directory.resolve(caseNo + "." + EXTENSION_IN);
        Path outputPath = directory.resolve(caseNo + "." + EXTENSION_OUT);

        try {
            Files.createDirectories(directory);
            // 用流拷贝而非 transferTo，避免相对路径解析到临时目录
            try (var input = inputFile.getInputStream()) {
                Files.copy(input, inputPath, StandardCopyOption.REPLACE_EXISTING);
            }
            try (var output = outputFile.getInputStream()) {
                Files.copy(output, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
            throw new BusinessException(500, "评测数据保存失败");
        }

        QuestionTestData testData = new QuestionTestData();
        testData.setQuestionId(questionId);
        testData.setCaseNo(caseNo);
        testData.setInputSize(inputFile.getSize());
        testData.setOutputSize(outputFile.getSize());
        testData.setVisible(visible == null ? VISIBLE_HIDDEN : visible);

        try {
            testDataMapper.insert(testData);
        } catch (RuntimeException e) {
            // 记录没写成功时文件不该留在磁盘上
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
            throw e;
        }
        return testData.getId();
    }

    // 校验文件非空、扩展名在白名单内
    private void validateFile(MultipartFile file, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "评测数据文件不能为空");
        }
        String filename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null || !expectedExtension.equalsIgnoreCase(extension)) {
            throw new BusinessException(400, "文件须为 ." + expectedExtension + " 格式");
        }
        List<String> allowed = fileUploadProperties.getTestData().getAllowedExtensions();
        if (allowed != null && !allowed.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "不允许的文件类型");
        }
    }

    // 评测数据目录为「上传根目录/评测数据子目录/题目 id」
    private Path resolveDirectory(Long questionId) {
        FileUploadProperties.TestData config = fileUploadProperties.getTestData();
        return Paths.get(fileUploadProperties.getRoot(), config.getDir(), String.valueOf(questionId));
    }

    // 删除文件，文件不存在时忽略
    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 清理失败不覆盖原始异常，残留文件由运维处理
        }
    }

    // 修改评测数据
    @Override
    public void updateTestData(QuestionTestDataUpdateDTO updateDTO) {
        QuestionTestData existing = testDataMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "评测数据不存在");
        }
        if (updateDTO.getCaseNo() != null
                && existsCaseNo(existing.getQuestionId(), updateDTO.getCaseNo(), updateDTO.getId())) {
            throw new BusinessException(409, "该测试点编号已存在");
        }

        QuestionTestData testData = new QuestionTestData();
        BeanUtils.copyProperties(updateDTO, testData);
        testDataMapper.updateById(testData);
    }

    // 删除评测数据，记录与磁盘文件一并清理
    @Override
    public void deleteTestData(Long id) {
        QuestionTestData existing = testDataMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "评测数据不存在");
        }
        testDataMapper.deleteById(id);
        deleteCaseFiles(existing.getQuestionId(), existing.getCaseNo());
    }

    // 删除指定题目的某组评测文件
    private void deleteCaseFiles(Long questionId, Integer caseNo) {
        Path directory = resolveDirectory(questionId);
        deleteQuietly(directory.resolve(caseNo + "." + EXTENSION_IN));
        deleteQuietly(directory.resolve(caseNo + "." + EXTENSION_OUT));
    }

    // 取题目下现有最大编号加一，没有数据时从 1 开始
    private int nextCaseNo(Long questionId) {
        List<QuestionTestData> existing = testDataMapper.selectList(
                new LambdaQueryWrapper<QuestionTestData>()
                        .eq(QuestionTestData::getQuestionId, questionId)
                        .orderByDesc(QuestionTestData::getCaseNo)
                        .last("limit 1"));
        if (existing.isEmpty()) {
            return 1;
        }
        return existing.get(0).getCaseNo() + 1;
    }

    // 同一题目下测试点编号是否已被占用，excludeId 用于修改时排除自身
    private boolean existsCaseNo(Long questionId, Integer caseNo, Long excludeId) {
        LambdaQueryWrapper<QuestionTestData> wrapper = new LambdaQueryWrapper<QuestionTestData>()
                .eq(QuestionTestData::getQuestionId, questionId)
                .eq(QuestionTestData::getCaseNo, caseNo);
        if (excludeId != null) {
            wrapper.ne(QuestionTestData::getId, excludeId);
        }
        return testDataMapper.exists(wrapper);
    }

    // 实体转视图对象
    private QuestionTestDataVO toVO(QuestionTestData testData) {
        QuestionTestDataVO vo = new QuestionTestDataVO();
        BeanUtils.copyProperties(testData, vo);
        return vo;
    }
}
