package github.ag777.util.script.groovy;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
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

    public static GroovyHelper newInstance(String baseDir) throws IOException {
        GroovyScriptEngine engine = new GroovyScriptEngine(baseDir);
        return new GroovyHelper(engine);
    }

    public <T>T exec(String scriptName, String methodName, Object... params) throws ScriptException, ResourceException, InstantiationException, IllegalAccessException {
        return GroovyUtils.exec(engine, scriptName, methodName, params);
    }

    public <T>T exec(String scriptName, Map<String, Object> params) throws ResourceException, ScriptException {
        return GroovyUtils.exec(engine, scriptName, params);
    }

    public static void main(String[] args) throws IOException, ScriptException, ResourceException, InstantiationException, IllegalAccessException {
        GroovyHelper helper = newInstance("D:\\temp\\程序测试\\");
        int result = helper.exec("test.groovy", "cal", 1, 2);
        System.out.println(result);
    }

}
