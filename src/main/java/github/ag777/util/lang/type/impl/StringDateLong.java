package com.kd.sprite2.util.lang.type.impl;

import com.kd.sprite2.util.lang.DateUtils;
import com.kd.sprite2.util.lang.ObjectUtils;
import com.kd.sprite2.util.lang.type.MultiType;

import java.util.Date;

/**
 * 字符串、Date、时间戳三类型
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/8 10:04
 */
public class StringDateLong extends MultiType {

    private StringDateLong(Object value) {
        super(value);
    }

    public static StringDateLong of(String date) {
        return new StringDateLong(date);
    }

    public static StringDateLong of(Date date) {
        return new StringDateLong(date);
    }

    public static StringDateLong of(Long timestamp) {
        return new StringDateLong(timestamp);
    }

    public String getStr() {
        return convert(
                when(String.class, s -> s),
                when(Date.class, d -> DateUtils.toString(d, DateUtils.DEFAULT_TEMPLATE_TIME)),
                when(Long.class, t -> DateUtils.toString(t, DateUtils.FORMATTER_TIME))
        );
    }

    public Date getDate() {
        return convert(
                when(String.class, ObjectUtils::toDate),
                when(Date.class, d -> d),
                when(Long.class, Date::new)
        );
    }

    public Long getLong() {
        return convert(
                when(String.class, s -> {
                    Date date = ObjectUtils.toDate(s);
                    return date != null ? date.getTime() : ObjectUtils.toLong(s);
                }),
                when(Date.class, Date::getTime),
                when(Long.class, t -> t)
        );
    }
}
