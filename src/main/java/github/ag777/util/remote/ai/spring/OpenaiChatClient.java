package github.ag777.util.remote.ai.spring;

import github.ag777.util.lang.StringUtils;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

import reactor.core.Disposable;

import java.util.List;
import java.util.function.BiConsumer;

import github.ag777.util.remote.ai.spring.model.ChatEvents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;

/**
 * 基于Spring AI的OpenAI客户端
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/7/24 上午11:04
 */
public class OpenaiChatClient {

    private final ChatClient client;
    private ChatClientRequestSpec request;

    /**
     * 创建DeepSeek客户端(思考内容放在reasoning_content中)
     * @param baseUrl 基础URL
     * @param apiKey API密钥
     * @return 客户端
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
     * 创建OpenAI客户端(思考内容放在think标签中)
     * @param apiKey API密钥
     * @return 客户端
     */
    public static OpenaiChatClient openai(String baseUrl, String apiKey) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .build();
        return new OpenaiChatClient(ChatClient.builder(model).build());
    }

    /**
     * 构造函数
     * @param client 客户端
     */
    public OpenaiChatClient(ChatClient client) {
        this.client = client;
    }

    /**
     * 设置系统提示词
     * @param system 系统提示词
     * @return 当前对象
     */
    public OpenaiChatClient system(String system) {
        request().system(system);
        return this;
    }

    /**
     * 设置历史消息
     * @param messages 历史消息
     * @return 当前对象
     */
    public OpenaiChatClient history(Message... messages) {
        request().messages(messages);
        return this;
    }

    /**
     * 设置历史消息
     * @param messages 历史消息
     * @return 当前对象
     */
    public OpenaiChatClient history(List<Message> messages) {
        request().messages(messages);
        return this;
    }

    /**
     * 设置用户提示词
     * @param user 用户提示词
     * @return 当前对象
     */
    public OpenaiChatClient user(String user) {
        request().user(user);
        return this;
    }

    /**
     * 设置模型
     * @param model 模型
     * @return 当前对象
     */
    public OpenaiChatClient model(String model) {
        return options(ChatOptions.builder().model(model).build());
    }

    /**
     * 设置选项
     * @param options 选项
     * @return 当前对象
     */
    public OpenaiChatClient options(ChatOptions options) {
        request().options(options);
        return this;
    }

    /**
     * 获取提示词
     * @return 提示词
     */
    public ChatClientRequestSpec request() {
        if (request == null) {
            request = this.client.prompt();
        }
        return request;
    }

    /**
     * 流式返回
     * @param eventHandler 事件处理器
     * @return 返回结果
     */
    public Disposable stream(BiConsumer<ChatEvents, String> eventHandler) {
        int[] state = {0}; // 0=初始状态，1=思考模式，2=消息模式
        StringBuilder thinkingBuffer = new StringBuilder();
        return request()
            .stream()
            .chatResponse()
            .doOnNext(res -> {
                AssistantMessage output = res.getResult().getOutput();
                String text = output.getText();
                if (output instanceof DeepSeekAssistantMessage) {
                    String reasoningContent = ((DeepSeekAssistantMessage) output).getReasoningContent();
                    if (!StringUtils.isEmpty(text)) {
                        // 切换到消息模式
                        if (state[0] == 1) {
                            // 从思考模式切换到消息模式
                            eventHandler.accept(ChatEvents.THINKING_END, "");
                        }
                        if (state[0] != 2) {
                            eventHandler.accept(ChatEvents.MESSAGE_START, "");
                            state[0] = 2;
                        }
                        eventHandler.accept(ChatEvents.MESSAGE, text);
                    } else if (!StringUtils.isEmpty(reasoningContent)) {
                        // 进入思考模式
                        if (state[0] == 0) {
                            eventHandler.accept(ChatEvents.THINKING_START, "");
                            state[0] = 1;
                        }
                        eventHandler.accept(ChatEvents.THINKING, reasoningContent);
                    }
                } else {
                    if (StringUtils.isEmpty(text)) {
                        return;
                    }
                    // 处理<think>和</think>中间的思考内容
                    if (state[0] == 0) {
                        if (text.startsWith("<think>")) {
                            // 进入思考模式
                            eventHandler.accept(ChatEvents.THINKING_START, "");
                            state[0] = 1;
                            String remaining = text.substring(7); // 去掉<think>
                            // 检查是否包含</think>
                            int endIndex = remaining.indexOf("</think>");
                            if (endIndex != -1) {
                                // 思考内容完整，一个消息中包含完整的<think>...</think>
                                String thinkContent = remaining.substring(0, endIndex);
                                if (!thinkContent.isEmpty()) {
                                    eventHandler.accept(ChatEvents.THINKING, thinkContent);
                                }
                                // 结束思考，进入消息模式
                                eventHandler.accept(ChatEvents.THINKING_END, "");
                                String messageContent = remaining.substring(endIndex + 8); // 去掉</think>
                                if (!messageContent.isEmpty()) {
                                    eventHandler.accept(ChatEvents.MESSAGE_START, "");
                                    eventHandler.accept(ChatEvents.MESSAGE, messageContent);
                                    state[0] = 2;
                                }
                            } else {
                                // 只有开始标签，后续内容都是思考内容
                                if (!remaining.isEmpty()) {
                                    thinkingBuffer.append(remaining);
                                }
                            }
                        } else {
                            // 直接进入消息模式
                            eventHandler.accept(ChatEvents.MESSAGE_START, "");
                            eventHandler.accept(ChatEvents.MESSAGE, text);
                            state[0] = 2;
                        }
                    } else if (state[0] == 1) {
                        // 当前在思考模式
                        int endIndex = text.indexOf("</think>");
                        if (endIndex != -1) {
                            // 找到结束标签
                            String thinkContent = text.substring(0, endIndex);
                            thinkingBuffer.append(thinkContent);
                            if (!thinkingBuffer.isEmpty()) {
                                eventHandler.accept(ChatEvents.THINKING, thinkingBuffer.toString());
                                thinkingBuffer.setLength(0);
                            }
                            // 结束思考，进入消息模式
                            eventHandler.accept(ChatEvents.THINKING_END, "");
                            String messageContent = text.substring(endIndex + 8); // 去掉</think>
                            if (!messageContent.isEmpty()) {
                                eventHandler.accept(ChatEvents.MESSAGE_START, "");
                                eventHandler.accept(ChatEvents.MESSAGE, messageContent);
                            }
                            state[0] = 2;
                        } else {
                            // 继续积累思考内容
                            thinkingBuffer.append(text);
                        }
                    } else {
                        // 消息模式，直接输出
                        eventHandler.accept(ChatEvents.MESSAGE, text);
                    }
                }
            })
            .doOnComplete(() -> {
                // 如果还有未处理的思考内容，先输出
                if (!thinkingBuffer.isEmpty()) {
                    eventHandler.accept(ChatEvents.THINKING, thinkingBuffer.toString());
                    eventHandler.accept(ChatEvents.THINKING_END, "");
                }
                eventHandler.accept(ChatEvents.COMPLETE, "");
            })
            .subscribe();
    }

    /**
     * 调用AI模型
     * @return 返回结果
     */
    public String call() {
        StringBuilder sb = new StringBuilder();
        request()
            .stream()
            .chatResponse()
            .doOnNext(res -> {
                AssistantMessage output = res.getResult().getOutput();
                if (output instanceof DeepSeekAssistantMessage) {
                    String text = output.getText();
                    if (StringUtils.isEmpty(text)) {
                        String reasoningContent = ((DeepSeekAssistantMessage) output).getReasoningContent();
                        if (StringUtils.isEmpty(reasoningContent)) {
                            // 思考和回答都为空,跳过处理
                            return;
                        }
                        if (sb.isEmpty()) {
                            sb.append("<think>");
                        }
                        sb.append(reasoningContent);
                    } else {
                        if (!sb.isEmpty()) {
                            sb.append("</think>");
                        }
                        sb.append(text);
                    }
                }
            })  // 实时打印每个流式片段
            .doOnComplete(() -> System.out.println("\n--- 流式响应完成 ---"))  // 流结束时打印提示
            .blockLast();// 阻塞等待流完成
        return sb.toString();
    }

}
