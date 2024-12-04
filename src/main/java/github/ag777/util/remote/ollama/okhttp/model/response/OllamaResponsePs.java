package github.ag777.util.remote.ollama.okhttp.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Ollama 正在运行的模型列表响应
 * 包含当前加载在内存中的所有模型信息
 * 
 * 响应示例:
 * {
 *   "models": [                                // 模型列表
 *     {
 *       "name": "mistral:latest",             // 模型名称
 *       "model": "mistral:latest",            // 模型标识
 *       "size": 5137025024,                   // 模型大小（字节）
 *       "digest": "2ae6f6dd7a3d...",         // 模型摘要
 *       "details": {                          // 模型详细信息
 *         "parent_model": "",                 // 父模型
 *         "format": "gguf",                   // 模型格式
 *         "family": "llama",                  // 模型家族
 *         "families": ["llama"],              // 所属家族列表
 *         "parameter_size": "7.2B",           // 参数大小
 *         "quantization_level": "Q4_0"        // 量化级别
 *       },
 *       "expires_at": "2024-06-04T14:38:31.83753-07:00",  // 过期时间
 *       "size_vram": 5137025024               // 显存占用（字节）
 *     }
 *   ]
 * }
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/02/19 11:30
 */
@Data
public class OllamaResponsePs {

    private List<Model> models;

    /**
     * 正在运行的模型信息
     */
    @Data
    public static class Model {
        /** 模型名称 */
        private final String name;
        /** 模型标识 */
        private final String model;
        /** 模型大小（字节） */
        private final long size;
        /** 模型摘要 */
        private final String digest;
        /** 模型详细信息 */
        private final Details details;
        /** 过期时间 */
        @SerializedName("expires_at")
        private final Date expiresAt;
        /** 显存占用（字节） */
        @SerializedName("size_vram")
        private final long sizeVram;

    }

    /**
     * 模型详细信息
     */
    @Data
    public static class Details {
        /** 父模型 */
        private final String parentModel;
        /** 模型格式 */
        private final String format;
        /** 模型家族 */
        private final String family;
        /** 所属家族列表 */
        private final List<String> families;
        /** 参数大小 */
        private final String parameterSize;
        /** 量化级别 */
        private final String quantizationLevel;
    }
} 