package github.ag777.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * 对selenium的二次封装
 * chrome: <a href="http://npm.taobao.org/mirrors/chromedriver/">chrome</a>
 * <a href="https://selenium-release.storage.googleapis.com/index.html">ie</a>
 * @author ag777 <837915770@vip.qq.com>
 * @version  2024/09/24 10:49
 */
public class SeleniumUtils {

    private static final String PROPERTY_KEY_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String PROPERTY_KEY_IE_DRIVER = "webdriver.ie.driver";

    public SeleniumUtils() {}

    /**
     *
     * @param driverPath chromedriver.exe路径
     * @param globalTimeoutMills 全局超时时间
     * @return 谷歌浏览器驱动
     */
    public static WebDriver getChromeDriver(String driverPath, Integer globalTimeoutMills) {
        return getChromeDriver(driverPath, null, globalTimeoutMills);
    }

    /**
     * 获取ie浏览器驱动
     *
     * @param driverPath         IEDriverServer.exe路径
     * @param options            配置
     * @param globalTimeoutMills 全局超时时间
     * @return 谷歌浏览器驱动
     */
    public static WebDriver getIeDriver(String driverPath, InternetExplorerOptions options, Integer globalTimeoutMills) {
        System.setProperty(PROPERTY_KEY_IE_DRIVER, driverPath);
        WebDriver driver;
        if (options != null) {
            driver = new InternetExplorerDriver(options);
        } else {
            driver = new InternetExplorerDriver();
        }

        // 设置全局超时
        setGlobalTimeout(driver, globalTimeoutMills);
        return driver;
    }

    /**
     *
     * @return ie配置举例
     */
    public InternetExplorerOptions getExampleIeOptions() {
        InternetExplorerOptions options = new InternetExplorerOptions();
        options.ignoreZoomSettings(); // 忽略缩放设置
        options.destructivelyEnsureCleanSession(); // 每次启动时清除缓存和 cookies
        options.requireWindowFocus(); // 启动时获得焦点
        options.enablePersistentHovering(); // 启用持久悬停
        // 忽略安全区域设置
        options.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        // 忽略缩放设置
        options.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
        return options;
    }

    /**
     * 获取chrome浏览器驱动
     * @param driverPath chromedriver.exe路径
     * @param options 配置
     * @param globalTimeoutMills 全局超时时间
     * @return 谷歌浏览器驱动
     */
    public static WebDriver getChromeDriver(String driverPath, ChromeOptions options, Integer globalTimeoutMills) {
        System.setProperty(PROPERTY_KEY_CHROME_DRIVER,
                driverPath);
        ChromeDriver driver = null;
        if (options != null) {
            driver = new ChromeDriver(options);
        } else {
            driver = new ChromeDriver();
        }

        // 最大化
//        maximize(driver);
        // 设置全局超时
        setGlobalTimeout(driver, globalTimeoutMills);
        return driver;
    }

    /**
     * 设置全局超时时间
     * @param driver 驱动
     * @param globalTimeoutMills 全局超时时间
     */
    public static void setGlobalTimeout(WebDriver driver, Integer globalTimeoutMills) {
        if (globalTimeoutMills != null) {
            //设置隐性等待
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(globalTimeoutMills));
        }
    }

    /**
     * 最大化窗口
     * @param driver 驱动
     */
    public static void maximize(WebDriver driver) {
        driver.manage().window().maximize();
    }

    /**
     *
     * @param context 页面元素
     * @param cssQuery cssQuery
     * @return 页面元素
     */
    public static WebElement select(SearchContext context, String cssQuery) {
        return context.findElement(By.cssSelector(cssQuery));
    }

    /**
     *
     * @param context 页面元素
     * @param cssQuery cssQuery
     * @return 页面元素列表
     */
    public static List<WebElement> selectAll(SearchContext context, String cssQuery) {
        return context.findElements(By.cssSelector(cssQuery));
    }

    /**
     * 寻找元素,带超时
     * @param driver 驱动
     * @param cssQuery cssQuery
     * @param timeoutMills 超时时间(毫秒)
     * @return 页面元素
     */
    public static WebElement select(WebDriver driver, String cssQuery, int timeoutMills) {
        return exec(driver, timeoutMills, d -> d.findElement(By.cssSelector(cssQuery)));
    }

    /**
     * 查找所有元素
     * @param driver 驱动
     * @param cssQuery cssQuery
     * @param timeoutMills 超时时间(毫秒)
     * @return 页面元素列表
     */
    public static List<WebElement> selectAll(WebDriver driver, String cssQuery, int timeoutMills) {
        return exec(driver, timeoutMills, d -> d.findElements(By.cssSelector(cssQuery)));
    }

    /**
     * 执行方法并设置超时
     * @param driver 驱动
     * @param timeoutMills 超时时间(毫秒)
     * @param isTrue 要执行的方法
     * @param <V> V
     * @return V
     */
    public static <V>V exec(WebDriver driver, int timeoutMills, Function<WebDriver, V> isTrue) {
        return new WebDriverWait( driver, Duration.ofMillis(timeoutMills)).until(isTrue);
    }

}
