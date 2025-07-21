package github.ag777.util.script.ql.express.custom;

import com.alibaba.qlexpress4.runtime.Parameters;
import com.alibaba.qlexpress4.runtime.QContext;
import github.ag777.util.lang.RegexUtils;
import github.ag777.util.lang.exception.model.ValidateException;

import java.util.regex.Pattern;

/**
 * 示例ql正则表达式函数 调用示例: find("aa123a", "\\d+")
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/7/26 上午8:59
 */
public class QlFuncFind extends BaseQlFunction<String> {
    @Override
    public String getFunctionName() {
        return "find";
    }

    @Override
    public String execute(QContext qContext, Parameters parameters) throws Exception {
        String text = getStr(parameters, 0);
        if (text == null) {
            return null;
        }
        String regex = getStr(parameters, 1);
        if (regex == null) {
            throw new ValidateException("缺少参数[正则表达式]");
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (Exception e) {
            throw new ValidateException("参数[正则表达式]格式错误:"+regex, e);
        }
        String replacement = getStr(parameters, 2);
        if (replacement == null) {
            return RegexUtils.find(text, pattern);
        }
        return RegexUtils.find(text, pattern, replacement);
    }
}
