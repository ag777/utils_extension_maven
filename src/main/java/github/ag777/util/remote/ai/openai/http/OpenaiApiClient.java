package github.ag777.util.remote.ai.openai.http;

import com.google.gson.JsonObject;
import github.ag777.util.gson.GsonUtils;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.http.HttpHelper;
import github.ag777.util.http.HttpUtils;
import github.ag777.util.http.model.MyCall;
import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.StringUtils;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.http.interf.OpenaiOnMessage;
import github.ag777.util.remote.ai.openai.http.request.OpenaiRequestChat;
import github.ag777.util.remote.ai.openai.http.util.OpenaiResponseChatStreamUtil;
import github.ag777.util.remote.ai.openai.http.util.OpenaiResponseChatUtil;
import github.ag777.util.remote.ai.openai.model.AiTool;
import github.ag777.util.remote.ai.openai.model.request.RequestBase;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * openai API 客户端
 * 提供与openai服务端交互的完整功能，支持文本生成、对话、模型管理等功能
 *
 * @author ag777 <837915770@vip.qq.com>
 */
public class OpenaiApiClient {
//    private static final Pattern P_END_OF_THINKING = Pattern.compile("<(?:/(?:t(?:h(?:i(?:n(?:k)?)?)?)?)?)?$");
    private final HttpHelper httpHelper;

    private String host = "127.0.0.1";
    private int port = 11434;
    private String url; // url优先度最高
    private Map<String, Object> headers;

    /**
     * 构造函数
     *
     * @param httpHelper HttpHelper实例
     */
    public OpenaiApiClient(HttpHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * 构造函数
     *
     * 使用默认的HttpHelper实例
     */
    public OpenaiApiClient() {
        this(new HttpHelper(HttpUtils.defaultBuilder()
                .readTimeout(5, TimeUnit.MINUTES)
                .build(), null));
    }

    /**
     * 设置服务地址
     *
     * @param host 服务地址
     * @return 当前实例
     */
    public OpenaiApiClient host(String host) {
        this.host = host;
        return this;
    }

    /**
     * 设置请求URL
     *
     * @param url URL
     * @return 当前实例
     */
    public OpenaiApiClient url(String url) {
        if (url.endsWith("/")) {
            url = url.substring(url.length()-1);
        }
        this.url = url;
        return this;
    }

    /**
     * 设置服务端口
     *
     * @param port 服务端口
     * @return 当前实例
     */
    public OpenaiApiClient port(int port) {
        this.port = port;
        return this;
    }

    /**
     * 设置API密钥
     *
     * @param apiKey apikey
     * @return 当前实例
     */
    public OpenaiApiClient apiKey(String apiKey) {
        if (StringUtils.isEmpty(apiKey)) {
            return this;
        }
        return header("Authorization", "Bearer " + apiKey);
    }

    /**
     * 设置HTTP头
     *
     * @param key 请求头的键
     * @param value 请求头的值
     * @return 当前实例
     */
    public OpenaiApiClient header(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>(3);
        }
        headers.put(key, value);
        return this;
    }

    /**
     * 同步对话
     *
     * @param request 请求参数
     * @return 响应结果
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     */
    public OpenaiResponseChatUtil chat(OpenaiRequestChat request) throws ValidateException, IOException {
        JsonObject jo = post(request, "/v1/chat/completions");
        return new OpenaiResponseChatUtil(jo);
    }

    /**
     * 流式对话
     *
     * @param request 请求参数
     * @param onMessage 消费函数
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     * @throws InterruptedException 中断异常
     */
    public void chatStream(OpenaiRequestChat request, OpenaiOnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        boolean[] isThinking= {false};
        String[] functionName = new String[1];
        StringBuilder toolParams = new StringBuilder();
        postStream(request, "/v1/chat/completions", jo -> {
            // {"model":"qwen2.5:14b","created_at":"2024-12-05T00:39:03.577136734Z","message":{"role":"assistant","content":"```"},"done":false}
            // {"role":"assistant","content":"","tool_calls":[{"function":{"name":"get_current_weather","arguments":{"format":"celsius","location":"上海"}}}]}}
            OpenaiResponseChatStreamUtil res = new OpenaiResponseChatStreamUtil(jo);
            OpenaiResponseChatStreamUtil.Content message = res.getMessage();
            if (message == null) {
                return;
            }
            String content = message.content();
            String thinking = message.thinking();
            OpenaiResponseChatStreamUtil.ToolCall toolCall = message.toolCall();
            AiTool aiTool = new AiTool();
            if (content == null && thinking != null) {
                if (!isThinking[0]) {
                    isThinking[0] = true;
                    content = "<think>"+thinking;
                } else {
                    content = thinking;
                }
            }
            // 处理工具调用
            if (toolCall != null) {
                if (!StringUtils.isEmpty(toolCall.getName())) {
                    functionName[0] = toolCall.getName();
                }
                toolParams.append(toolCall.getArguments());
            }
            if ("tool_calls".equals(message.finishReason())) {
                aiTool = new AiTool(functionName[0], GsonUtils.get().toMapWithException(toolParams.toString()));
                toolParams.delete(0, toolParams.length());
            }
            onMessage.accept(content, aiTool, res);
        });
    }

    /**
     * 获取输入流
     *
     * @param request 请求参数
     * @param path    请求路径
     * @return 输入流
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     */
    private InputStreamResponse getInputStream(Object request, String path) throws IOException, ValidateException {
        String url = getUrl(path);
        String requestJson = GsonUtils.get().toJson(request);
        System.out.println(requestJson);
        System.out.println("===");
        MyCall call = httpHelper.postJson(url, requestJson, null, headers);
        Response response = call.executeForResponse();
        if (response.body() == null) {
            throw new IOException("Failed to get response stream");
        }
        InputStream in = response.body().byteStream();
        if (response.isSuccessful()) {
            return new InputStreamResponse(call, in);
        } else {
            String json = IOUtils.readText(in, StandardCharsets.UTF_8);
            try {
                JsonObject jo = GsonUtils.toJsonObjectWithException(json);
                throw new ValidateException(JsonObjectUtils.getStr(jo, "error", "大模型服务返回码:" + response.code()));
            } catch (GsonSyntaxException e) {
                throw new ValidateException("解析返回出现异常", e);
            }
        }
    }

    private <T>T get(String path, Class<T> clazz) throws IOException, ValidateException, InterruptedException {
        return get(path, json -> GsonUtils.get().fromJsonWithException(json, clazz));
    }

    private <T>T get(String path, ResultTrans<T> trans) throws IOException, ValidateException, InterruptedException {
        // 发起 HTTP GET 请求获取模型列表信息
        MyCall call = httpHelper.get(getUrl(path), null, null);

        // 执行请求并获取输入流，若响应体为空则抛出验证异常
        try (InputStream is = call.executeForInputStream().orElseThrow(() -> new ValidateException("Empty response body"))) {
            String json = IOUtils.readText(is, StandardCharsets.UTF_8);
            return trans.accept(json);
        } catch (GsonSyntaxException e) {
            throw new ValidateException("解析返回异常", e);
        }
    }

    /**
     * 发送POST请求
     *
     * @param request 请求参数
     * @param path    请求路径
     * @return 响应结果
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     */
    private JsonObject post(RequestBase<?> request, String path) throws IOException, ValidateException {
        request.stream(false);
        InputStreamResponse in = getInputStream(
                request,
                path
        );
        String json = IOUtils.readText(in.body, StandardCharsets.UTF_8);
        try {
            return GsonUtils.toJsonObjectWithException(json);
        } catch (GsonSyntaxException e) {
            throw new ValidateException("解析返回异常", e);
        }
    }

    /**
     * 发送POST请求（流式）
     *
     * @param request  请求参数
     * @param path     请求路径
     * @param consumer 消费函数
     * @throws IOException       IO异常
     * @throws ValidateException 验证异常
     */
    private void postStream(RequestBase<?> request, String path, StreamHandler<JsonObject> consumer) throws IOException, ValidateException, InterruptedException {
        request.stream(true);
        InputStreamResponse in = getInputStream(
                request,
                path
        );
        try {
            handleStream(in.body, consumer);
        } finally {
            in.call.cancel();
        }
    }

    /**
     * 处理流式响应
     *
     * @param in       输入流
     * @param consumer 消费函数
     * @throws ValidateException    验证异常
     * @throws InterruptedException 线程中断异常
     */
    private void handleStream(InputStream in, StreamHandler<JsonObject> consumer) throws ValidateException, InterruptedException {
        try {
            BufferedReader procin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            String json;
            while ((line = procin.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.startsWith("data: ")) {
                    continue;
                }
                json = line.substring(6);
                if ("[DONE]".equals(json)) {
                    continue;
                }
                JsonObject jo = GsonUtils.toJsonObjectWithException(json);
                consumer.accept(jo);
            }
        } catch (GsonSyntaxException|IllegalStateException e) {
            throw new ValidateException("解析返回出现异常", e);
        } catch (IOException e) {
            throw new ValidateException("解析返回出现IO异常", e);
        } finally {
            IOUtils.close(in);
        }

    }

    /**
     * 获取请求URL
     * 
     * @param path 请求路径
     * @return 请求URL
     */
    private String getUrl(String path) {
        if (url != null) {
            return url + path;
        }
        return "http://" + host + ":" + port + path;
    }

    /**
     * 流式处理函数
     * 
     * @param <T> 参数类型
     */
    @FunctionalInterface
    public interface StreamHandler<T> {
        void accept(T jo) throws GsonSyntaxException, ValidateException, InterruptedException;
    }

    @FunctionalInterface
    public interface ResultTrans<T> {
        T accept(String result) throws GsonSyntaxException, ValidateException, InterruptedException;
    }

    public record InputStreamResponse(MyCall call, InputStream body) {}
}
