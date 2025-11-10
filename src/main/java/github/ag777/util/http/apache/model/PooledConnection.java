package github.ag777.util.http.apache.model;

import github.ag777.util.http.apache.SimpleHttpConnectionPool;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.IOException;

/**
 * 连接包装类
 */
public class PooledConnection implements AutoCloseable {
    @Getter
    private final CloseableHttpClient httpClient;
    private final long createTime;
    @Getter
    private volatile boolean valid = true;
    @Setter
    private volatile boolean returned = false;
    private final SimpleHttpConnectionPool pool;

    public PooledConnection(CloseableHttpClient httpClient, SimpleHttpConnectionPool pool) {
        this.httpClient = httpClient;
        this.createTime = System.currentTimeMillis();
        this.pool = pool;
    }

    public void markInvalid() {
        this.valid = false;
    }

    /**
     * 将连接归还给连接池（放回队列尾部）
     */
    @Override
    public void close() {
        if (!returned) {
            pool.returnConnection(this);
            returned = true;
        }
    }

    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // 忽略关闭异常
        }
        valid = false;
    }
}
