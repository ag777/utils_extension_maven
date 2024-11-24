package github.ag777.util.remote.ollama.okhttp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/17 23:46
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class OllamaRequestChat {

    private String model;
    private List<MessageDTO> messages;
    private Map<String, Object> options;
    private Boolean stream;

    public OllamaRequestChat addMessage(String role, String content) {
        if (messages == null) {
            messages = new ArrayList<>(1);
        }
        messages.add(new MessageDTO(role, content));
        return this;
    }

}
