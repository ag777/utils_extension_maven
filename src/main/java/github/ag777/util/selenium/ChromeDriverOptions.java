package github.ag777.util.selenium;

import com.ag777.util.lang.collection.MapUtils;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * @author ag777＜ag777@vip.qq.com＞
 * @Date 2021/10/17 18:05
 */
public class ChromeDriverOptions {

    private ChromeDriverOptions() {}

    /**
     *
     * @return 不带图片的配置
     */
    public static ChromeOptions noPic() {
        ChromeOptions options= new ChromeOptions();
        options.addArguments("--test-type --no-sandbox");
        options.addArguments("--enable-strict-powerful-feature-restrictions");

        options.setExperimentalOption("prefs",  MapUtils.of(
            "profile.default_content_setting_values", MapUtils.of(
                    "images", 2
            )
        ));

        return options;
    }
}
