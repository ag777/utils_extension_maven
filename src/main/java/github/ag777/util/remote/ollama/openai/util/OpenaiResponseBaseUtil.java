package github.ag777.util.remote.ollama.openai.util;

import com.ag777.util.gson.GsonUtils;
import com.google.gson.JsonObject;
import lombok.Getter;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/24 上午10:20
 */
@Getter
public class OpenaiResponseBaseUtil {
    protected JsonObject data;

    public OpenaiResponseBaseUtil(JsonObject data) {
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
