package github.ag777.util.remote.ai.spring;

import github.ag777.util.remote.ai.spring.model.ChatEvents;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 基于 Spring AI 2.0 的 ChatClient 轻量封装。
 * <p>
 * 每次调用 call/stream/entity 等终结方法后，当前请求会被清空，可继续复用本对象创建下一次请求。
 * 请求构建过程不是线程安全的；并发调用时请为每个请求创建独立实例，或直接使用 {@link #client()}。
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/7/29
 */
public class OpenaiChatClient {

    private static final String REASONING_CONTENT = "reasoningContent";

    private final ChatClient client;
    private ChatClientRequestSpec request;

    /**
     * 创建 DeepSeek 客户端，reasoning_content 会映射为 DeepSeekAssistantMessage。
     */
    public static OpenaiChatClient deepSeek(String baseUrl, String apiKey) {
        DeepSeekChatModel model = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .build();
        return new OpenaiChatClient(ChatClient.builder(model).build());
    }

    /**
     * 创建 OpenAI 或 OpenAI 兼容客户端。
     * Spring AI 2.0 已改用官方 openai-java SDK，连接参数由 OpenAiChatOptions 配置。
     */
    public static OpenaiChatClient openai(String baseUrl, String apiKey) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .options(options)
                .build();
        return new OpenaiChatClient(ChatClient.builder(model).build());
    }

    public OpenaiChatClient(ChatClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    public ChatClient client() {
        return client;
    }

    public OpenaiChatClient system(String system) {
        request().system(system);
        return this;
    }

    /**
     * Spring AI 2.0 支持为系统消息附加元数据。
     */
    public OpenaiChatClient system(String system, Map<String, Object> metadata) {
        request().system(spec -> spec.text(system).metadata(metadata));
        return this;
    }

    public OpenaiChatClient history(Message... messages) {
        request().messages(messages);
        return this;
    }

    public OpenaiChatClient history(List<Message> messages) {
        request().messages(messages);
        return this;
    }

    public OpenaiChatClient user(String user) {
        request().user(user);
        return this;
    }

    /**
     * Spring AI 2.0 支持为用户消息附加元数据。
     */
    public OpenaiChatClient user(String user, Map<String, Object> metadata) {
        request().user(spec -> spec.text(user).metadata(metadata));
        return this;
    }

    public OpenaiChatClient model(String model) {
        return options(ChatOptions.builder().model(model));
    }

    /**
     * 2.0 的请求选项 API 接收 Builder，以便与模型默认选项做属性级合并。
     */
    public OpenaiChatClient options(ChatOptions.Builder<?> options) {
        request().options(options);
        return this;
    }

    /**
     * 兼容旧调用方式，并转为 2.0 的 Builder API。
     */
    public OpenaiChatClient options(ChatOptions options) {
        return options(options.mutate());
    }

    /**
     * 配置 OpenAI 专属能力，例如 reasoningEffort、verbosity、serviceTier、promptCacheKey。
     */
    public OpenaiChatClient openAiOptions(Consumer<OpenAiChatOptions.Builder> customizer) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();
        customizer.accept(builder);
        return options(builder);
    }

    /**
     * 注册 ToolCallback、ToolCallbackProvider 或带 @Tool 方法的 POJO。
     * 2.0 会自动注册 ToolCallingAdvisor 并执行工具调用循环。
     */
    public OpenaiChatClient tools(Object... tools) {
        request().tools(tools);
        return this;
    }

    public OpenaiChatClient toolContext(Map<String, Object> context) {
        request().toolContext(context);
        return this;
    }

    public OpenaiChatClient advisors(Advisor... advisors) {
        request().advisors(advisors);
        return this;
    }

    /**
     * 设置 Advisor 参数；使用聊天记忆时必须在这里传入 ChatMemory.CONVERSATION_ID。
     */
    public OpenaiChatClient advisorParam(String name, Object value) {
        request().advisors(spec -> spec.param(name, value));
        return this;
    }

    public OpenaiChatClient advisorParams(Map<String, Object> params) {
        request().advisors(spec -> spec.params(params));
        return this;
    }

    public ChatClientRequestSpec request() {
        if (request == null) {
            request = client.prompt();
        }
        return request;
    }

    /**
     * 返回原始响应流。取消订阅会向上游传播 cancel，并关闭底层流式 HTTP 请求。
     */
    public Flux<ChatResponse> responseFlux() {
        return takeRequest().stream().chatResponse();
    }

    /**
     * 事件化流式调用。返回值调用 dispose() 即可随时中断请求。
     */
    public Disposable stream(BiConsumer<ChatEvents, String> eventHandler) {
        Objects.requireNonNull(eventHandler, "eventHandler");
        StreamEventParser parser = new StreamEventParser(eventHandler);
        AtomicBoolean terminated = new AtomicBoolean();

        return responseFlux()
                .doOnNext(parser::accept)
                .doOnCancel(() -> {
                    if (terminated.compareAndSet(false, true)) {
                        parser.cancel();
                    }
                })
                .subscribe(
                        ignored -> { },
                        error -> {
                            if (terminated.compareAndSet(false, true)) {
                                parser.error(error);
                            }
                        },
                        () -> {
                            if (terminated.compareAndSet(false, true)) {
                                parser.complete();
                            }
                        });
    }

    /**
     * 同步调用并返回最终文本。若需要可中断调用，请使用 callCancellable() 或 stream()。
     */
    public String call() {
        return takeRequest().call().content();
    }

    public ChatResponse callResponse() {
        return takeRequest().call().chatResponse();
    }

    /**
     * 同时返回模型响应与 Advisor 执行上下文，适合读取 RAG 文档等上下文数据。
     */
    public ChatClientResponse callClientResponse() {
        return takeRequest().call().chatClientResponse();
    }

    /**
     * 将模型输出转换成 Java 对象。
     *
     * @param nativeStructuredOutput 使用提供商原生 JSON Schema 约束
     * @param validateSchema 校验 Schema，失败时由 2.0 的递归 Advisor 自动重试
     */
    public <T> T entity(Class<T> type, boolean nativeStructuredOutput, boolean validateSchema) {
        return takeRequest().call().entity(type,
                spec -> configureEntity(spec, nativeStructuredOutput, validateSchema));
    }

    public <T> T entity(ParameterizedTypeReference<T> type,
                        boolean nativeStructuredOutput,
                        boolean validateSchema) {
        return takeRequest().call().entity(type,
                spec -> configureEntity(spec, nativeStructuredOutput, validateSchema));
    }

    /**
     * 同时保留 ChatResponse（含 token/cache usage）与结构化实体。
     */
    public <T> ResponseEntity<ChatResponse, T> responseEntity(
            Class<T> type, boolean nativeStructuredOutput, boolean validateSchema) {
        return takeRequest().call().responseEntity(type,
                spec -> configureEntity(spec, nativeStructuredOutput, validateSchema));
    }

    /**
     * 启动一个可取消、可等待结果的调用。
     */
    public CancellableCall callCancellable() {
        StringBuilder content = new StringBuilder();
        CompletableFuture<String> future = new CompletableFuture<>();
        Disposable subscription = responseFlux()
                .map(response -> response.getResult().getOutput().getText())
                .filter(Objects::nonNull)
                .subscribe(content::append, future::completeExceptionally,
                        () -> future.complete(content.toString()));
        return new CancellableCall(subscription, future);
    }

    private ChatClientRequestSpec takeRequest() {
        ChatClientRequestSpec current = request();
        request = null;
        return current;
    }

    private static void configureEntity(ChatClient.EntityParamSpec spec,
                                        boolean nativeStructuredOutput,
                                        boolean validateSchema) {
        if (nativeStructuredOutput) {
            spec.useProviderStructuredOutput();
        }
        if (validateSchema) {
            spec.validateSchema();
        }
    }

    /**
     * 可取消调用句柄。cancel() 等价于取消 Reactor Subscription。
     */
    public static final class CancellableCall implements AutoCloseable {
        private final Disposable subscription;
        private final CompletableFuture<String> result;

        private CancellableCall(Disposable subscription, CompletableFuture<String> result) {
            this.subscription = subscription;
            this.result = result;
        }

        public CompletableFuture<String> result() {
            return result;
        }

        public boolean cancel() {
            if (subscription.isDisposed()) {
                return false;
            }
            subscription.dispose();
            return result.cancel(false);
        }

        public boolean isDisposed() {
            return subscription.isDisposed();
        }

        @Override
        public void close() {
            cancel();
        }
    }

    private static final class StreamEventParser {
        private static final String THINK_START = "<think>";
        private static final String THINK_END = "</think>";

        private final BiConsumer<ChatEvents, String> handler;
        private final StringBuilder pending = new StringBuilder();
        private int state; // 0=尚未输出，1=思考，2=回答

        private StreamEventParser(BiConsumer<ChatEvents, String> handler) {
            this.handler = handler;
        }

        private void accept(ChatResponse response) {
            if (response == null || response.getResult() == null) {
                return;
            }
            AssistantMessage output = response.getResult().getOutput();
            String reasoning = reasoning(output);
            if (!isEmpty(reasoning)) {
                thinking(reasoning);
            }
            if (!isEmpty(output.getText()) && !isEmpty(reasoning)) {
                endThinking();
            }
            parseText(output.getText());
        }

        private static String reasoning(AssistantMessage output) {
            if (output instanceof DeepSeekAssistantMessage deepSeek) {
                return deepSeek.getReasoningContent();
            }
            Object value = output.getMetadata().get(REASONING_CONTENT);
            return value == null ? null : value.toString();
        }

        private void parseText(String text) {
            if (isEmpty(text)) {
                return;
            }
            pending.append(text);
            while (!pending.isEmpty()) {
                if (state == 0) {
                    int start = pending.indexOf(THINK_START);
                    if (start == 0) {
                        pending.delete(0, THINK_START.length());
                        startThinking();
                    }
                    else if (start > 0) {
                        message(take(start));
                    }
                    else if (couldBeTagPrefix(pending, THINK_START)) {
                        return;
                    }
                    else {
                        message(take(pending.length()));
                    }
                }
                else if (state == 1) {
                    int end = pending.indexOf(THINK_END);
                    if (end >= 0) {
                        thinking(take(end));
                        pending.delete(0, THINK_END.length());
                        endThinking();
                    }
                    else if (couldBeTagSuffix(pending, THINK_END)) {
                        int safeLength = pending.length() - matchingSuffixLength(pending, THINK_END);
                        thinking(take(safeLength));
                        return;
                    }
                    else {
                        thinking(take(pending.length()));
                    }
                }
                else {
                    message(take(pending.length()));
                }
            }
        }

        private void thinking(String content) {
            if (isEmpty(content)) {
                return;
            }
            startThinking();
            handler.accept(ChatEvents.THINKING, content);
        }

        private void startThinking() {
            if (state == 0) {
                state = 1;
                handler.accept(ChatEvents.THINKING_START, "");
            }
        }

        private void endThinking() {
            if (state == 1) {
                handler.accept(ChatEvents.THINKING_END, "");
                state = 0;
            }
        }

        private void message(String content) {
            if (isEmpty(content)) {
                return;
            }
            if (state == 1) {
                endThinking();
            }
            if (state != 2) {
                state = 2;
                handler.accept(ChatEvents.MESSAGE_START, "");
            }
            handler.accept(ChatEvents.MESSAGE, content);
        }

        private void complete() {
            flushPending();
            closeOpenSection();
            handler.accept(ChatEvents.COMPLETE, "");
        }

        private void cancel() {
            flushPending();
            closeOpenSection();
            handler.accept(ChatEvents.CANCELLED, "");
        }

        private void error(Throwable error) {
            flushPending();
            closeOpenSection();
            handler.accept(ChatEvents.ERROR,
                    error.getMessage() == null ? error.getClass().getName() : error.getMessage());
        }

        private void flushPending() {
            if (pending.isEmpty()) {
                return;
            }
            String content = take(pending.length());
            if (state == 1) {
                thinking(content);
            }
            else {
                message(content);
            }
        }

        private void closeOpenSection() {
            if (state == 1) {
                endThinking();
            }
            else if (state == 2) {
                handler.accept(ChatEvents.MESSAGE_END, "");
            }
        }

        private String take(int length) {
            String value = pending.substring(0, length);
            pending.delete(0, length);
            return value;
        }

        private static boolean couldBeTagPrefix(StringBuilder value, String tag) {
            return value.length() < tag.length() && tag.startsWith(value.toString());
        }

        private static boolean couldBeTagSuffix(StringBuilder value, String tag) {
            return matchingSuffixLength(value, tag) > 0;
        }

        private static int matchingSuffixLength(StringBuilder value, String tag) {
            int max = Math.min(value.length(), tag.length() - 1);
            for (int length = max; length > 0; length--) {
                if (value.substring(value.length() - length).equals(tag.substring(0, length))) {
                    return length;
                }
            }
            return 0;
        }

        private static boolean isEmpty(String value) {
            return value == null || value.isEmpty();
        }
    }
}
