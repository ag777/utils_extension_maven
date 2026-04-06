package github.ag777.util.remote.ai.http.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.ag777.util.gson.GsonUtils;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.remote.ai.http.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI兼容接口协议适配器。
 * 
 * <p>实现了OpenAI API协议的请求体构建和响应解析，可用于所有兼容OpenAI协议的AI服务，
 * 包括但不限于：
 * <ul>
 * <li>OpenAI官方API</li>
 * <li>DeepSeek</li>
 * <li>硅基流动</li>
 * <li>其他兼容OpenAI格式的服务</li>
 * </ul>
 * 
 * <p>该适配器支持：
 * <ul>
 * <li>标准聊天完成请求格式</li>
 * <li>流式响应解析</li>
 * <li>工具调用支持</li>
 * <li>推理内容处理</li>
 * </ul>
 * 
 * @author ag777
 * @since 1.0
 */
public class OpenAiCompatibleProvider implements AiHttpProvider {

    /**
     * 构建OpenAI兼容的请求体。
     * 
     * @param request 统一请求对象
     * @param stream 是否为流式请求
     * @return 符合OpenAI API格式的请求体Map
     */
    @Override
    public Map<String, Object> buildRequestBody(AiHttpRequest request, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.model());
        body.put("messages", request.messages());
        body.put("stream", stream);
        if (request.tools() != null && !request.tools().isEmpty()) {
            body.put("tools", request.tools());
        }
        body.putAll(request.options());
        body.putAll(request.extraBody());
        return body;
    }

    /**
     * 解析OpenAI兼容的非流式响应。
     * 
     * @param json 响应JSON对象
     * @return 解析后的统一响应对象
     * @throws GsonSyntaxException JSON解析异常
     */
    @Override
    public AiHttpResponse parseResponse(JsonObject json) throws GsonSyntaxException {
        AiHttpResponse response = new AiHttpResponse().raw(json);
        JsonArray choices = JsonObjectUtils.getJsonArray(json, "choices");
        if (choices == null || choices.isEmpty()) {
            return response;
        }
        JsonObject choice = choices.get(0).getAsJsonObject();
        response.finishReason(JsonObjectUtils.getStr(choice, "finish_reason"));
        JsonObject message = JsonObjectUtils.getJsonObject(choice, "message");
        if (message == null) {
            return response;
        }
        response.content(JsonObjectUtils.getStr(message, "content"));
        String reasoning = JsonObjectUtils.getStr(message, "reasoning_content");
        if (reasoning == null) {
            reasoning = JsonObjectUtils.getStr(message, "reasoning");
        }
        response.reasoning(reasoning);
        response.toolCalls(parseToolCalls(JsonObjectUtils.getJsonArray(message, "tool_calls")));
        return response;
    }

    /**
     * 解析OpenAI兼容的流式响应分片。
     * 
     * @param json 流式分片JSON对象
     * @return 解析后的分片对象
     * @throws GsonSyntaxException JSON解析异常
     */
    @Override
    public AiHttpChunk parseStreamChunk(JsonObject json) throws GsonSyntaxException {
        AiHttpChunk chunk = new AiHttpChunk().raw(json);
        JsonArray choices = JsonObjectUtils.getJsonArray(json, "choices");
        if (choices == null || choices.isEmpty()) {
            return chunk;
        }
        JsonObject choice = choices.get(0).getAsJsonObject();
        chunk.finishReason(JsonObjectUtils.getStr(choice, "finish_reason"));
        JsonObject delta = JsonObjectUtils.getJsonObject(choice, "delta");
        if (delta == null) {
            return chunk;
        }
        chunk.content(JsonObjectUtils.getStr(delta, "content"));
        String reasoning = JsonObjectUtils.getStr(delta, "reasoning_content");
        if (reasoning == null) {
            reasoning = JsonObjectUtils.getStr(delta, "reasoning");
        }
        chunk.reasoning(reasoning);
        chunk.toolCalls(parseToolCallDeltas(JsonObjectUtils.getJsonArray(delta, "tool_calls")));
        return chunk;
    }

    /**
     * 解析工具调用数组。
     * 
     * @param toolCalls 工具调用JSON数组
     * @return 解析后的工具调用列表
     * @throws GsonSyntaxException JSON解析异常
     */
    private List<AiHttpToolCall> parseToolCalls(JsonArray toolCalls) throws GsonSyntaxException {
        List<AiHttpToolCall> items = new ArrayList<>();
        if (toolCalls == null) {
            return items;
        }
        for (JsonElement element : toolCalls) {
            JsonObject item = element.getAsJsonObject();
            JsonObject functionJson = JsonObjectUtils.getJsonObject(item, "function");
            String argumentsText = functionJson == null ? null : JsonObjectUtils.getStr(functionJson, "arguments");
            AiHttpToolFunction function = new AiHttpToolFunction()
                    .name(functionJson == null ? null : JsonObjectUtils.getStr(functionJson, "name"))
                    .argumentsText(argumentsText)
                    .arguments(parseArguments(argumentsText));
            AiHttpToolCall toolCall = new AiHttpToolCall()
                    .index(JsonObjectUtils.getInt(item, "index"))
                    .id(JsonObjectUtils.getStr(item, "id"))
                    .type(JsonObjectUtils.getStr(item, "type", "function"))
                    .function(function);
            items.add(toolCall);
        }
        return items;
    }

    /**
     * 解析流式工具调用增量数组。
     * 
     * @param toolCalls 工具调用增量JSON数组
     * @return 解析后的工具调用增量列表
     * @throws GsonSyntaxException JSON解析异常
     */
    private List<AiHttpToolCallDelta> parseToolCallDeltas(JsonArray toolCalls) throws GsonSyntaxException {
        List<AiHttpToolCallDelta> items = new ArrayList<>();
        if (toolCalls == null) {
            return items;
        }
        for (JsonElement element : toolCalls) {
            JsonObject item = element.getAsJsonObject();
            JsonObject functionJson = JsonObjectUtils.getJsonObject(item, "function");
            AiHttpToolFunctionDelta function = null;
            if (functionJson != null) {
                function = new AiHttpToolFunctionDelta()
                        .name(JsonObjectUtils.getStr(functionJson, "name"))
                        .argumentsDelta(JsonObjectUtils.getStr(functionJson, "arguments"));
            }
            AiHttpToolCallDelta delta = new AiHttpToolCallDelta()
                    .index(JsonObjectUtils.getInt(item, "index"))
                    .id(JsonObjectUtils.getStr(item, "id"))
                    .type(JsonObjectUtils.getStr(item, "type"))
                    .function(function);
            items.add(delta);
        }
        return items;
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
}
