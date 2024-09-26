package github.ag777.util.remote.ollama.okhttp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/17 23:43
 */
@NoArgsConstructor
@Data
public class OllamaResponseChat {


    private String model;
    private String createdAt;
    private MessageDTO message;
    private Boolean done;

    @NoArgsConstructor
    @Data
    public static class MessageDTO {
        private String role;
        private String content;
    }
}
