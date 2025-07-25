package github.ag777.util.script.ql.express.custom;

import com.alibaba.qlexpress4.runtime.Parameters;
import com.alibaba.qlexpress4.runtime.QContext;
import com.alibaba.qlexpress4.runtime.function.CustomFunction;
import github.ag777.util.lang.ObjectUtils;

import java.util.Date;

/**
 * 自定义ql函数基类
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/7/26 上午8:58
 */
public abstract class BaseQlFunction<T> implements CustomFunction {

    /**
     * 执行函数
     * @param qContext 上下文
     * @param parameters 参数
     * @return 执行结果
     * @throws Throwable 执行异常
     */
    @Override
    public Object call(QContext qContext, Parameters parameters) throws Throwable {
        return execute(qContext, parameters);
    }

    /**
     * 获取函数名称
     * @return 函数名称
     */
    public abstract String getFunctionName();

    /**
     * 执行函数
     * @param qContext 上下文
     * @param parameters 参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public abstract T execute(QContext qContext, Parameters parameters) throws Exception;

    /**
     * 获取字符串
     * @param parameters 参数
     * @param index 索引
     * @return 字符串
     */
    public String getStr(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toStr(value);
    }

    /**
     * 获取字符串
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 字符串
     */
    public String getStr(Parameters parameters, int index, String defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toStr(value, defaultValue);
    }

    /**
     * 获取整数
     * @param parameters 参数
     * @param index 索引
     * @return 整数
     */
    public Integer getInt(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toInt(value);
    }

    /**
     * 获取整数
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 整数
     */
    public Integer getInt(Parameters parameters, int index, Integer defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toInt(value, defaultValue);
    }

    /**
     * 获取长整数
     * @param parameters 参数
     * @param index 索引
     * @return 长整数
     */
    public Long getLong(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toLong(value);
    }

    /**
     * 获取长整数
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 长整数
     */
    public Long getLong(Parameters parameters, int index, Long defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toLong(value, defaultValue);
    }

    /**
     * 获取浮点数
     * @param parameters 参数
     * @param index 索引
     * @return 浮点数
     */
    public Float getFloat(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toFloat(value);
    }

    /**
     * 获取浮点数
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 浮点数
     */
    public Float getFloat(Parameters parameters, int index, Float defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toFloat(value, defaultValue);
    }

    /**
     * 获取双精度浮点数
     * @param parameters 参数
     * @param index 索引
     * @return 双精度浮点数
     */
    public Double getDouble(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toDouble(value);
    }

    /**
     * 获取双精度浮点数
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 双精度浮点数
     */
    public Double getDouble(Parameters parameters, int index, Double defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toDouble(value, defaultValue);
    }

    /**
     * 获取布尔值
     * @param parameters 参数
     * @param index 索引
     * @return 布尔值
     */
    public Boolean getBool(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toBoolean(value);
    }

    /**
     * 获取布尔值
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public Boolean getBool(Parameters parameters, int index, Boolean defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toBoolean(value, defaultValue);
    }

    /**
     * 获取日期
     * @param parameters 参数
     * @param index 索引
     * @return 日期
     */
    public Date getDate(Parameters parameters, int index) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toDate(value);
    }

    /**
     * 获取日期
     * @param parameters 参数
     * @param index 索引
     * @param defaultValue 默认值
     * @return 日期
     */
    public Date getDate(Parameters parameters, int index, Date defaultValue) {
        Object value = parameters.getValue(index);
        return ObjectUtils.toDate(value, defaultValue);
    }

}
