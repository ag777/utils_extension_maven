package github.ag777.util.lang.type.impl;

import github.ag777.util.lang.ObjectUtils;
import github.ag777.util.lang.type.MultiType;

/**
 * 字符串和Integer双类型
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class StringInt extends MultiType {

    private StringInt(Object value) {
        super(value);
    }

    public static StringInt of(String value) {
        return new StringInt(value);
    }

    public static StringInt of(Integer value) {
        return new StringInt(value);
    }

    public String getStr() {
        return convert(
                when(String.class, s -> s),
                when(Integer.class, ObjectUtils::toStr)
        );
    }

    public Integer getInt() {
        return convert(
                when(String.class, ObjectUtils::toInt),
                when(Integer.class, i -> i)
        );
    }
}
