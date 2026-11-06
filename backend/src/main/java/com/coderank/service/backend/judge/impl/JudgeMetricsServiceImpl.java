package com.coderank.service.backend.judge.impl;

import com.coderank.common.enums.ResultCodeEnum;
import com.coderank.entity.pojo.JudgeMachine;
import com.coderank.entity.vo.judge.JudgeMetricVO;
import com.coderank.entity.vo.judge.JudgeMetricsVO;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.backend.judge.JudgeMachineMapper;
import com.coderank.service.backend.judge.JudgeMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评测机指标监控业务实现，解析 Prometheus 文本格式。
 */
@Slf4j
@Service
public class JudgeMetricsServiceImpl implements JudgeMetricsService {

    /** 指标前缀，只保留评测机自身指标 */
    private static final String METRIC_PREFIX = "go_judge";

    /** 抓取失败时的提示 */
    private static final String MESSAGE_METRICS_DISABLED = "未开启监控，启动时需加 -enable-metrics 参数";

    private final JudgeMachineMapper judgeMachineMapper;

    private final CloseableHttpClient judgeHttpClient;

    public JudgeMetricsServiceImpl(JudgeMachineMapper judgeMachineMapper,
                                   @Qualifier("judgeHttpClient") CloseableHttpClient judgeHttpClient) {
        this.judgeMachineMapper = judgeMachineMapper;
        this.judgeHttpClient = judgeHttpClient;
    }

    // 抓取指定评测机的 Prometheus 指标
    @Override
    public JudgeMetricsVO getMetrics(Long machineId) {
        JudgeMachine machine = judgeMachineMapper.selectById(machineId);
        if (machine == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "评测机不存在");
        }

        JudgeMetricsVO.JudgeMetricsVOBuilder builder = JudgeMetricsVO.builder()
                .machineId(machine.getId())
                .machineName(machine.getName())
                .endpoint(machine.getMetricsEndpoint());

        // 指标端口与主服务不同，未登记地址说明没有开启监控
        if (!StringUtils.hasText(machine.getMetricsEndpoint())) {
            return builder.available(false)
                    .message(MESSAGE_METRICS_DISABLED)
                    .elapsed(0L)
                    .build();
        }

        long start = System.currentTimeMillis();
        try {
            HttpGet request = new HttpGet(buildMetricsUrl(machine.getMetricsEndpoint()));
            if (StringUtils.hasText(machine.getAuthToken())) {
                request.setHeader("Authorization", "Bearer " + machine.getAuthToken());
            }
            HttpResult result = judgeHttpClient.execute(request, response -> new HttpResult(
                    response.getCode(),
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));

            if (result.status() != 200) {
                builder.available(false).message("指标接口返回状态码 " + result.status());
            } else {
                builder.available(true).metrics(parsePrometheus(result.body()));
            }
        } catch (Exception e) {
            log.warn("评测机指标抓取失败，id={}，endpoint={}，原因={}",
                    machine.getId(), machine.getMetricsEndpoint(), describeError(e));
            builder.available(false).message(describeError(e));
        }

        return builder.elapsed(System.currentTimeMillis() - start).build();
    }

    // 解析 Prometheus 文本格式，只保留评测机自身指标
    private List<JudgeMetricVO> parsePrometheus(String body) {
        Map<String, String> helps = new LinkedHashMap<>();
        List<JudgeMetricVO> metrics = new ArrayList<>();

        for (String rawLine : body.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("# HELP ")) {
                String rest = line.substring("# HELP ".length()).trim();
                int space = rest.indexOf(' ');
                if (space > 0) {
                    helps.put(rest.substring(0, space), rest.substring(space + 1).trim());
                }
                continue;
            }
            // TYPE 等其余注释行跳过
            if (line.startsWith("#")) {
                continue;
            }

            JudgeMetricVO metric = parseSample(line);
            if (metric == null || !metric.getName().startsWith(METRIC_PREFIX)) {
                continue;
            }
            metric.setHelp(helps.get(metric.getName()));
            metrics.add(metric);
        }
        return metrics;
    }

    // 解析单行样本：指标名{标签} 数值
    private JudgeMetricVO parseSample(String line) {
        int braceStart = line.indexOf('{');
        int braceEnd = line.indexOf('}');
        boolean hasLabels = braceStart > 0 && braceEnd > braceStart;

        String name = hasLabels ? line.substring(0, braceStart).trim() : null;
        int valueStart;
        Map<String, String> labels = new LinkedHashMap<>();

        if (hasLabels) {
            String labelText = line.substring(braceStart + 1, braceEnd);
            parseLabels(labelText, labels);
            valueStart = braceEnd + 1;
        } else {
            // 无标签时，指标名后面第一个空白之前是名字，之后是数值
            int space = line.indexOf(' ');
            if (space < 0) {
                return null;
            }
            name = line.substring(0, space).trim();
            valueStart = space;
        }

        String valueText = line.substring(valueStart).trim();
        // 数值后面可能跟着时间戳，只取第一段
        int space = valueText.indexOf(' ');
        if (space > 0) {
            valueText = valueText.substring(0, space);
        }

        return JudgeMetricVO.builder()
                .name(name)
                .labels(labels)
                .value(parseValue(valueText))
                .build();
    }

    // 解析标签串，支持双引号内的逗号与转义
    private void parseLabels(String text, Map<String, String> labels) {
        int index = 0;
        int length = text.length();
        while (index < length) {
            int eq = text.indexOf('=', index);
            if (eq < 0) {
                return;
            }
            String key = text.substring(index, eq).trim();
            int quoteStart = text.indexOf('"', eq);
            if (quoteStart < 0) {
                return;
            }

            StringBuilder value = new StringBuilder();
            int cursor = quoteStart + 1;
            while (cursor < length) {
                char ch = text.charAt(cursor);
                if (ch == '\\' && cursor + 1 < length) {
                    value.append(text.charAt(cursor + 1));
                    cursor += 2;
                    continue;
                }
                if (ch == '"') {
                    break;
                }
                value.append(ch);
                cursor++;
            }
            if (StringUtils.hasText(key)) {
                labels.put(key, value.toString());
            }
            index = cursor + 1;
            // 跳过标签之间的逗号
            while (index < length && (text.charAt(index) == ',' || text.charAt(index) == ' ')) {
                index++;
            }
        }
    }

    // 解析数值，NaN 与无穷大按空处理
    private Double parseValue(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 拼接指标地址，容忍地址末尾多写斜杠
    private String buildMetricsUrl(String endpoint) {
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/metrics";
    }

    // 把网络异常翻译成可直接展示的中文原因
    private String describeError(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String type = cause.getClass().getSimpleName();
        if (type.contains("ConnectTimeout") || cause instanceof ConnectException) {
            return "连接失败，请确认评测机已启动且端口对外开放";
        }
        if (type.contains("SocketTimeout") || cause instanceof SocketTimeoutException) {
            return "请求超时，评测机响应过慢";
        }
        if (cause instanceof UnknownHostException) {
            return "无法解析服务地址";
        }
        return cause.getMessage() == null ? type : cause.getMessage();
    }

    /**
     * 状态码与响应体。
     */
    private record HttpResult(int status, String body) {
    }
}
