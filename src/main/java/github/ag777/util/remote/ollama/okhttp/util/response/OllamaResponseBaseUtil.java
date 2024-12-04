package github.ag777.util.remote.ollama.okhttp.util.response;

import com.ag777.util.gson.GsonUtils;
import com.google.gson.JsonObject;
import lombok.Getter;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/5 上午10:37
 */
@Getter
public class OllamaResponseBaseUtil {
    protected JsonObject data;

    public OllamaResponseBaseUtil(JsonObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public String prettyFormat() {
        return GsonUtils.get().toPrettyJson(data);
    }
}
