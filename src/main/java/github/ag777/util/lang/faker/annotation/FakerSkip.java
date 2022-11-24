package github.ag777.util.lang.faker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 跳过构造
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/24 16:15
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FakerSkip {
}
