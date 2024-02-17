package github.ag777.util.remote.ollama.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/17 23:46
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class OllamaRequestChat {

    private String model;
    private List<MessagesDTO> messages;
    private Boolean stream;

    public OllamaRequestChat addMessage(String role, String content) {
        if (messages == null) {
            messages = new ArrayList<>(1);
        }
        messages.add(new MessagesDTO(role, content));
        return this;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Accessors(chain = true)
    public static class MessagesDTO {
        public static final String ROLE_SYSTEM = "system";
        public static final String ROLE_USER = "user";
        public static final String ROLE_ASSISTANT = "assistant";

        private String role;
        private String content;
    }
}
