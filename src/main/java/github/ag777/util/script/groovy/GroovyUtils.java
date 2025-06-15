package github.ag777.util.script.groovy;

import github.ag777.util.lang.collection.MapUtils;
import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
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
     * 获取指定 Groovy 脚本中所有方法的信息。
     * <p>
     * 该方法使用 GroovyScriptEngine 加载指定的 Groovy 脚本，并提取其中定义的所有方法。
     * 对于每个方法，它将收集方法的名称以及其参数的详细信息（包括类型和名称）。
     * <p>
     * 注意：此方法不处理 Groovy 生成的合成方法。
     *
     * @param engine GroovyScriptEngine 实例，用于加载和解析 Groovy 脚本。
     * @param scriptName 要加载的 Groovy 脚本的名称。
     * @return 包含方法信息的 MethodInfo 对象列表。
     * @throws ClassNotFoundException 如果无法找到或加载指定的脚本类。
     */
    public static List<MethodInfo> getMethodsAndParameters(GroovyScriptEngine engine, String scriptName) throws ClassNotFoundException {
        List<MethodInfo> methodsList = new ArrayList<>();

        // 使用 GroovyClassLoader 从引擎中加载 Groovy 脚本类
        Class<?> scriptClass = engine.getGroovyClassLoader().loadClass(scriptName);

        // 获取 Groovy 脚本中声明的所有方法
        Method[] methods = scriptClass.getDeclaredMethods();

        // 遍历所有方法
        for (Method method : methods) {
            // 跳过 Groovy 生成的合成方法和 main 方法
            if (!method.isSynthetic() && !isMainMethod(method)) {
                MethodInfo methodInfo = new MethodInfo();
                List<ParameterInfo> paramsList = new ArrayList<>();

                // 获取每个方法的参数
                for (Parameter parameter : method.getParameters()) {
                    ParameterInfo paramInfo = new ParameterInfo();
                    // 将参数类型和名称设置到参数信息对象中
                    paramInfo.setType(parameter.getType().getSimpleName());
                    paramInfo.setName(parameter.getName());
                    // 将参数信息对象添加到参数列表中
                    paramsList.add(paramInfo);
                }

                // 设置方法名和参数列表到方法信息对象中
                methodInfo.setName(method.getName());
                methodInfo.setParams(paramsList);

                // 将方法信息对象添加到方法列表中
                methodsList.add(methodInfo);
            }
        }

        return methodsList;
    }

    /**
     * 检查方法是否为 main 方法
     */
    private static boolean isMainMethod(Method method) {
        int modifiers = method.getModifiers();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) &&
                "main".equals(method.getName()) &&
                parameterTypes.length == 1 &&
                parameterTypes[0] == String[].class;
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

    @Data
    public static class MethodInfo {
        private String name;
        private List<ParameterInfo> params;
    }

    @Data
    public static class ParameterInfo {
        private String type;
        private String name;
    }
}
