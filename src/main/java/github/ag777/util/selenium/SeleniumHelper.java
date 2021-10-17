package github.ag777.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.function.Function;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 对selenium的二次封装
 * @Date 2021/10/13 15:39
 */
public class SeleniumHelper implements AutoCloseable{
    private WebDriver driver;

    public SeleniumHelper(WebDriver driver) {
        this.driver = driver;
    }

    public static SeleniumHelper chrome(String driverPath) {
        WebDriver driver = SeleniumUtils.getChromeDriver(driverPath, null);
        return new SeleniumHelper(driver);
    }

    public static SeleniumHelper chrome(String driverPath, ChromeOptions options) {
        WebDriver driver = SeleniumUtils.getChromeDriver(driverPath, options,null);
        return new SeleniumHelper(driver);
    }



    public WebDriver getDriver() {
        return driver;
    }

    public SeleniumHelper get(String url) {
        driver.get(url);
        return this;
    }

    /**
     * 寻找元素,带超时
     * @param cssQuery cssQuery
     * @param timeoutMills 超时时间(毫秒)
     * @return 页面元素
     */
    public  WebElement select(String cssQuery, int timeoutMills) {
        return SeleniumUtils.select(driver, cssQuery, timeoutMills);
    }

    /**
     * 执行方法并设置超时
     * @param timeoutMills 超时时间(毫秒)
     * @param isTrue 要执行的方法
     * @param <V> V
     * @return V
     */
    public <V>V exec(int timeoutMills, Function<WebDriver, V> isTrue) {
        return SeleniumUtils.exec(driver, timeoutMills, isTrue);
    }

    /**
     * 设置全局超时时间
     * @param globalTimeoutMills 全局超时时间
     * @return SeleniumHelper
     */
    public SeleniumHelper setGlobalTimeout(int globalTimeoutMills) {
        SeleniumUtils.setGlobalTimeout(driver, globalTimeoutMills);
        return this;
    }

    /**
     * 最大化窗口展示
     * @return SeleniumHelper
     */
    public SeleniumHelper maximize() {
        SeleniumUtils.maximize(driver);
        return this;
    }

    @Override
    public void close() {
        if (driver != null) {
            synchronized (SeleniumHelper.class) {
                if (driver != null) {
                    driver.quit();
                    driver = null;
                }
            }
        }
    }
}
