package github.ag777.util.software.oof.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 115 网盘 文件条目
 * 对应示例（如：docx 等具体文件）
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class OofFileInfo extends OofEntry {

    /** 文件 ID（唯一） */
    private String fid;

    /** 用户 ID（归属用户） */
    private Integer uid;

    /** 大小（字节数） */
    private Integer s;

    /** 状态（推测：1 正常；其它值可能表示异常/回收站等） */
    private Integer sta;

    /** 父路径标识/盘符路径（推测，与 cid/pid 共同描述层级） */
    private String pt;

    /** 类型标识（推测：0=目录，1=文件；与 d/c 字段在不同接口有差异） */
    private Integer d;

    /** 计数/评论数（推测；部分接口为占位） */
    private Integer c;

    /** 图标/扩展名标识（如 docx、jpg 等） */
    private String ico;

    /** 分类（如 DOC、PIC 等）。字段名为 class，避免关键字冲突使用 classX */
    @SerializedName("class")
    private String classX;

    /** 文件属性（扩展信息，通常为空字符串） */
    private String fatr;

    /** 文件 SHA1 指纹 */
    private String sha;

    /** 质量/清晰度标识（推测） */
    private Integer q;
}


