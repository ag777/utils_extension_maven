package github.ag777.util.software.everything.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/8/9 下午3:55
 */
@Data
@Accessors(chain = true)
public class EveryThingSearchOptions {
    // 是否匹配路径
    private boolean matchPath;
    // 是否区分大小写
    private boolean matchCase;
    // 是否全字匹配
    private boolean matchWholeWord;
    // 是否使用正则表达式进行匹配
    private boolean useRegex;
    // 最大匹配数
    private int max;
    // 匹配结果的偏移量
    private int offset;
    // 排序方式
    private int sort;
    // 请求的特征码，用于标识请求的特定属性或行为
    private int requestFlags;


    public EveryThingSearchOptions() {
        matchPath = false;
        matchCase = false;
        matchWholeWord = false;
        useRegex = false;
        // The default state is 0xFFFFFFFF, or all results.
        max = 0xFFFFFFFF;
        offset = 0;
        sort = EverythingSorts.EVERYTHING_SORT_NAME_ASCENDING;
        requestFlags = DwRequestFlags.EVERYTHING_DEFAULT;
    }

    public static EveryThingSearchOptions newInstance() {
        return new EveryThingSearchOptions();
    }

    public EveryThingSearchOptions matchPath() {
        return setMatchPath(true);
    }
    public EveryThingSearchOptions matchCase() {
        return setMatchCase(true);
    }
    public EveryThingSearchOptions matchWholeWord() {
        return setMatchWholeWord(true);
    }
    public EveryThingSearchOptions setRegex() {
        return setUseRegex(true);
    }
}
