package github.ag777.util.remote.ai.http.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.ag777.util.gson.GsonUtils;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.http.HttpHelper;
import github.ag777.util.http.model.MyCall;
import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.remote.ai.http.config.AiHttpClientConfig;
import github.ag777.util.remote.ai.http.exception.AiHttpException;
import github.ag777.util.remote.ai.http.model.AiHttpChunk;
import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;
import github.ag777.util.remote.ai.http.model.AiHttpToolCallDelta;
import github.ag777.util.remote.ai.http.provider.AiHttpProvider;
import github.ag777.util.remote.ai.http.provider.OpenAiCompatibleProvider;
import github.ag777.util.remote.ai.http.stream.AiHttpFuture;
import github.ag777.util.remote.ai.http.stream.AiHttpStreamHandler;
import github.ag777.util.remote.ai.http.support.AiHttpResponseAccumulator;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

/**
 * 通用AI HTTP客户端。
 * 
 * <p>提供了与各种AI服务进行HTTP交互的统一接口，支持：
 * <ul>
 * <li>同步和异步聊天完成</li>
 * <li>流式响应处理</li>
 * <li>工具调用支持</li>
 * <li>可取消的异步请求</li>
 * <li>多协议适配</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * // 同步调用
 * AiHttpResponse response = client.chat(request);
 * 
 * // 异步调用
 * AiHttpFuture future = client.chatAsync(request);
 * AiHttpResponse result = future.get();
 * 
 * // 流式调用
 * client.chatStream(request, new AiHttpStreamHandler() {
 *     @Override
 *     public void onContent(String delta, AiHttpChunk chunk) {
 *         System.out.println("收到: " + delta);
 *     }
 * });
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
public class AiHttpClient {
    private final AiHttpClientConfig config;
    private final AiHttpProvider provider;

    /**
     * 创建AI HTTP客户端。
     * 
     * @param config 客户端配置
     * @param provider 协议适配器
     */
    public AiHttpClient(AiHttpClientConfig config, AiHttpProvider provider) {
        this.config = config;
        this.provider = provider;
    }

    /**
     * 创建OpenAI兼容的AI HTTP客户端。
     * 
     * @param config 客户端配置
     * @return 配置了OpenAI兼容协议的客户端
     */
    public static AiHttpClient openAiCompatible(AiHttpClientConfig config) {
        return new AiHttpClient(config, new OpenAiCompatibleProvider());
    }

    /**
     * 同步聊天完成。
     * 
     * @param request 聊天请求
     * @return 聊天响应
     * @throws AiHttpException 调用失败时抛出
     */
    public AiHttpResponse chat(AiHttpRequest request) {
        return execute(request, false, null);
    }

    /**
     * 异步聊天完成（无流式处理）。
     * 
     * @param request 聊天请求
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(AiHttpRequest request) {
        return chatAsync(request, null);
    }

    /**
     * 异步聊天完成（可选择流式处理）。
     * 
     * @param request 聊天请求
     * @param streamHandler 流式处理器，如果为null则不进行流式处理
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(AiHttpRequest request, AiHttpStreamHandler streamHandler) {
        AiHttpFuture future = new AiHttpFuture();
        Executor executor = config.executor();
        executor.execute(() -> {
            try {
                AiHttpResponse response = execute(request, streamHandler != null, streamHandler, future);
                if (!future.isDone()) {
                    future.complete(response);
                }
            } catch (Throwable e) {
                if (streamHandler != null) {
                    try {
                        streamHandler.onError(e);
                    } catch (Throwable ignored) {
                    }
                }
                if (!future.isDone()) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    /**
     * 流式聊天完成。
     * 
     * @param request 聊天请求
     * @param streamHandler 流式处理器
     * @return 聊天响应
     * @throws AiHttpException 调用失败时抛出
     */
    public AiHttpResponse chatStream(AiHttpRequest request, AiHttpStreamHandler streamHandler) {
        return execute(request, true, streamHandler);
    }

    private AiHttpResponse execute(AiHttpRequest request, boolean stream, AiHttpStreamHandler streamHandler) {
        return execute(request, stream, streamHandler, null);
    }

    private AiHttpResponse execute(AiHttpRequest request, boolean stream, AiHttpStreamHandler streamHandler, AiHttpFuture future) {
        if (request == null) {
            throw new IllegalArgumentException("request不能为空");
        }
        Map<String, Object> body = provider.buildRequestBody(request, stream);
        String requestJson = GsonUtils.get().toJson(body);
        Map<String, Object> headers = buildHeaders();
        String url = buildUrl();
        HttpHelper httpHelper = config.httpHelper();
        MyCall call = httpHelper.postJson(url, requestJson, null, headers);
        if (future != null) {
            future.bind(call);
        }
        if (streamHandler != null) {
            streamHandler.onStart(request);
        }
        try (Response response = call.executeForResponse()) {
            if (response.body() == null) {
                throw new AiHttpException("响应体为空", response.code(), null, url);
            }
            if (!response.isSuccessful()) {
                String responseBody = IOUtils.readText(response.body().byteStream(), StandardCharsets.UTF_8);
                throw buildResponseException(response.code(), responseBody, url);
            }
            if (stream) {
                return readStream(response.body().byteStream(), streamHandler, future);
            }
            String responseBody = IOUtils.readText(response.body().byteStream(), StandardCharsets.UTF_8);
            try {
                JsonObject json = GsonUtils.toJsonObjectWithException(responseBody);
                AiHttpResponse result = provider.parseResponse(json);
                if (streamHandler != null) {
                    streamHandler.onComplete(result);
                }
                return result;
            } catch (Exception e) {
                throw new AiHttpException("解析响应失败", response.code(), responseBody, url, e);
            }
        } catch (CancellationException e) {
            throw e;
        } catch (AiHttpException e) {
            throw e;
        } catch (Exception e) {
            if (future != null && future.isCancelled()) {
                throw new CancellationException("请求已取消");
            }
            throw new AiHttpException("调用大模型接口失败", null, null, url, e);
        }
    }

    private AiHttpResponse readStream(InputStream inputStream, AiHttpStreamHandler streamHandler, AiHttpFuture future) throws IOException, GsonSyntaxException {
        AiHttpResponseAccumulator accumulator = new AiHttpResponseAccumulator();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (future != null && future.isCancelled()) {
                    throw new CancellationException("请求已取消");
                }
                if (line.isEmpty() || !line.startsWith("data: ")) {
                    continue;
                }
                String payload = line.substring(6);
                if ("[DONE]".equals(payload)) {
                    continue;
                }
                JsonObject json;
                try {
                    json = GsonUtils.toJsonObjectWithException(payload);
                } catch (Exception e) {
                    throw new AiHttpException("解析流式分片失败", null, payload, buildUrl(), e);
                }
                AiHttpChunk chunk = provider.parseStreamChunk(json);
                accumulator.append(chunk);
                if (streamHandler != null) {
                    if (chunk.hasReasoning()) {
                        streamHandler.onReasoning(chunk.reasoning(), chunk);
                    }
                    if (chunk.hasContent()) {
                        streamHandler.onContent(chunk.content(), chunk);
                    }
                    if (chunk.hasToolCalls()) {
                        for (AiHttpToolCallDelta toolCall : chunk.toolCalls()) {
                            streamHandler.onToolCall(toolCall, chunk);
                        }
                    }
                    streamHandler.onChunk(chunk);
                }
            }
        }
        AiHttpResponse result = accumulator.toResponse();
        if (streamHandler != null) {
            streamHandler.onComplete(result);
        }
        return result;
    }

    private Map<String, Object> buildHeaders() {
        Map<String, Object> headers = new LinkedHashMap<>(config.headers());
        if (config.apiKey() != null && !config.apiKey().isEmpty()) {
            headers.put(config.apiKeyHeader(), config.apiKeyPrefix() + config.apiKey());
        }
        headers.putIfAbsent("Content-Type", "application/json");
        return headers;
    }

    private String buildUrl() {
        String baseUrl = config.baseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String path = config.chatPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    private AiHttpException buildResponseException(int statusCode, String responseBody, String url) {
        try {
            JsonObject json = GsonUtils.toJsonObjectWithException(responseBody);
            JsonElement error = JsonObjectUtils.get(json, "error");
            if (error != null && error.isJsonObject()) {
                String message = JsonObjectUtils.getStr(error.getAsJsonObject(), "message");
                return new AiHttpException(message == null ? "大模型接口返回异常" : message, statusCode, responseBody, url);
            }
            if (error != null && error.isJsonPrimitive()) {
                return new AiHttpException(error.getAsString(), statusCode, responseBody, url);
            }
        } catch (Exception ignored) {
        }
        return new AiHttpException("大模型接口返回异常", statusCode, responseBody, url);
    }
}
