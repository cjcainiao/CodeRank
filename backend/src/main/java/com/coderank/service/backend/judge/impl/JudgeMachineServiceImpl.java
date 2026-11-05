package com.coderank.service.backend.judge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.judge.JudgeMachineCreateDTO;
import com.coderank.entity.dto.judge.JudgeMachineQueryDTO;
import com.coderank.entity.dto.judge.JudgeMachineUpdateDTO;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.vo.judge.JudgeMachineCheckVO;
import com.coderank.entity.vo.judge.JudgeMachineVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.judge.JudgeMachineMapper;
import com.coderank.service.backend.judge.JudgeMachineService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台评测机管理业务实现。
 */
@Service
public class JudgeMachineServiceImpl implements JudgeMachineService {

    private static final Logger log = LoggerFactory.getLogger(JudgeMachineServiceImpl.class);

    /** 分页每页最大条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 排序方向：升序 */
    private static final String SORT_ASC = "asc";

    /** 排序字段：权重 */
    private static final String SORT_FIELD_WEIGHT = "weight";

    /** 排序字段：创建时间 */
    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    /** 排序字段：更新时间 */
    private static final String SORT_FIELD_UPDATE_TIME = "updateTime";

    /** 默认状态：启用 */
    private static final int DEFAULT_STATUS = 1;

    /** 默认权重 */
    private static final int DEFAULT_WEIGHT = 1;

    /** 默认最大并发数 */
    private static final int DEFAULT_MAX_PARALLEL = 4;

    /** 默认 CPU 时间上限，纳秒 */
    private static final long DEFAULT_CPU_LIMIT = 5_000_000_000L;

    /** 默认墙钟时间上限，纳秒 */
    private static final long DEFAULT_CLOCK_LIMIT = 10_000_000_000L;

    /** 默认内存上限，字节 */
    private static final long DEFAULT_MEMORY_LIMIT = 268_435_456L;

    /** 默认进程数上限 */
    private static final int DEFAULT_PROC_LIMIT = 64;

    /** 默认输出上限，字节 */
    private static final long DEFAULT_OUTPUT_LIMIT = 1_048_576L;

    /** 探测结果：失败 */
    private static final int CHECK_FAILED = 0;

    /** 探测结果：成功 */
    private static final int CHECK_OK = 1;

    private final JudgeMachineMapper judgeMachineMapper;

    private final CloseableHttpClient judgeHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JudgeMachineServiceImpl(JudgeMachineMapper judgeMachineMapper,
                                   @Qualifier("judgeHttpClient") CloseableHttpClient judgeHttpClient) {
        this.judgeMachineMapper = judgeMachineMapper;
        this.judgeHttpClient = judgeHttpClient;
    }

    // 分页查询评测机
    @Override
    public PageResult<JudgeMachineVO> pageMachines(JudgeMachineQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10L : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);

        // 仅接受 asc，其余按降序处理，避免拼接非法排序值
        boolean ascending = SORT_ASC.equalsIgnoreCase(queryDTO.getSortOrder());

        LambdaQueryWrapper<JudgeMachine> wrapper = new LambdaQueryWrapper<JudgeMachine>()
                .like(StringUtils.hasText(queryDTO.getName()), JudgeMachine::getName, queryDTO.getName())
                .eq(queryDTO.getStatus() != null, JudgeMachine::getStatus, queryDTO.getStatus());

        applySort(wrapper, queryDTO.getSortField(), ascending);

        Page<JudgeMachine> page = judgeMachineMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page, this::toVO);
    }

    // 按白名单字段排序，未指定字段时按权重降序
    private void applySort(LambdaQueryWrapper<JudgeMachine> wrapper, String sortField, boolean ascending) {
        switch (sortField == null ? "" : sortField) {
            case SORT_FIELD_WEIGHT -> wrapper.orderBy(true, ascending, JudgeMachine::getWeight);
            case SORT_FIELD_CREATE_TIME -> wrapper.orderBy(true, ascending, JudgeMachine::getCreateTime);
            case SORT_FIELD_UPDATE_TIME -> wrapper.orderBy(true, ascending, JudgeMachine::getUpdateTime);
            default -> wrapper.orderByDesc(JudgeMachine::getWeight).orderByAsc(JudgeMachine::getId);
        }
    }

    // 按 id 查询评测机
    @Override
    public JudgeMachineVO getMachineById(Long id) {
        JudgeMachine machine = judgeMachineMapper.selectById(id);
        if (machine == null) {
            throw new BusinessException(404, "评测机不存在");
        }
        return toVO(machine);
    }

    // 新增评测机
    @Override
    public Long createMachine(JudgeMachineCreateDTO createDTO) {
        if (existsName(createDTO.getName())) {
            throw new BusinessException(409, "机器名称已存在");
        }

        JudgeMachine machine = new JudgeMachine();
        BeanUtils.copyProperties(createDTO, machine);
        machine.setStatus(createDTO.getStatus() == null ? DEFAULT_STATUS : createDTO.getStatus());
        machine.setWeight(createDTO.getWeight() == null ? DEFAULT_WEIGHT : createDTO.getWeight());
        machine.setMaxParallel(createDTO.getMaxParallel() == null
                ? DEFAULT_MAX_PARALLEL : createDTO.getMaxParallel());
        machine.setCpuLimit(createDTO.getCpuLimit() == null ? DEFAULT_CPU_LIMIT : createDTO.getCpuLimit());
        machine.setClockLimit(createDTO.getClockLimit() == null
                ? DEFAULT_CLOCK_LIMIT : createDTO.getClockLimit());
        machine.setMemoryLimit(createDTO.getMemoryLimit() == null
                ? DEFAULT_MEMORY_LIMIT : createDTO.getMemoryLimit());
        machine.setProcLimit(createDTO.getProcLimit() == null ? DEFAULT_PROC_LIMIT : createDTO.getProcLimit());
        machine.setOutputLimit(createDTO.getOutputLimit() == null
                ? DEFAULT_OUTPUT_LIMIT : createDTO.getOutputLimit());

        judgeMachineMapper.insert(machine);
        return machine.getId();
    }

    // 修改评测机
    @Override
    public void updateMachine(JudgeMachineUpdateDTO updateDTO) {
        JudgeMachine existing = judgeMachineMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "评测机不存在");
        }
        if (StringUtils.hasText(updateDTO.getName()) && existsNameExceptId(updateDTO.getName(), updateDTO.getId())) {
            throw new BusinessException(409, "机器名称已存在");
        }

        JudgeMachine machine = new JudgeMachine();
        BeanUtils.copyProperties(updateDTO, machine);
        judgeMachineMapper.updateById(machine);
    }

    // 删除评测机
    @Override
    public void deleteMachine(Long id) {
        if (judgeMachineMapper.selectById(id) == null) {
            throw new BusinessException(404, "评测机不存在");
        }
        judgeMachineMapper.deleteById(id);
    }

    // 探测评测机是否可用，请求版本接口确认服务在线
    @Override
    public JudgeMachineCheckVO checkMachine(Long id) {
        JudgeMachine machine = judgeMachineMapper.selectById(id);
        if (machine == null) {
            throw new BusinessException(404, "评测机不存在");
        }
        return probe(machine);
    }

    // 探测全部评测机，逐台串行执行避免同时压垮多台机器
    @Override
    public List<JudgeMachineCheckVO> checkAllMachines() {
        List<JudgeMachine> machines = judgeMachineMapper.selectList(
                new LambdaQueryWrapper<JudgeMachine>().orderByAsc(JudgeMachine::getId));
        return machines.stream().map(this::probe).toList();
    }

    // 单台探测并回写结果
    private JudgeMachineCheckVO probe(JudgeMachine machine) {
        JudgeMachineCheckVO vo = new JudgeMachineCheckVO();
        long start = System.currentTimeMillis();
        try {
            HttpGet request = new HttpGet(buildVersionUrl(machine.getEndpoint()));
            // 回调内只读取响应，状态码判断放到外层，避免异常被客户端包装后丢失原始信息
            HttpResult result = judgeHttpClient.execute(request, response -> new HttpResult(
                    response.getCode(),
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));

            if (result.status() != 200) {
                vo.setOk(false);
                vo.setMessage("服务返回状态码 " + result.status());
            } else {
                JsonNode json = objectMapper.readTree(result.body());
                vo.setOk(true);
                vo.setVersion(textOrNull(json, "buildVersion"));
                vo.setOs(textOrNull(json, "os"));
                vo.setPlatform(textOrNull(json, "platform"));
            }
        } catch (Exception e) {
            // 连接失败属于预期内的探测结果，只记简要信息，不打印堆栈
            log.warn("评测机探测失败，id={}，endpoint={}，原因={}",
                    machine.getId(), machine.getEndpoint(), describeError(e));
            vo.setOk(false);
            vo.setMessage(describeError(e));
        }

        vo.setElapsed(System.currentTimeMillis() - start);
        // 探测结果落库，便于列表直接展示在线状态
        JudgeMachine update = new JudgeMachine();
        update.setId(machine.getId());
        update.setLastCheckTime(LocalDateTime.now());
        update.setLastCheckOk(Boolean.TRUE.equals(vo.getOk()) ? CHECK_OK : CHECK_FAILED);
        judgeMachineMapper.updateById(update);

        return vo;
    }

    // 拼接版本接口地址，容忍地址末尾多写斜杠
    private String buildVersionUrl(String endpoint) {
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/version";
    }

    // 把网络异常翻译成可直接展示的中文原因
    private String describeError(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String type = cause.getClass().getSimpleName();
        if (type.contains("ConnectTimeout") || cause instanceof ConnectException) {
            return "连接超时，请确认评测机已启动且端口对外开放";
        }
        if (type.contains("SocketTimeout") || cause instanceof SocketTimeoutException) {
            return "请求超时，评测机响应过慢";
        }
        if (cause instanceof UnknownHostException) {
            return "无法解析服务地址";
        }
        return cause.getMessage() == null ? type : cause.getMessage();
    }

    // 读取文本字段，缺失时返回空
    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    // 机器名称是否已存在，比较时忽略大小写与首尾空格
    private boolean existsName(String name) {
        return judgeMachineMapper.exists(new LambdaQueryWrapper<JudgeMachine>()
                .apply("lower(trim(name)) = lower(trim({0}))", name));
    }

    // 机器名称是否被除指定评测机外的其他记录使用
    private boolean existsNameExceptId(String name, Long id) {
        return judgeMachineMapper.exists(new LambdaQueryWrapper<JudgeMachine>()
                .apply("lower(trim(name)) = lower(trim({0}))", name)
                .ne(JudgeMachine::getId, id));
    }

    // 实体转视图对象，令牌只暴露是否已配置
    private JudgeMachineVO toVO(JudgeMachine machine) {
        JudgeMachineVO vo = new JudgeMachineVO();
        BeanUtils.copyProperties(machine, vo);
        vo.setHasToken(StringUtils.hasText(machine.getAuthToken()));
        return vo;
    }

    /**
     * 状态码与响应体。
     */
    private record HttpResult(int status, String body) {
    }
}
