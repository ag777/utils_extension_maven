package github.ag777.util.lang.faker;

import com.ag777.util.lang.RegexUtils;
import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * faker库拓展
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/24 11:51
 */
public class FakerUtils {

    public static String regexify(Faker faker, String reg, String[]... groups) {
        reg = buildReg(reg, groups);
        return faker.regexify(reg);
    }

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

    private FakerUtils() {}
}
