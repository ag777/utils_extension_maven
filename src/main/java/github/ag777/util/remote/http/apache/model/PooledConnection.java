package github.ag777.util.remote.http.apache.model;

import github.ag777.util.remote.http.apache.SimpleHttpConnectionPool;
import lombok.Getter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 连接包装类
 */
public class PooledConnection implements AutoCloseable {
    @Getter
    private final CloseableHttpClient httpClient;
    private final long createTime;
    @Getter
    private volatile boolean valid = true;
    private final AtomicBoolean returned = new AtomicBoolean(true);
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
     * 标记：该连接被借出，等待归还
     */
    public void markBorrowed() {
        returned.set(false);
    }

    /**
     * 将连接归还给连接池（放回队列尾部）
     */
    @Override
    public void close() {
        // 仅在“借出未归还”时归还一次
        if (returned.compareAndSet(false, true)) {
            pool.returnConnection(this);
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
