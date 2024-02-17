package github.ag777.util.remote.ollama;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpHelper;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.model.OllamaRequestChat;
import github.ag777.util.remote.ollama.model.OllamaResponseChat;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <a href="https://github.com/ollama/ollama/blob/main/docs/api.md">ollama项目的API文档</a>
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/17 23:42
 */
public class OllamaChatHelper {
    public static final String MODEL_NAME_QWEN_7B = "qwen:7b";
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
        helper.post(
                new OllamaRequestChat()
                        .setModel(MODEL_NAME_QWEN_7B)
                        .addMessage(
                                OllamaRequestChat.MessagesDTO.ROLE_USER,
                                "计算1+1"
                        ),
                m-> System.out.print(m.getMessage().getContent())
        );
    }

    public void post(OllamaRequestChat requestChat, Consumer<OllamaResponseChat> onLine) throws IOException, ValidateException {
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
        try (InputStream is = inputStream.get();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                OllamaResponseChat msg = GsonUtils.get().fromJson(line, OllamaResponseChat.class);
                onLine.accept(msg);
            }
        }
    }

    private String getUrl() {
        return "http://" + host + ":" + port+path;
    }
}
