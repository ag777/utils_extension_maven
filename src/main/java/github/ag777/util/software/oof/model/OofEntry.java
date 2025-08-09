package github.ag777.util.software.oof.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 115 网盘条目基类（公共字段）
 * 文件与文件夹公共的元数据均在此类中维护
 */
@NoArgsConstructor
@Data
public class OofEntry {

    /** 应用/来源标识（115 接口常见为 1） */
    private Integer aid;

    /** 所在目录 ID（当前条目所在父目录/分类） */
    private String cid;

    /** 名称（文件名或文件夹名，已转义的 Unicode 可能出现） */
    private String n;

    /** 类型/模式标识（推测：媒体/特殊类型标记，0 代表普通） */
    private Integer m;

    /** 取回码/短码（Pick Code，用于 Web 端定位/下载） */
    private String pc;

    /** 创建时间（可能为字符串或秒级时间戳字符串，接口表现不统一） */
    private String t;

    /** 更新时间（通常为秒级时间戳字符串） */
    private String te;

    /** 预览/缩略生成时间（推测，秒级时间戳字符串） */
    private String tp;

    /** 最近使用/访问时间（推测，秒级时间戳字符串） */
    private String tu;

    /** 最近操作/排序参考时间（推测，秒级时间戳字符串） */
    private String to;

    /** 排序/权重（0 为默认；部分接口用于置顶/自定义排序） */
    private Integer p;

    /** 子项数量（目录为包含的文件/文件夹数；文件场景可能为占位） */
    private Integer fc;

    /** 分享状态（0 未分享；非 0 可能为已分享/分享类型） */
    private Integer sh;

    /** 备注/扩展字段（通常为空字符串） */
    private String e;

    /** 描述标识/是否存在描述（推测） */
    private Integer fdes;

    /** 高清/高分辨率标识（推测） */
    private Integer hdf;

    /** 扩展类型/加密类型（推测） */
    private Integer et;

    /** 扩展位置/错误位点（推测） */
    private String epos;

    /** 文件版本/版本号（推测） */
    private Integer fvs;

    /** 校验码：0 表示正常；非 0 表示异常/风控等 */
    @SerializedName("check_code")
    private Integer checkCode;

    /** 校验信息（与 check_code 对应的人类可读信息） */
    @SerializedName("check_msg")
    private String checkMsg;

    /** 归属用户 ID（与登录用户一致） */
    private Integer fuuid;

    /**
     * 标签列表（Tag List）。当条目被打上标签时返回对应标签集合；
     * 无标签通常返回空数组。
     */
    private List<FlDTO> fl;

    /** 资源 URL/占位（下载/预览地址，很多接口返回为空） */
    private String u;

    /** 是否为快捷方式/引用（推测：0 否 1 是） */
    private Integer issct;

    /** 评分/排序分（推测） */
    private Integer score;

    /** 是否置顶（0 否 1 是） */
    @SerializedName("is_top")
    private Integer isTop;

    @NoArgsConstructor
    @Data
    public static class FlDTO {
        /** 标签 ID（字符串形式的长整型） */
        private String id;

        /** 标签名称（UTF-8 字符串，可能为转义的 Unicode） */
        private String name;

        /** 排序权重（字符串数字，"0" 表示默认） */
        private String sort;

        /** 颜色（十六进制 RGB，如 #FFC032） */
        private String color;

        /** 标签更新时间（Unix 秒级时间戳） */
        @SerializedName("update_time")
        private Integer updateTime;

        /** 标签创建时间（Unix 秒级时间戳） */
        @SerializedName("create_time")
        private Integer createTime;
    }
}


