package github.ag777.util.remote.chatgpt;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpHelper;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * chat-gpt 接口调用辅助类
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2023/10/28 16:53
 */
public class ChatGptHelper {
    private final String apiKey;
    private final HttpHelper http;
    
    public ChatGptHelper(String apiKey, OkHttpClient.Builder builder) {
        this.apiKey = apiKey;
        this.http = new HttpHelper(builder.build(), "chat-gpt");
    }
    
    public static ChatGptHelper withProxy(String apiKey, String host, int port) {
        return new ChatGptHelper(apiKey, HttpUtils.builderWithProxy(null, host, port));
    }

    public static void main(String[] args) throws IOException, ValidateException, JsonSyntaxException {
        // openai官网申请到的key, 代理地址，代理端口号
        String reply = withProxy("sk-xxx", "127.0.0.1", 8080)
                .post(
                        GptMessageBuilder.newInstance(GptMessageBuilder.TYPE_GPT_3_5_TURBO)
                                .addSystemMessage("系统提示词")
                                .addUserMessage("用户说的话")
                                .build()
                );

        System.out.println(reply);

    }
    public String post(GptRequest option) throws ValidateException, JsonSyntaxException, IOException {
        MyCall call = http.postJson(
                "https://api.openai.com/v1/chat/completions",
                GsonUtils.get().toJson(option),
                null,
                MapUtils.of(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer "+apiKey
                )
        );
        Optional<GtpResponse> res = call.executeForObjForce(GtpResponse.class);
        if (!res.isPresent()) {
            throw new ValidateException("返回为空");
        }

        if (res.get().getError() != null) {
            GtpResponse.ErrorDTO error = res.get().getError();
            throw new ValidateException(error.getType() + ": " + error.getMessage());
        }
        return res.get().getChoices().get(0).getMessage().getContent();
    }

    public static class GptMessageBuilder {
        public static final String TYPE_GPT_3_5_TURBO = "gpt-3.5-turbo";
        private final GptRequest request;

        public GptMessageBuilder(GptRequest request) {
            this.request = request;
        }

        public static GptMessageBuilder newInstance(String type) {
            GptRequest request = new GptRequest(type)   // 设置GPT3.5模型的名称或ID
                .setTemperature(0.5F) // 设置温度参数为0.5，控制生成文本的多样性
                .setTopP(1F) // 设置Top-p采样的概率阈值为1，控制生成文本时模型考虑的概率分布的范围
                .setFrequencyPenalty(0) // 设置频率惩罚参数为0，不惩罚模型生成高频词语的倾向
                .setPresencePenalty(1.1F) // 设置存在惩罚参数
                .setStream(false) // 设置是否通过流式传输方式返回结果为false
                .setN(1); // 设置生成文本的数量为1
            return new GptMessageBuilder(request);
        }

        /**
         * 添加一条消息。
         *
         * @param role    角色
         * @param content 消息内容
         * @return GptMessageBuilder实例
         */
        public GptMessageBuilder addMessage(String role, String content) {
            this.request.getMessages().add(new GptRequest.MessagesDTO(role, content));
            return this;
        }

        /**
         * 添加一条系统消息。
         *
         * @param content 消息内容
         * @return GptMessageBuilder实例
         */
        public GptMessageBuilder addSystemMessage(String content) {
            return addMessage(GptRequest.ROLE_SYSTEM, content);
        }

        /**
         * 添加一条用户消息。
         *
         * @param content 消息内容
         * @return GptMessageBuilder实例
         */
        public GptMessageBuilder addUserMessage(String content) {
            return addMessage(GptRequest.ROLE_USER, content);
        }

        /**
         * 添加一条助手消息。
         *
         * @param message 消息内容
         * @return GptMessageBuilder实例
         */
        public GptMessageBuilder addAssistantMessage(String message) {
            return addMessage(GptRequest.ROLE_ASSISTANT, message);
        }

        public GptRequest build() {
            return request;
        }

        public String getJson() {
            return GsonUtils.get().toJson(request);
        }
    }

    @NoArgsConstructor
    @Data
    @Accessors(chain = true)
    public static class GptRequest {
        public static final String ROLE_SYSTEM = "system";
        public static final String ROLE_USER = "user";
        public static final String ROLE_ASSISTANT = "assistant";

        /**
         * GPT3.5模型的名称
         */
        private String model;

        /**
         * 消息列表，用于生成文本的上下文。
         */
        private List<MessagesDTO> messages;

        /**
         * 温度参数控制生成文本的多样性。较高的温度值（如1.0）会使输出更随机和创造性，
         * 而较低的温度值（如0.2）会使输出更加确定和保守。0表示完全确定性的输出。
         * 值越大，回复越随机
         * 取值范围：[0.0, 1.0]之间的浮点数。
         */
        private Float temperature;

        /**
         * Top-p（nucleus）采样的概率阈值。它控制生成文本时模型考虑的概率分布的范围。
         * 较高的值（如1.0）会使更多的概率质量被考虑，而较低的值（如0.1）会使更少的概率质量被考虑。
         * 对于翻译文本的需求，通常建议将topP设置为较低的值，例如0.1或更低。较低的topP值有助于确保生成的翻译结果更加准确和一致，避免生成过多的不确定性和多样性
         * 取值范围：(0, 1]之间的浮点数。
         */
        @SerializedName("top_p")
        private Float topP;

        /**
         * 存在惩罚参数用于惩罚模型生成已经在输入文本中出现过的词语。较高的值会减少生成已经存在的词语的概率，
         * 从而使生成文本更加多样化。
         * 值越大，越有可能扩展到新话题
         * 取值范围：大于等于0的浮点数。
         */
        @SerializedName("presence_penalty")
        private Float presencePenalty;

        /**
         * 频率惩罚参数用于惩罚模型生成高频词语的倾向。较高的值会减少生成高频词语的概率，
         * 从而使生成文本更加多样化。
         * 值越大，越有可能降低重复字词
         * 取值范围：大于等于0的整数。
         */
        @SerializedName("frequency_penalty")
        private Integer frequencyPenalty;

        /**
         * 生成文本的最大长度限制。模型生成的文本将被截断至最大长度。
         * 本身具有默认值。通常情况下，建议根据您的应用场景和需求来设置 max_tokens 的值。
         * 取值范围：[1, 2048]之间的整数。
         */
        @SerializedName("max_tokens")
        private Integer maxTokens;

        /**
         * 控制是否通过流式传输方式返回结果。如果设置为true，则可以在模型生成文本的同时获取部分结果。
         */
        private Boolean stream;

        /**
         * 生成文本的数量。可以指定生成多少个不同的文本示例。
         * 取值范围：[1, 10]之间的整数。
         */
        private Integer n;

        public GptRequest(String model) {
            this.model = model;
            this.messages = new ArrayList<>();
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class MessagesDTO {
            /**
             * 消息的角色。可以是"system"、"user"或"assistant"之一
             */
            private String role;

            /**
             * 消息的内容。这是用户的输入或模型的先前回复文本
             */
            private String content;
        }
    }

    /**
     * {
     * "id": "xxx",
     * "object": "chat.completion",
     * "created": 1698482622,
     * "model": "gpt-3.5-turbo-0613",
     * "choices": [
     * {
     * "index": 0,
     * "message": {
     * "role": "assistant",
     * "content": "我是回复"
     * },
     * "finish_reason": "stop"
     * }
     * ],
     * "usage": {
     * "prompt_tokens": 201,
     * "completion_tokens": 125,
     * "total_tokens": 326
     * }
     * }
     * 或者
     * {
     * "error": {
     * "message": "Unrecognized request arguments supplied: frequencyPenalty, maxTokens, presencePenalty, topP",
     * "type": "invalid_request_error"
     * }
     * }
     */
    @NoArgsConstructor
    @Data
    public static class GtpResponse {

        private String id;
        private String object;
        private Integer created;
        private String model;
        private List<ChoicesDTO> choices;
        private UsageDTO usage;
        private ErrorDTO error;

        @NoArgsConstructor
        @Data
        public static class UsageDTO {
            private Integer promptTokens;
            private Integer completionTokens;
            private Integer totalTokens;
        }

        @NoArgsConstructor
        @Data
        public static class ChoicesDTO {
            private Integer index;
            private MessageDTO message;
            private String finishReason;

            @NoArgsConstructor
            @Data
            public static class MessageDTO {
                private String role;
                private String content;
            }
        }

        @NoArgsConstructor
        @Data
        public static class ErrorDTO {
            private String message;
            private String type;
        }
    }
}
