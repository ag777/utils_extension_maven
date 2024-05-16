package github.ag777.util.script.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * groovy二次封装,适合执行脚本文件
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/9/13 9:23
 */
public class GroovyHelper {
    private final GroovyScriptEngine engine;
    public GroovyHelper(GroovyScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * 创建一个新的GroovyHelper实例。
     * @param baseDir Groovy脚本的基础目录路径。
     * @return 返回一个初始化好的GroovyHelper实例。
     * @throws IOException 如果在初始化过程中读取目录失败。
     */
    public static GroovyHelper newInstance(String baseDir) throws IOException {
        GroovyScriptEngine engine = new GroovyScriptEngine(baseDir);
        return new GroovyHelper(engine);
    }

    /**
     * 创建一个新的GroovyHelper实例，并通过指定的包名添加导入。
     *
     * @param baseDir Groovy脚本的基础目录路径。
     * @param packageNames 要添加星号导入的包名数组。
     * @return 返回一个配置了指定导入的GroovyHelper新实例。
     * @throws IOException 如果在实例创建过程中遇到IO异常。
     */
    public static GroovyHelper newInstanceWithImports(String baseDir, String... packageNames) throws IOException {
        // 配置编译器以添加星号导入
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new org.codehaus.groovy.control.customizers.ImportCustomizer().addStarImports(packageNames));
        return newInstance(baseDir, config); // 创建并返回配置了导入的GroovyHelper实例
    }

    /**
     * 创建一个新的GroovyHelper实例，允许通过CompilerConfiguration自定义编译器配置。
     * @param baseDir Groovy脚本的基础目录路径。
     * @param config 编译器配置，允许自定义Groovy编译选项。
     * @return 返回一个初始化好的GroovyHelper实例。
     * @throws IOException 如果在初始化过程中读取目录失败。
     */
    public static GroovyHelper newInstance(String baseDir, CompilerConfiguration config) throws IOException {
        GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
        return newInstance(baseDir, classLoader);
    }

    /**
     * 创建一个新的GroovyHelper实例，允许通过自定义的GroovyClassLoader来加载类。
     * @param baseDir Groovy脚本的基础目录路径。
     * @param classLoader 自定义的Groovy类加载器。
     * @return 返回一个初始化好的GroovyHelper实例。
     * @throws IOException 如果在初始化过程中读取目录失败。
     */
    public static GroovyHelper newInstance(String baseDir, GroovyClassLoader classLoader) throws IOException {
        GroovyScriptEngine engine = new GroovyScriptEngine(baseDir, classLoader);
        return new GroovyHelper(engine);
    }

    /**
     * 执行指定脚本中的方法。
     * @param scriptName 脚本名称，指定要执行的Groovy脚本文件的名称。
     * @param methodName 要调用的方法名。
     * @param params 方法参数，可变参数，传入方法调用所需的参数。
     * @return 执行结果，返回类型为泛型T，由具体执行的方法决定。
     * @throws ScriptException 如果脚本执行出错。
     * @throws ResourceException 如果资源访问出错。
     * @throws InstantiationException 如果实例化异常发生。
     * @throws IllegalAccessException 如果访问权限不足。
     * @throws ClassNotFoundException 如果指定的类找不到。
     */
    public <T>T exec(String scriptName, String methodName, Object... params) throws ScriptException, ResourceException, InstantiationException, IllegalAccessException {
        return GroovyUtils.exec(engine, scriptName, methodName, params);
    }

    /**
     * 获取脚本中所有方法及其参数列表。
     * @param scriptName 脚本名称，指定要查询方法的Groovy脚本文件的名称。
     * @return 方法信息列表，返回一个包含方法名和参数列表的MethodInfo对象的列表。
     * @throws ClassNotFoundException 如果指定的类找不到。
     */
    public List<GroovyUtils.MethodInfo> getMethodsAndParameters(String scriptName) throws ClassNotFoundException {
        return GroovyUtils.getMethodsAndParameters(engine, scriptName);
    }

    /**
     * 使用Map形式的参数执行指定脚本中的方法。
     * @param scriptName 脚本名称，指定要执行的Groovy脚本文件的名称。
     * @param params 方法参数，以Map形式传入，键为参数名，值为参数值。
     * @return 执行结果，返回类型为泛型T，由具体执行的方法决定。
     * @throws ScriptException 如果脚本执行出错。
     * @throws ResourceException 如果资源访问出错。
     */
    public <T>T exec(String scriptName, Map<String, Object> params) throws ResourceException, ScriptException {
        return GroovyUtils.exec(engine, scriptName, params);
    }

    public static void main(String[] args) throws IOException, ScriptException, ResourceException, InstantiationException, IllegalAccessException {
        GroovyHelper helper = newInstance("D:\\temp\\程序测试\\");
        int result = helper.exec("test.groovy", "cal", 1, 2);
        System.out.println(result);
    }

}
