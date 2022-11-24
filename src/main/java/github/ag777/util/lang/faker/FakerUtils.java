package github.ag777.util.lang.faker;

import com.ag777.util.lang.RegexUtils;
import com.github.javafaker.Faker;
import github.ag777.util.lang.faker.annotation.FakerConfig;
import github.ag777.util.lang.faker.annotation.FakerSkip;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * faker库拓展
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/24 17:57
 */
public class FakerUtils {

    /**
     *
     * @param classOfT 对象对应类
     * @param <T> 对象类型
     * @return 生成的对象
     * @throws InstantiationException 对象初始化异常
     * @throws IllegalAccessException IllegalAccessException
     */
    public static <T>T build(Class<T> classOfT) throws InstantiationException, IllegalAccessException {
        T obj = classOfT.newInstance();
        return build(obj);
    }

    /**
     * 给对象的字段设置随机值
     * @param obj 对象
     * @param <T> 对象类型
     * @return 对象
     * @throws IllegalAccessException 设置字段值时发生异常
     */
    public static <T>T build(T obj) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(FakerSkip.class) != null) {
                continue;
            }
            Object value = newVal(new Faker(Locale.CHINA), field);
            if (value != null) {
                boolean accessible = field.isAccessible();
                try {
                    field.setAccessible(true);
                    field.set(obj, value);
                } finally {
                    field.setAccessible(accessible);
                }
            }
        }
        return obj;
    }

    /**
     * 根据正则和参数生成随机字符串
     * regexify(new Faker(), "\\d{3}$?$", {"aa"})=>"454aa"
     * @param faker Faker
     * @param reg 正则
     * @param groups 参数
     * @return 符合目标要求的字符串
     */
    public static String regexify(Faker faker, String reg, String[]... groups) {
        reg = buildReg(reg, groups);
        return faker.regexify(reg);
    }

    /**
     * 将参数回填到正则里
     * buildReg(".+$?$", {"aa"})=>".+aa"
     * @param reg 正则
     * @param groups 参数
     * @return 正则
     */
    private static String buildReg(String reg, String[]... groups) {
        if (groups == null) {
            return reg;
        }
        Pattern p = Pattern.compile("(?<!\\\\)\\$\\?\\$");
        return RegexUtils.replace(reg, p, (m, i)->{
            String[] group = groups[i];
            if (group.length == 0) {
                return "";
            }
            String replacement = Arrays.stream(group).map(e->
                    e.replace("\\","\\\\")
                            .replace(".", "\\.")
                            .replace("@", "\\@")
            ).collect(Collectors.joining("|"));
            return "("+replacement+")";
        });
    }

    /**
     * 根据字段类型和注解随机生成值
     * 目前仅仅支持部分基础类型
     * @param faker Faker
     * @param field 字段
     * @return 字段对应的值
     */
    private static Object newVal(Faker faker, Field field) {
        FakerConfig config = field.getAnnotation(FakerConfig.class);
        if (Number.class.isAssignableFrom(field.getType())) {
            if (Integer.class.isAssignableFrom(field.getType())) {
                int min = config.min() < Integer.MIN_VALUE?Integer.MIN_VALUE:(int)config.min();
                int max = config.max() > Integer.MAX_VALUE?Integer.MAX_VALUE:(int)config.max();
                return ThreadLocalRandom.current().nextInt(min, max);
            } else if (Long.class.isAssignableFrom(field.getType())) {
                return ThreadLocalRandom.current().nextLong(config.min(), config.max());
            } else if (Double.class.isAssignableFrom(field.getType())) {
                double min = config.min() < Double.MIN_VALUE?Double.MIN_VALUE:(int)config.min();
                double max = config.max() > Double.MAX_VALUE?Double.MAX_VALUE:(int)config.max();
                return ThreadLocalRandom.current().nextDouble(min, max);
            } else if (Float.class.isAssignableFrom(field.getType())) {
                return ThreadLocalRandom.current().nextFloat();
            }
        } else if(String.class.equals(field.getType())) {
            switch (config.type()) {
                case REGEX:
                    FakerConfig.FakerRegexPara[] fakerRegexParas = config.regexParas();
                    String[][] paras = Arrays.stream(fakerRegexParas).map(FakerConfig.FakerRegexPara::value)
                            .toArray(String[][]::new);
                    return FakerUtils.regexify(faker, config.regex(), paras);
                case NAME:
                    return faker.name().name();
                case PHONE:
                    return faker.phoneNumber().cellPhone();
                default:
                    return null;
            }
        } else if (Boolean.class.equals(field.getType())) {
            return ThreadLocalRandom.current().nextBoolean();
        }
        return null;
    }

    private FakerUtils() {}
}
