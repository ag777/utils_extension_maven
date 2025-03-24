package github.ag777.util.remote.ollama.okhttp;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.http.HttpHelper;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import com.google.gson.JsonObject;
import github.ag777.util.remote.ollama.okhttp.interf.OnMessage;
import github.ag777.util.remote.ollama.okhttp.model.OllamaToolCall;
import github.ag777.util.remote.ollama.okhttp.model.request.*;
import github.ag777.util.remote.ollama.okhttp.model.response.OllamaResponsePs;
import github.ag777.util.remote.ollama.okhttp.model.response.OllamaResponseTags;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseChatUtil;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseEmbedUtil;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseGenerateUtil;
import github.ag777.util.remote.ollama.openai.util.OpenaiResponseChatUtil;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Ollama API 客户端
 * 提供与Ollama服务端交互的完整功能，支持文本生成、对话、模型管理等功能
 * 
 * 主要功能：
 * 1. 文本生成
 *    - 同步生成 {@link #generate(OllamaRequestGenerate)}
 *    - 流式生成 {@link #generateStream(OllamaRequestGenerate, Consumer)}
 * 
 * 2. 对话功能
 *    - 同步对话 {@link #chat(OllamaRequestChat)}
 *    - 流式对话 {@link #chatStream(OllamaRequestChat, OnMessage)}
 * 
 * 3. 模型管理
 *    - 加载模型 {@link #loadModel(String)}
 *    - 卸载模型 {@link #unloadModel(String)}
 *    - 获取模型列表 {@link #listModels()}
 *    - 创建模型 {@link #createModel(OllamaRequestCreate)}
 *    - 删除模型 {@link #deleteModel(String)}
 *
 * 4. 嵌入向量
 *    - 生成文本的嵌入向量 {@link #embed(OllamaRequestEmbed)}
 * 
 * 配置说明：
 * - 默认连接本地服务 127.0.0.1:11434
 * - 支持通过 {@link #host(String)} 和 {@link #port(int)} 修改服务地址
 * - 默认请求超时时间为5分钟
 * 
 * 使用示例：
 * <pre>{@code
 * // 创建客户端
 * OllamaApiClient client = new OllamaApiClient();
 * 
 * // 1. 基础文本生成
 * OllamaRequestGenerate request = new OllamaRequestGenerate("llama2")
 *     .prompt("Tell me a joke");
 * OllamaResponseGenerate response = client.generate(request);
 * System.out.println(response.getMessage());
 * 
 * // 2. 流式对话
 * OllamaRequestChat chatRequest = new OllamaRequestChat("qwen")
 *     .addMessage("user", "What's the weather?");
 * client.chatStream(chatRequest, (message, tools) -> {
 *     System.out.print(message);  // 实时打印响应
 * });
 * 
 * // 3. 模型管理
 * List<OllamaResponseTags.Item> models = client.listModels();
 * for (OllamaResponseTags.Item model : models) {
 *     System.out.println(model.getName());
 * }
 * 
 * // 4. 删除模型
 * client.deleteModel("mario-assistant");  // 删除指定名称的模型
 * 
 * // 5. 生成嵌入向量
 * // 5.1 单个文本
 * OllamaResponseEmbed embedResponse = client.embed("all-minilm", "Hello, world!");
 * List<Float> vector = embedResponse.getEmbedding();
 * 
 * // 5.2 多个文本
 * List<String> texts = Arrays.asList("Hello", "World");
 * OllamaResponseEmbed embeddings = client.embed("all-minilm", texts);
 * List<List<Float>> vectors = embeddings.getEmbeddings();
 * }</pre>
 * 
 * 注意事项：
 * 1. 确保Ollama服务已启动且可访问
 * 2. 使用流式接口时注意处理异常
 * 3. 对于长文本生成，建议使用流式接口
 * 4. 模型加载可能需要较长时间，请耐心等待
 * 5. 删除模型操作不可恢复，请谨慎使用
 * 6. 生成嵌入向量时，建议使用专门的嵌入模型（如 all-minilm）
 * 
 * @author ag777 <837915770@vip.qq.com>
 */
public class OllamaApiClient {
    private final HttpHelper httpHelper;

    private String host = "127.0.0.1";
    private int port = 11434;

    /**
     * 构造函数
     * 
     * @param httpHelper HttpHelper实例
     */
    public OllamaApiClient(HttpHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * 构造函数
     * 
     * 使用默认的HttpHelper实例
     */
    public OllamaApiClient() {
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
    public OllamaApiClient host(String host) {
        this.host = host;
        return this;
    }

    /**
     * 设置服务端口
     * 
     * @param port 服务端口
     * @return 当前实例
     */
    public OllamaApiClient port(int port) {
        this.port = port;
        return this;
    }

    /**
     * 同步文本生成
     * 
     * @param request 请求参数
     * @return 响应结果
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     */
    public OllamaResponseGenerateUtil generate(OllamaRequestGenerate request) throws IOException, ValidateException {
        JsonObject jo = post(request, "/api/generate");
        return new OllamaResponseGenerateUtil(jo);
    }

    /**
     * 流式文本生成
     * 
     * @param request 请参数
     * @param consumer 消费函数
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     * @throws InterruptedException  中断异常
     */
    public void generateStream(OllamaRequestGenerate request, Consumer<String> consumer) throws IOException, ValidateException, InterruptedException {
        postStream(request, "/api/generate", false, jo -> {
            // {"model":"qwen2.5:14b","created_at":"2024-12-04T09:07:17.990324356Z","response":"To","done":false}
            String res = JsonObjectUtils.getStr(jo, "response");
            if (StringUtils.isEmpty(res)) {
                return;
            }
            consumer.accept(res);
        });
    }

    /**
     * 同步对话
     * 
     * @param request 请求参数
     * @return 响应结果
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     */
    public OllamaResponseChatUtil chat(OllamaRequestChat request) throws ValidateException, IOException {
        JsonObject jo = post(request, "/api/chat");
        return new OllamaResponseChatUtil(jo);
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
    public void chatStream(OllamaRequestChat request, OnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        postStream(request, "/api/chat", false, jo -> {
            // {"model":"qwen2.5:14b","created_at":"2024-12-05T00:39:03.577136734Z","message":{"role":"assistant","content":"```"},"done":false}
            // {"role":"assistant","content":"","tool_calls":[{"function":{"name":"get_current_weather","arguments":{"format":"celsius","location":"上海"}}}]}}
            OllamaResponseChatUtil res = new OllamaResponseChatUtil(jo);
            String message = res.getMessage();
            List<OllamaToolCall> functions = res.getToolCalls();
            if (message == null && functions == null) {
                return;
            }
            onMessage.accept(StringUtils.emptyIfNull(message), functions);
        });
    }

    /**
     * 同步对话
     *
     * @param request 请求参数
     * @return 响应结果
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     */
    public OpenaiResponseChatUtil chatCompletions(OllamaRequestChat request) throws ValidateException, IOException {
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
    public void chatCompletionsStream(OllamaRequestChat request, OnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        postStream(request, "/v1/chat/completions", true, jo -> {
            // {"model":"qwen2.5:14b","created_at":"2024-12-05T00:39:03.577136734Z","message":{"role":"assistant","content":"```"},"done":false}
            // {"role":"assistant","content":"","tool_calls":[{"function":{"name":"get_current_weather","arguments":{"format":"celsius","location":"上海"}}}]}}
            OpenaiResponseChatUtil res = new OpenaiResponseChatUtil(jo);
            String message = res.getMessage();
            if (message == null) {
                return;
            }
            onMessage.accept(StringUtils.emptyIfNull(message), null);
        });
    }

    /**
     * 生成文本的嵌入向量表示
     *
     * @param request 请求参数
     * @return 嵌入向量响应
     * @throws ValidateException 验证异常
     * @throws IOException IO异常
     */
    public OllamaResponseEmbedUtil embed(OllamaRequestEmbed request) throws ValidateException, IOException {
        JsonObject jo = post(request, "/api/embed");
        return new OllamaResponseEmbedUtil(jo);
    }

    /**
     * 加载模型
     * 
     * @param modelName 模型名称
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     */
    public void loadModel(String modelName) throws ValidateException, IOException {
        // {"model":"llama3.2","created_at":"2024-09-12T03:54:03.516566Z","response":"","done":true,"done_reason":"unload"}}
        // 忽略结果
        post(
                OllamaRequestGenerate.ofLoadModel(modelName),
                "/api/generate"
        );
    }

    /**
     * 卸载模型
     * 
     * @param modelName 模型名称
     * @throws ValidateException 验证异常
     * @throws IOException       IO异常
     */
    public void unloadModel(String modelName) throws ValidateException, IOException {
        // {"model":"llama3.2","created_at":"2024-09-12T03:54:03.516566Z","response":"","done":true,"done_reason":"unload"}}
        // 忽略结果
        post(
                OllamaRequestGenerate.ofUnloadModel(modelName),
                "/api/generate"
        );
    }

    /**
     * 获取模型列表
     * 
     * @return 模型列表
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     * @throws InterruptedException 中断异常
     */
    public List<OllamaResponseTags.Item> listModels() throws IOException, ValidateException, InterruptedException {
        OllamaResponseTags res = get("/api/tags", OllamaResponseTags.class);
        return res.getModels();
    }

    /**
     * 获取正在运行的模型列表
     * 返回当前加载在内存中的所有模型信息
     *
     * @return 正在运行的模型列表响应
     * @throws IOException         当发生 IO 错误时抛出
     * @throws JsonSyntaxException 当 JSON 解析错误时抛出
     * @throws ValidateException  验证异常
     * @throws InterruptedException 中断异常
     */
    public List<OllamaResponsePs.Model> ps() throws IOException, JsonSyntaxException, ValidateException, InterruptedException {
        OllamaResponsePs res = get("/api/ps", OllamaResponsePs.class);
        return res.getModels();
    }

    /**
     * 创建模型
     *
     * @param request 请求参数
     * @throws ValidateException 验证异常
     * @throws IOException IO异常
     */
    public void createModel(OllamaRequestCreate request) throws ValidateException, IOException {
        post(request, "/api/create");
    }

    /**
     * 流式创建模型
     *
     * @param request 请求参数
     * @param consumer 消费函数，用于处理创建过程中的状态信息
     * @throws ValidateException 验证异常
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    public void createModelStream(OllamaRequestCreate request, Consumer<String> consumer) throws ValidateException, IOException, InterruptedException {
        postStream(request, "/api/create", false, jo -> {
            // {"status":"reading model metadata"}
            // {"status":"creating system layer"}
            // {"status":"writing manifest"}
            // {"status":"success"}
            String status = JsonObjectUtils.getStr(jo, "status");
            if (StringUtils.isEmpty(status)) {
                return;
            }
            consumer.accept(status);
        });
    }

    /**
     * 删除模型及其数据
     * 如果模型不存在，返回 404 Not Found
     *
     * @param modelName 要删除的模型名称
     * @throws ValidateException 验证异常
     * @throws IOException IO异常
     */
    public void deleteModel(String modelName) throws ValidateException, IOException {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }

        String url = getUrl("/api/delete");
        JsonObject request = new JsonObject();
        request.addProperty("model", modelName);

        MyCall call = httpHelper.deleteJson(url, request.toString(), null, null);
        try {
            Response response = call.executeForResponse();

            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    throw new ValidateException("模型 " + modelName + " 不存在");
                }
                throw new ValidateException("删除模型失败，服务返回码:" + response.code());
            }
        } finally {
            IOUtils.close(call);
        }
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
        MyCall call = httpHelper.postJson(url, requestJson, null, null);
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
                throw new ValidateException(JsonObjectUtils.getStr(jo, "error", "ollama服务返回码:" + response.code()));
            } catch (JsonSyntaxException e) {
                throw new ValidateException("解析返回出现异常", e);
            }
        }
    }

    private <T>T get(String path, Class<T> clazz) throws IOException, ValidateException, InterruptedException {
        return get(path, json -> GsonUtils.get().fromJsonWithException(json, clazz));
    }

    private <T>T get(String path, ResultTrans<T> trans) throws IOException, ValidateException, InterruptedException {
        // 发起 HTTP GET 请求获取模型列表信息
        try (MyCall call = httpHelper.get(getUrl(path), null, null)) {
            // 执行请求并获取输入流，若响应体为空则抛出验证异常
            try (InputStream is = call.executeForInputStream().orElseThrow(() -> new ValidateException("Empty response body"))) {
                String json = IOUtils.readText(is, StandardCharsets.UTF_8);
                return trans.accept(json);
            } catch (JsonSyntaxException e) {
                throw new ValidateException("解析返回异常", e);
            }
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
    private JsonObject post(OllamaRequestBase<?> request, String path) throws IOException, ValidateException {
        request.stream(false);
        InputStreamResponse in = getInputStream(
                request,
                path
        );
        String json = IOUtils.readText(in.body, StandardCharsets.UTF_8);
        try {
            return GsonUtils.toJsonObjectWithException(json);
        } catch (JsonSyntaxException e) {
            throw new ValidateException("解析返回异常", e);
        }
    }

    /**
     * 发送POST请求（流式）
     *
     * @param request 请求参数
     * @param path    请求路径
     * @param openai 是否为openai格式
     * @param consumer 消费函数
     * @throws IOException          IO异常
     * @throws ValidateException    验证异常
     */
    private void postStream(OllamaRequestBase<?> request, String path, boolean openai, StreamHandler<JsonObject> consumer) throws IOException, ValidateException, InterruptedException {
        request.stream(true);
        InputStreamResponse in = getInputStream(
                request,
                path
        );
        try {
            handleStream(in.body, openai, consumer);
        } finally {
            in.call.cancel();
        }
    }

    /**
     * 处理流式响应
     * 
     * @param in      输入流
     * @param openai 是否为openai格式
     * @param consumer 消费函数
     * @throws ValidateException 验证异常
     * @throws InterruptedException 线程中断异常
     */
    private void handleStream(InputStream in, boolean openai, StreamHandler<JsonObject> consumer) throws ValidateException, InterruptedException {
        try {
            BufferedReader procin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            String json;
            while ((line = procin.readLine()) != null) {
                if (openai) {
                    if (!line.startsWith("data: ")) {
                        continue;
                    }
                    json = line.substring(6);
                    if ("[DONE]".equals(json)) {
                        continue;
                    }
                } else {
                    json = line;
                }
                JsonObject jo = GsonUtils.toJsonObjectWithException(json);
                consumer.accept(jo);
            }
        } catch (JsonSyntaxException|IllegalStateException e) {
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
        return "http://" + host + ":" + port + path;
    }

    /**
     * 流式处理函数
     * 
     * @param <T> 参数类型
     */
    @FunctionalInterface
    public interface StreamHandler<T> {
        void accept(T jo) throws JsonSyntaxException, ValidateException, InterruptedException;
    }

    @FunctionalInterface
    public interface ResultTrans<T> {
        T accept(String result) throws JsonSyntaxException, ValidateException, InterruptedException;
    }

    public record InputStreamResponse(MyCall call, InputStream body) {}
}
