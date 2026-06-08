package com.kd.sprite2.util.lang.type.impl;

import com.kd.sprite2.util.lang.DateUtils;
import com.kd.sprite2.util.lang.ObjectUtils;
import com.kd.sprite2.util.lang.type.MultiType;

import java.util.Date;

/**
 * 字符串和Date双类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/8 10:04
 */
public class StringDate extends MultiType {
    private StringDate(Object value) {
        super(value);
    }

    public static StringDate of(String date) {
        return new StringDate(date);
    }

    public static StringDate of(Date date) {
        return new StringDate(date);
    }

    public String getStr() {
        return convert(
                when(String.class, s -> s),
                when(Date.class, d -> DateUtils.toString(d, DateUtils.DEFAULT_TEMPLATE_TIME))
        );
    }

    public Date getDate() {
        return convert(
                when(String.class, ObjectUtils::toDate),
                when(Date.class, d -> d)
        );
    }
}
