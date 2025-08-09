package github.ag777.util.software.oof.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 115 网盘 文件夹条目
 * 对应示例（如：手机相册 等目录）
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class OofFolder extends OofEntry {

    /** 父目录 ID（根目录为 "0"） */
    private String pid;

    /** 备注/颜色标记/自定义标签（推测：cc） */
    private String cc;

    /** 规范化名称/别名（推测：ns，接口中常与 n 一致） */
    private String ns;

    /** 系统相册/特殊目录标识（推测：0 否，1 是） */
    private Integer ispl;
}


