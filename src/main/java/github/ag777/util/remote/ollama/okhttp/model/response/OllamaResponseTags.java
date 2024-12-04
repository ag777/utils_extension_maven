package github.ag777.util.remote.ollama.okhttp.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:46
 */
@Data
public class OllamaResponseTags {
    List<Item> models;

    @Data
    public static class Item {
        private String name;
        private String model;
        @SerializedName("modified_at")
        private String modifiedAt;
        private Long size;
        private String digest;
    }
}
