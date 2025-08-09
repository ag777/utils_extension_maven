package github.ag777.util.software.oof;

import github.ag777.util.http.HttpHelper;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/9 下午4:04
 */
public class OofUtil {
    private final String cookie;
    private final HttpHelper http;

    public OofUtil(HttpHelper http, String cookie) {
        this.cookie = cookie;
        this.http = http;
    }

    public OofPathUtil path() {
        return new OofPathUtil(http, cookie);
    }

    public OofFileUtil file() {
        return new OofFileUtil(http, cookie);
    }
}
