package github.ag777.util.remote.ollama.okhttp;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpHelper;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.okhttp.model.MessageDTO;
import github.ag777.util.remote.ollama.okhttp.model.OllamaOptions;
import github.ag777.util.remote.ollama.okhttp.model.OllamaRequestChat;
import github.ag777.util.remote.ollama.okhttp.model.OllamaResponseChat;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <a href="https://github.com/ollama/ollama/blob/main/docs/api.md">ollama项目的API文档</a>
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/17 23:42
 */
public class OllamaChatHelper {
    @Setter
    private String host = "localhost";
    @Setter
    private int port = 11434;
    @Setter
    private String path = "/api/chat";

    private final HttpHelper http;

    public OllamaChatHelper(HttpHelper http) {
        this.http = http;
    }

    @SneakyThrows
    public static void main(String[] args) {
        OllamaChatHelper helper = new OllamaChatHelper(new HttpHelper(HttpUtils.defaultBuilder().readTimeout(5, TimeUnit.MINUTES).build(), null));
        helper.setHost("127.0.0.1");
        helper.chatAsync(
                "qwen2.5:14b",
                OllamaOptions.newInstance().toMap(),
                List.of(
                        userMessage("讲一个笑话")
                ),
                System.out::print
        );
    }


    public String chat(String modelName, Map<String, Object> options, List<MessageDTO> messages) throws IOException, ValidateException {
        OllamaResponseChat res = chatForRes(modelName, options, messages);
        return getMessage(res);
    }

    public OllamaResponseChat chatForRes(String modelName, Map<String, Object> options, List<MessageDTO> messages) throws IOException, ValidateException {
        InputStream is = getInputStream(
                getRequest(modelName, options, messages));
        String reply = IOUtils.readText(is, StandardCharsets.UTF_8);
        return GsonUtils.get().fromJson(reply, OllamaResponseChat.class);
    }

    public void chatAsync(String modelName, Map<String, Object> options, List<MessageDTO> messages, Consumer<String> onMsg) throws IOException, ValidateException {
        chatAsyncForRes(modelName, options, messages, res->onMsg.accept(getMessage(res)));
    }

    public void chatAsyncForRes(String modelName, Map<String, Object> options, List<MessageDTO> messages, Consumer<OllamaResponseChat> onMsg) throws IOException, ValidateException {
        InputStream is = getInputStream(
                getRequest(modelName, options, messages));
        IOUtils.readLines(is, line->{
            OllamaResponseChat msg = GsonUtils.get().fromJson(line, OllamaResponseChat.class);
            onMsg.accept(msg);
        },StandardCharsets.UTF_8);
    }

    private static OllamaRequestChat getRequest(String modelName, Map<String, Object> options, List<MessageDTO> messages) {
        return new OllamaRequestChat()
                .setModel(modelName)
                .setOptions(options)
                .setStream(false)
                .setMessages(messages);
    }

    private static String getMessage(OllamaResponseChat res) {
        return res.getMessage().getContent();
    }


    private InputStream getInputStream(OllamaRequestChat requestChat) throws IOException, ValidateException {
        MyCall call = http.postJson(
                getUrl(),
                GsonUtils.get().toJson(requestChat),
                null,
                null
        );
        Response response = call.executeForResponse();
        if (!response.isSuccessful()) {
            throw new ValidateException("response code:"+response.code());
        }
        Optional<InputStream> inputStream = HttpUtils.responseInputStream(response);
        if (!inputStream.isPresent()) {
            throw new ValidateException("response is empty");
        }
        return inputStream.get();
    }

    /**
     * 创建一个系统消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为系统角色的消息对象。
     */
    public static MessageDTO systemMessage(String message) {
        return new MessageDTO(MessageDTO.ROLE_SYSTEM, message);
    }

    /**
     * 创建一个用户消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为用户角色的消息对象。
     */
    public static MessageDTO userMessage(String message) {
        return new MessageDTO(MessageDTO.ROLE_USER, message);
    }

    /**
     * 创建一个助手消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为助手角色的消息对象。
     */
    public static MessageDTO assistantMessage(String message) {
        return new MessageDTO(MessageDTO.ROLE_ASSISTANT, message);
    }

    private String getUrl() {
        return "http://" + host + ":" + port+path;
    }
}
