package github.ag777.util.remote.ai.spring;

import github.ag777.util.remote.ai.spring.model.ChatEvents;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.annotation.Tool;
import reactor.core.Disposable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Spring AI 2.0 主要能力示例。
 * <p>
 * 此类不保存密钥，也不直接执行网络请求；调用方创建 OpenaiChatClient 后选择示例方法即可。
 */
public final class SpringAi2FeaturesDemo {

    private SpringAi2FeaturesDemo() {
    }

    /**
     * 2.0 会为 ChatClient 自动注册 ToolCallingAdvisor，无需手工编写工具调用循环。
     */
    public static String toolCalling(OpenaiChatClient client) {
        return client
                .user("今天是几号？请计算 7 天后的日期。")
                .tools(new DateTools())
                .call();
    }

    /**
     * 使用提供商原生 JSON Schema，并在响应不符合 Schema 时自动重试。
     */
    public static ResponseEntity<ChatResponse, TravelPlan> structuredOutput(OpenaiChatClient client) {
        return client
                .system("你是行程规划助手。")
                .user("生成一份杭州两日游计划。",
                        Map.of("messageId", "travel-demo", "priority", "normal"))
                .responseEntity(TravelPlan.class, true, true);
    }

    /**
     * OpenAI 2.0 专属选项，以及统一的 prompt cache token 指标。
     */
    public static CacheUsage reasoningAndCacheUsage(OpenaiChatClient client) {
        ChatResponse response = client
                .openAiOptions(options -> options
                        .model("gpt-5")
                        .reasoningEffort("medium")
                        .verbosity("low")
                        .promptCacheKey("spring-ai-2-demo")
                        .streamUsage(true))
                .user("简要解释 Reactor 的背压。")
                .callResponse();

        Usage usage = response.getMetadata().getUsage();
        return new CacheUsage(
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                usage == null ? null : usage.getCacheReadInputTokens(),
                usage == null ? null : usage.getCacheWriteInputTokens());
    }

    /**
     * 2.0 的聊天记忆必须为每次请求显式提供 conversationId。
     * 同一个 memoryAdvisor 和 conversationId 可在后续调用中继续使用。
     */
    public static MemorySession newMemorySession(int maxMessages) {
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .build();
        return new MemorySession(MessageChatMemoryAdvisor.builder(memory).build());
    }

    /**
     * 启动后可由任意线程调用返回值的 dispose()，取消信号会传播到底层 HTTP 流。
     */
    public static Disposable cancellableStream(OpenaiChatClient client) {
        return client
                .user("写一篇很长的科幻小说。")
                .stream((event, content) -> {
                    if (event == ChatEvents.MESSAGE || event == ChatEvents.THINKING) {
                        System.out.print(content);
                    }
                    else if (event == ChatEvents.CANCELLED) {
                        System.out.println("\n请求已取消");
                    }
                });
    }

    /**
     * 既可 future.result().join() 等待，也可 future.cancel() 主动中断。
     */
    public static OpenaiChatClient.CancellableCall cancellableCall(OpenaiChatClient client) {
        return client
                .user("生成一份详细的 Java 性能优化手册。")
                .callCancellable();
    }

    public record TravelPlan(String city, List<DayPlan> days) {
    }

    public record DayPlan(int day, List<String> activities) {
    }

    public record CacheUsage(Integer promptTokens,
                             Integer completionTokens,
                             Long cacheReadInputTokens,
                             Long cacheWriteInputTokens) {
    }

    public record MemorySession(MessageChatMemoryAdvisor advisor) {

        public String ask(OpenaiChatClient client, String conversationId, String question) {
            return client
                    .advisors(advisor)
                    .advisorParam(ChatMemory.CONVERSATION_ID, conversationId)
                    .user(question)
                    .call();
        }
    }

    public static final class DateTools {

        @Tool(description = "获取服务器当前日期")
        public String currentDate() {
            return LocalDate.now().toString();
        }

        @Tool(description = "在 ISO-8601 日期上增加指定天数")
        public String plusDays(String isoDate, int days) {
            return LocalDate.parse(isoDate).plusDays(days).toString();
        }
    }
}
