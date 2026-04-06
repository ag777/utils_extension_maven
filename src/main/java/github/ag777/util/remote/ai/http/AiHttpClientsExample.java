package github.ag777.util.remote.ai.http;

import github.ag777.util.remote.ai.http.client.AiHttpChatSession;
import github.ag777.util.remote.ai.http.client.AiHttpClient;
import github.ag777.util.remote.ai.http.config.AiHttpClientConfig;
import github.ag777.util.remote.ai.http.model.AiHttpChunk;
import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;
import github.ag777.util.remote.ai.http.model.AiHttpToolCall;
import github.ag777.util.remote.ai.http.model.AiHttpToolCallDelta;
import github.ag777.util.remote.ai.http.stream.AiHttpFuture;
import github.ag777.util.remote.ai.http.stream.AiHttpStreamHandler;
import github.ag777.util.remote.ai.openai.model.request.RequestTool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link AiHttpClients} 使用示例。
 * <p>
 * 这是一个“示例类”，主要用于展示各种常见调用方式，默认不会自动执行真实网络请求。
 * 你可以把这里的方法复制到你自己的业务代码里，按需裁剪。
 * </p>
 *
 * @author ag777
 */
public class AiHttpClientsExample {

    /**
     * 改成你自己的兼容 OpenAI 协议地址，例如：
     * https://api.deepseek.com
     * https://api.siliconflow.cn
     */
    private static final String BASE_URL = "https://api.siliconflow.cn";

    /**
     * 改成你自己的 key。
     */
    private static final String API_KEY = "sk-dxjrmquxtvvlqpdpiovfoidxcgcjtwnqquavnsgzfexcralt";

    /**
     * 改成你自己的模型名。
     */
    private static final String MODEL = "deepseek-ai/DeepSeek-V3.2";

    private AiHttpClientsExample() {
    }

    /**
     * 如果你只是想快速试一下，可以手动执行这个 main。
     */
    public static void main(String[] args) throws Exception {
        // simplestChat();
        // sessionChat();
        // customRequestChat();
         streamChat();
        // asyncChat();
        // asyncChatAndCancel();
        // customConfigClient();
        // toolCallChat();
    }

    /**
     * 场景1：最简单的同步调用。
     * <p>
     * 适合一次性问答，不关心历史消息，不关心流式输出。
     * </p>
     */
    public static void simplestChat() {
        AiHttpClient client = AiHttpClients.openAiCompatible(BASE_URL, API_KEY);

        AiHttpResponse response = client.chat(
                AiHttpRequest.ofModel(MODEL)
                        .user("你好，请用三句话介绍一下自己")
        );

        System.out.println("content = " + response.content());
        System.out.println("reasoning = " + response.reasoning());
        printToolCalls(response);
    }

    /**
     * 场景2：会话式调用。
     * <p>
     * 适合你有系统提示词、固定模型、固定参数，需要多次复用一套上下文时使用。
     * </p>
     */
    public static void sessionChat() {
        AiHttpChatSession session = AiHttpClients.openAiCompatibleSession(BASE_URL, API_KEY, MODEL)
                .system("你是一个Java工具库设计助手")
                .temperature(0.3)
                .topP(0.8);

        AiHttpResponse response = session.chat("请帮我设计一个可取消的异步HTTP调用API");
        System.out.println(response.content());

        // 如果你愿意，也可以把自己的历史消息提前塞进去。
        session.assistant("上一次我们讨论的是异步封装与流式响应");
        AiHttpResponse next = session.chat("继续给出工具调用模型设计建议");
        System.out.println(next.content());
    }

    /**
     * 场景3：手动构造完整请求。
     * <p>
     * 适合你需要精细控制 messages/options/extraBody 时使用。
     * </p>
     */
    public static void customRequestChat() {
        AiHttpClient client = AiHttpClients.openAiCompatible(BASE_URL, API_KEY);

        AiHttpRequest request = AiHttpRequest.ofModel(MODEL)
                .system("你是一个严谨的代码审查助手")
                .user("请审查下面这段设计思路")
                .assistant("好的，请继续提供上下文")
                .user("我希望统一封装同步、流式、异步三种调用方式")
                .temperature(0.2)
                .maxTokens(2048)
                .option("presence_penalty", 0.1)
                .extraBody("reasoning_effort", "high");

        AiHttpResponse response = client.chat(request);
        System.out.println(response.content());
    }

    /**
     * 场景4：流式输出。
     * <p>
     * 适合聊天窗口、命令行实时打印、边生成边消费的场景。
     * </p>
     */
    public static void streamChat() {
        AiHttpChatSession session = AiHttpClients.openAiCompatibleSession(BASE_URL, API_KEY, MODEL)
                .system("你是一个中文技术写作助手");

        AiHttpResponse finalResponse = session.chatStream(
                "请分步骤讲解 CompletableFuture 和可取消 HTTP 请求如何结合",
                new AiHttpStreamHandler() {
                    @Override
                    public void onStart(AiHttpRequest request) {
                        System.out.println("=== stream start ===");
                    }

                    @Override
                    public void onReasoning(String delta, AiHttpChunk chunk) {
                        System.out.print(delta);
                    }

                    @Override
                    public void onContent(String delta, AiHttpChunk chunk) {
                        System.out.print(delta);
                    }

                    @Override
                    public void onToolCall(AiHttpToolCallDelta delta, AiHttpChunk chunk) {
                        if (delta.hasFunction()) {
                            System.out.println();
                            System.out.println("[tool delta] name=" + delta.function().name());
                            System.out.println("[tool delta] argumentsDelta=" + delta.function().argumentsDelta());
                        }
                    }

                    @Override
                    public void onComplete(AiHttpResponse response) {
                        System.out.println();
                        System.out.println("=== stream complete ===");
                    }
                }
        );

        System.out.println("final content = " + finalResponse.content());
    }

    /**
     * 场景5：异步调用。
     * <p>
     * 适合你要把请求挂到业务线程池、拼装回调链、或者并发发多个请求时使用。
     * </p>
     */
    public static void asyncChat() {
        AiHttpClient client = AiHttpClients.openAiCompatible(BASE_URL, API_KEY);

        AiHttpFuture future = client.chatAsync(
                AiHttpRequest.ofModel(MODEL)
                        .user("请给我一个简洁的 API 分层建议")
        );

        future.thenAccept(response -> {
            System.out.println("async result:");
            System.out.println(response.content());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        // 示例中不阻塞主线程；真实业务中可按需 join/get。
    }

    /**
     * 场景6：异步 + 流式 + 手动取消。
     * <p>
     * 适合用户点击“停止生成”时取消底层 HTTP 请求。
     * </p>
     */
    public static void asyncChatAndCancel() throws Exception {
        AiHttpChatSession session = AiHttpClients.openAiCompatibleSession(BASE_URL, API_KEY, MODEL);

        AiHttpFuture future = session.chatAsync(
                "请写一篇很长很长的文章来介绍 Java 中的异步编程",
                new AiHttpStreamHandler() {
                    @Override
                    public void onContent(String delta, AiHttpChunk chunk) {
                        System.out.print(delta);
                    }
                }
        );

        TimeUnit.SECONDS.sleep(2);

        // 模拟用户点击“停止生成”
        boolean cancelled = future.cancel(true);
        System.out.println();
        System.out.println("cancelled = " + cancelled);
    }

    /**
     * 场景7：自定义客户端配置。
     * <p>
     * 适合你需要改 header、改 path、改鉴权头、改 executor 时使用。
     * </p>
     */
    public static void customConfigClient() {
        AiHttpClientConfig config = AiHttpClientConfig.create("https://api.siliconflow.cn")
                .chatPath("/v1/chat/completions")
                .apiKey(API_KEY)
                .apiKeyHeader("Authorization")
                .apiKeyPrefix("Bearer ")
                .header("X-Trace-Id", "demo-trace-id");

        AiHttpClient client = AiHttpClient.openAiCompatible(config);

        AiHttpResponse response = client.chat(
                AiHttpRequest.ofModel("Qwen/Qwen2.5-7B-Instruct")
                        .user("请简单介绍一下硅基流动兼容 OpenAI 的调用方式")
        );

        System.out.println(response.content());
    }

    /**
     * 场景8：带 tool 定义的调用，以及如何读取 tool call。
     * <p>
     * 这里展示的是“声明工具 + 读取模型返回的工具调用”。
     * </p>
     */
    public static void toolCallChat() {
        AiHttpClient client = AiHttpClients.openAiCompatible(BASE_URL, API_KEY);

        RequestTool weatherTool = RequestTool.of("get_weather", "根据城市查询天气")
                .addParameter("city", "string", "城市名称", true, null)
                .addParameter("unit", "string", "温度单位", false, List.of("celsius", "fahrenheit"));

        AiHttpRequest request = AiHttpRequest.ofModel(MODEL)
                .system("你可以在必要时调用天气工具")
                .user("帮我查询上海的天气")
                .addTool(weatherTool)
                .option("tool_choice", "auto");

        AiHttpResponse response = client.chat(request);

        System.out.println("content = " + response.content());
        printToolCalls(response);
    }

    /**
     * 统一打印最终 tool call。
     */
    private static void printToolCalls(AiHttpResponse response) {
        if (response.toolCalls() == null || response.toolCalls().isEmpty()) {
            System.out.println("toolCalls = []");
            return;
        }
        for (AiHttpToolCall toolCall : response.toolCalls()) {
            System.out.println("toolCall.type = " + toolCall.type());
            System.out.println("toolCall.id = " + toolCall.id());
            if (toolCall.function() != null) {
                System.out.println("toolCall.function.name = " + toolCall.function().name());
                System.out.println("toolCall.function.argumentsText = " + toolCall.function().argumentsText());
                System.out.println("toolCall.function.arguments = " + toolCall.function().arguments());
            }
            if (!toolCall.extra().isEmpty()) {
                System.out.println("toolCall.extra = " + toolCall.extra());
            }
        }
    }
}
