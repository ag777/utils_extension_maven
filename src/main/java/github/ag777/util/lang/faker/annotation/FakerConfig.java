package github.ag777.util.lang.faker.annotation;

import github.ag777.util.lang.faker.model.FakerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 假数据标注
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version  2022/11/24 16:06
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FakerConfig {
    FakerType type() default FakerType.REGEX;
    String regex() default "";
    FakerRegexPara[] regexParas() default {};
    long max() default Long.MAX_VALUE;
    long min() default Long.MIN_VALUE;


    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface FakerRegexPara {
        String[] value();
    }
}
