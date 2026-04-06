package github.ag777.util.remote.ai.http.support;

import com.google.gson.JsonObject;
import github.ag777.util.gson.GsonUtils;
import github.ag777.util.remote.ai.http.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流式响应累积器。
 * 
 * <p>用于在流式响应过程中累积和组装最终响应内容。该类负责：
 * <ul>
 * <li>累积文本内容和推理内容</li>
 * <li>组装工具调用信息</li>
 * <li>维护完成状态和原始响应数据</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpResponseAccumulator accumulator = new AiHttpResponseAccumulator();
 * for (AiHttpChunk chunk : chunks) {
 *     accumulator.append(chunk);
 * }
 * AiHttpResponse response = accumulator.toResponse();
 * }</pre>
 * 
 * @author ag777
 * @since 1.0
 */
@Data
public class AiHttpResponseAccumulator {
    private final StringBuilder content = new StringBuilder();
    private final StringBuilder reasoning = new StringBuilder();
    private final Map<Integer, ToolBuffer> toolBuffers = new LinkedHashMap<>();
    private String finishReason;
    private JsonObject lastRaw;

    /**
     * 追加流式响应分片到累积器。
     * 
     * @param chunk 流式响应分片，如果为null则忽略
     */
    public void append(AiHttpChunk chunk) {
        if (chunk == null) {
            return;
        }
        if (chunk.hasContent()) {
            content.append(chunk.content());
        }
        if (chunk.hasReasoning()) {
            reasoning.append(chunk.reasoning());
        }
        if (chunk.hasToolCalls()) {
            for (AiHttpToolCallDelta delta : chunk.toolCalls()) {
                int index = delta.index() == null ? 0 : delta.index();
                ToolBuffer buffer = toolBuffers.computeIfAbsent(index, k -> new ToolBuffer(index));
                if (delta.id() != null) {
                    buffer.id = delta.id();
                }
                if (delta.type() != null) {
                    buffer.type = delta.type();
                }
                if (delta.hasFunction()) {
                    AiHttpToolFunctionDelta function = delta.function();
                    if (function.name() != null) {
                        buffer.functionName = function.name();
                    }
                    if (function.argumentsDelta() != null) {
                        buffer.argumentsText.append(function.argumentsDelta());
                    }
                }
                if (delta.extra() != null && !delta.extra().isEmpty()) {
                    buffer.extra.putAll(delta.extra());
                }
            }
        }
        if (chunk.finishReason() != null) {
            finishReason = chunk.finishReason();
        }
        if (chunk.raw() != null) {
            lastRaw = chunk.raw();
        }
    }

    /**
     * 将累积的数据转换为最终的响应对象。
     * 
     * @return 组装完成的响应对象，包含所有累积的内容、工具调用等信息
     */
    public AiHttpResponse toResponse() {
        List<AiHttpToolCall> toolCalls = new ArrayList<>(toolBuffers.size());
        for (ToolBuffer buffer : toolBuffers.values()) {
            String argumentsText = buffer.argumentsText.toString();
            Map<String, Object> arguments = parseArguments(argumentsText);
            toolCalls.add(new AiHttpToolCall()
                    .index(buffer.index)
                    .id(buffer.id)
                    .type(buffer.type)
                    .function(new AiHttpToolFunction()
                            .name(buffer.functionName)
                            .argumentsText(argumentsText)
                            .arguments(arguments)));
            if (!buffer.extra.isEmpty()) {
                toolCalls.get(toolCalls.size() - 1).extra().putAll(buffer.extra);
            }
        }
        return new AiHttpResponse()
                .content(content.toString())
                .reasoning(reasoning.toString())
                .toolCalls(toolCalls)
                .finishReason(finishReason)
                .raw(lastRaw);
    }

    /**
     * 解析工具调用参数文本为Map对象。
     * 
     * @param argumentsText 参数文本，JSON格式
     * @return 解析后的参数Map，解析失败时返回空Map
     */
    private Map<String, Object> parseArguments(String argumentsText) {
        if (argumentsText == null || argumentsText.isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            return GsonUtils.get().toMap(argumentsText);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    /**
     * 工具调用缓冲区，用于在流式响应中临时存储工具调用信息。
     */
    private static class ToolBuffer {
        private final Integer index;
        private String id;
        private String type = "function";
        private String functionName;
        private final StringBuilder argumentsText = new StringBuilder();
        private final Map<String, Object> extra = new LinkedHashMap<>();

        /**
         * 创建工具调用缓冲区。
         * 
         * @param index 工具调用索引
         */
        private ToolBuffer(Integer index) {
            this.index = index;
        }
    }
}
