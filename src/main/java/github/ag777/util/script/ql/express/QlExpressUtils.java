package github.ag777.util.script.ql.express;


import com.alibaba.qlexpress4.Express4Runner;
import com.alibaba.qlexpress4.InitOptions;
import com.alibaba.qlexpress4.QLOptions;
import com.alibaba.qlexpress4.aparser.ImportManager;
import com.alibaba.qlexpress4.exception.QLException;
import com.alibaba.qlexpress4.security.QLSecurityStrategy;
import github.ag777.util.script.ql.express.custom.BaseQlFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * <a href="https://github.com/alibaba/QLExpress/tree/branch_version_4.0.0.dev">官网</a>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/5/9 上午10:07
 */
@Slf4j
public class QlExpressUtils {
    
    private static final Express4Runner runner = new Express4Runner(InitOptions.builder()
        // 在创建 Express4Runner 时默认导入工具包下的所有类，此时脚本中就不需要额外的 import 语句    
        .addDefaultImport(
                Collections.singletonList(ImportManager.importPack("github.ag777.util"))
        )
        .securityStrategy(QLSecurityStrategy.open())
        .build());

    /**
     * 添加自定义函数
     * @param customFunction 自定义函数
     */
    public static void addCustomFunction(BaseQlFunction<?> customFunction) {
        runner.addFunction(customFunction.getFunctionName(), customFunction);
    }

    /**
     * 执行表达式并返回字符串
     * @param express 表达式
     * @param dataModel 数据模型
     * @return 执行结果
     * @throws QLException 执行异常
     */
    public static String execute2Str(String express, Map<String, Object> dataModel) throws QLException {
        return execute2Str(express, dataModel, QLOptions.builder().precise(true).build());
    }

    /**
     * 执行表达式并返回结果
     * @param express 表达式
     * @param dataModel 数据模型
     * @return 执行结果
     * @throws QLException 执行异常
     */
    public static Object execute(String express, Map<String, Object> dataModel) throws QLException {
        return runner.execute(express, dataModel, QLOptions.builder().precise(true).build()).getResult();
    }

    /**
     * 执行表达式并返回字符串
     * @param express 表达式
     * @param dataModel 数据模型
     * @param timeoutMillis 超时时间
     * @return 执行结果
     * @throws QLException 执行异常 
     */
    public static String execute2Str(String express, Map<String, Object> dataModel, long timeoutMillis) throws QLException {
        return execute2Str(express, dataModel, QLOptions.builder().precise(true).timeoutMillis(timeoutMillis).build());
    }

    /**
     * 执行表达式并返回结果
     * @param express 表达式
     * @param dataModel 数据模型
     * @param timeoutMillis 超时时间
     * @return 执行结果
     * @throws QLException 执行异常
     */
    public static Object execute(String express, Map<String, Object> dataModel, long timeoutMillis) throws QLException {
        return execute(express, dataModel, QLOptions.builder().precise(true).timeoutMillis(timeoutMillis).build());
    }

    /**
     * 执行表达式并返回字符串
     * @param express 表达式
     * @param dataModel 数据模型
     * @param qlOptions 选项
     * @return 执行结果
     * @throws QLException 执行异常
     */
    public static String execute2Str(String express, Map<String, Object> dataModel, QLOptions qlOptions) throws QLException {
        Object obj = execute(express, dataModel, qlOptions);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * 执行表达式并返回结果
     * @param script 表达式
     * @param context 数据模型
     * @param qlOptions 选项
     * @return 执行结果
     * @throws QLException 执行异常
     */
    public static Object execute(String script, Map<String, Object> context, QLOptions qlOptions) throws QLException {
        return runner.execute(script, context, qlOptions).getResult();
    }

}
