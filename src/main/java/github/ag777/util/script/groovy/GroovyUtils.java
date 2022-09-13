package github.ag777.util.script.groovy;

import com.ag777.util.lang.collection.MapUtils;
import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.util.Map;

/**
 * groovy二次封装, 适合执行脚本字符串
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/9/13 9:45
 */
public class GroovyUtils {

    /**
     * 执行脚本字符串
     * @param content 脚本内容
     * @param params 参数
     * @param <T> 返回值类型
     * @return 返回值
     * @throws GroovyRuntimeException 脚本执行异常
     */
    @SuppressWarnings("unchecked")
    public static <T>T exec(String content, Map<String, Object> params) throws GroovyRuntimeException {
        GroovyShell sh = new GroovyShell();
        if (!MapUtils.isEmpty(params)) {
            for (String key : params.keySet()) {
                sh.setVariable(key, params.get(key));
            }
        }
        return (T) sh.evaluate(content);
    }

    /**
     * 
     * @param groovyShell 引擎
     * @param content 脚本字符串
     * @return 脚本
     */
    public static Script getScript(GroovyShell groovyShell, String content) {
        return groovyShell.parse(content);
    }

    /**
     * 执行脚本
     * @param script 脚本
     * @param params 参数
     * @param <T> 返回值类型
     * @return 返回值
     * @throws GroovyRuntimeException 脚本执行异常
     */
    @SuppressWarnings("unchecked")
    public static <T>T exec(Script script, Map<String, Object> params) throws GroovyRuntimeException {
        Binding binding = getBinding(params);
        script.setBinding(binding);
        return (T) script.run();
    }

    /**
     * 执行脚本文件(指定方法)
     * @param engine 引擎
     * @param scriptName 脚本名
     * @param methodName 方法名
     * @param params 参数
     * @param <T> 返回值类型
     * @return 返回值
     * @throws ScriptException 脚本异常
     * @throws ResourceException 脚本文件读取异常
     * @throws InstantiationException 脚本解析异常
     * @throws IllegalAccessException 参数异常
     */
    @SuppressWarnings("unchecked")
    public static <T>T exec(GroovyScriptEngine engine, String scriptName, String methodName, Object... params) throws ScriptException, ResourceException, InstantiationException, IllegalAccessException {
        Class<?> scriptClass = engine.loadScriptByName(scriptName);
        GroovyObject scriptInstance = (GroovyObject)scriptClass.newInstance();
        return (T) scriptInstance.invokeMethod(methodName, params);
    }

    /**
     * 执行脚本文件(执行默认方法)
     * @param engine 引擎
     * @param scriptName 脚本名
     * @param params 参数
     * @param <T> 返回值类型
     * @return 返回值
     * @throws ResourceException 脚本文件读取异常
     * @throws ScriptException 脚本异常
     */
    @SuppressWarnings("unchecked")
    public static <T>T exec(GroovyScriptEngine engine, String scriptName, Map<String, Object> params) throws ResourceException, ScriptException {
        Binding binding = getBinding(params);
        return (T) engine.run(scriptName, binding);
    }

    /**
     * @param params 参数
     * @return Binding
     */
    public static Binding getBinding(Map<String, Object> params) {
        Binding binding = new Binding();
        if (!MapUtils.isEmpty(params)) {
            for (String key : params.keySet()) {
                binding.setVariable(key, params.get(key));
            }
        }
        return binding;
    }

    public static void main(String[] args) {
        String text = "c=a+b;\n" +
                "println c";
        exec(
                text,
                MapUtils.of("a", 1, "b", 2)
        );
        GroovyShell gs = new GroovyShell();
        Script script = getScript(gs, text);
        exec(script, MapUtils.of("a", 1, "b", 2));
        exec(script, MapUtils.of("a", 3));
    }
}
