package github.ag777.util.lang.type;

import java.util.Objects;
import java.util.function.Function;

/**
 * 多类型容器。
 * <p>内部持有单一原始值，由子类对外提供面向不同目标类型的"视图"（转换）。
 * 本类保持通用，不依赖任何具体业务类型。
 * <p>子类只需通过 {@link #convert(Case[])} 声明"源类型 -&gt; 目标值"的映射规则，
 * 类型判断与 null 兜底由本类统一处理。
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/8 09:49
 */
public abstract class MultiType {

    protected final Object value;

    protected MultiType(Object value) {
        this.value = value;
    }

    /**
     * @return 原始值
     */
    public Object val() {
        return value;
    }

    /**
     * @return 原始值是否为 null
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * @return 原始值的运行时类型，null 时返回 null
     */
    public Class<?> rawType() {
        return value == null ? null : value.getClass();
    }

    /**
     * @param type 类型
     * @return 原始值是否为指定类型的实例
     */
    public boolean is(Class<?> type) {
        return type.isInstance(value);
    }

    /**
     * 当原始值为指定类型时直接返回（强转），否则返回 null。不做语义转换。
     *
     * @param type 目标类型
     * @param <T>  目标类型
     * @return 原始值或 null
     */
    public <T> T as(Class<T> type) {
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * 按原始值的运行时类型，匹配第一条命中的规则并执行转换；value 为 null 或无规则命中时返回 null。
     *
     * @param cases 转换规则，按声明顺序匹配，见 {@link #when(Class, Function)}
     * @param <R>   目标类型
     * @return 转换结果
     */
    @SafeVarargs
    protected final <R> R convert(Case<R>... cases) {
        if (value == null) {
            return null;
        }
        for (Case<R> c : cases) {
            if (c.type().isInstance(value)) {
                return c.mapper().apply(value);
            }
        }
        return null;
    }

    /**
     * 构造一条转换规则。
     *
     * @param type   源类型
     * @param mapper 该源类型到目标值的转换函数
     * @param <S>    源类型
     * @param <R>    目标类型
     */
    protected static <S, R> Case<R> when(Class<S> type, Function<? super S, ? extends R> mapper) {
        @SuppressWarnings("unchecked")
        Function<Object, R> f = (Function<Object, R>) mapper;
        return new Case<>(type, f);
    }

    /**
     * 转换规则：源类型 + 转换函数。
     */
    protected record Case<R>(Class<?> type, Function<Object, R> mapper) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(value, ((MultiType) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
