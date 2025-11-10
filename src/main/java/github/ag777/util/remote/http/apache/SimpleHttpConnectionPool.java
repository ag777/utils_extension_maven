package github.ag777.util.remote.http.apache;

import github.ag777.util.remote.http.apache.model.PoolStatus;
import github.ag777.util.remote.http.apache.model.PooledConnection;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单的定长HTTP连接池
 * 支持轮询使用连接：从队列头部获取，从队列尾部归还
 */
public class SimpleHttpConnectionPool {
    private final BlockingQueue<PooledConnection> connectionQueue;
    private final int poolSize;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final ReentrantLock poolLock = new ReentrantLock();

    // 默认超时配置
    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;

    // 代理配置
    private volatile HttpHost proxyHost;

    // 请求配置构建器
    private final RequestConfig.Builder requestConfigBuilder;

    /**
     * 构造函数
     * @param poolSize 连接池大小
     */
    public SimpleHttpConnectionPool(int poolSize) {
        this(poolSize, null, 10, TimeUnit.SECONDS);
    }

    /**
     * 构造函数（支持代理和请求配置）
     * @param poolSize 连接池大小
     * @param requestConfigBuilder 请求配置构建器，null表示使用默认配置
     */
    public SimpleHttpConnectionPool(int poolSize, RequestConfig.Builder requestConfigBuilder) {
        this(poolSize, requestConfigBuilder, 10, TimeUnit.SECONDS);
    }

    /**
     * 构造函数（支持代理、请求配置和超时设置）
     * @param poolSize 连接池大小
     * @param requestConfigBuilder 请求配置构建器，null表示使用默认配置
     * @param defaultTimeout 默认超时时间
     * @param defaultTimeUnit 默认超时时间单位
     */
    public SimpleHttpConnectionPool(int poolSize, RequestConfig.Builder requestConfigBuilder, long defaultTimeout, TimeUnit defaultTimeUnit) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("连接池大小必须大于0");
        }
        if (defaultTimeout <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }

        this.poolSize = poolSize;
        this.requestConfigBuilder = requestConfigBuilder != null ? requestConfigBuilder : createDefaultRequestConfigBuilder();
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeUnit = defaultTimeUnit;
        this.connectionQueue = new ArrayBlockingQueue<>(poolSize);
        this.connectionManager = new PoolingHttpClientConnectionManager();

        initializePool();
    }

    /**
     * 设置代理服务器
     * @param proxyHost 代理服务器，null表示不使用代理
     */
    public SimpleHttpConnectionPool setProxy(HttpHost proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }


    /**
     * 设置代理
     * @param host Clash代理主机
     * @param port Clash代理端口
     */
    public SimpleHttpConnectionPool setProxy(String host, int port) {
        setProxy(new HttpHost(host, port));
        return this;
    }

    /**
     * 设置代理
     * @param port Clash代理端口
     */
    public SimpleHttpConnectionPool setProxy(int port) {
        setProxy("127.0.0.1", port);
        return this;
    }

    /**
     * 设置Clash代理（默认端口10801）
     */
    public SimpleHttpConnectionPool setClashProxy() {
        setProxy(10801);
        return this;
    }

    /**
     * 设置Clash代理
     * @param port Clash代理端口
     */
    public SimpleHttpConnectionPool setClashProxy(int port) {
        setProxy(port);
        return this;
    }

    /**
     * 初始化连接池
     */
    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            addNewConnectionToPool();
        }
    }

    /**
     * 创建新连接并添加到池中
     */
    private void addNewConnectionToPool() {
        PooledConnection newConnection = null;
        try {
            var clientBuilder = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setConnectionManagerShared(true)
                    .setDefaultRequestConfig(createRequestConfig());

            // 如果设置了代理，则添加路由规划器
            if (proxyHost != null) {
                DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                clientBuilder.setRoutePlanner(routePlanner);
            }

            CloseableHttpClient httpClient = clientBuilder.build();
            newConnection = new PooledConnection(httpClient, this);

            // 尝试将连接添加到队列
            if (connectionQueue.offer(newConnection)) {
                activeConnections.incrementAndGet();
            } else {
                // 队列已满，销毁刚创建的连接
                newConnection.destroy();
            }
        } catch (Exception e) {
            // 创建连接失败，清理资源
            if (newConnection != null) {
                newConnection.destroy();
            }
        }
    }

    /**
     * 创建默认请求配置构建器
     */
    private RequestConfig.Builder createDefaultRequestConfigBuilder() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(10000))
                .setResponseTimeout(Timeout.ofMilliseconds(30000));
    }

    /**
     * 创建请求配置（包含代理设置）
     */
    private RequestConfig createRequestConfig() {
        return requestConfigBuilder.build();
    }

    /**
     * 从连接池借用连接（从队列头部获取），使用默认超时时间
     * 这个方法是线程安全的
     *
     * @return 连接对象，使用完后必须调用close()方法归还
     * @throws InterruptedException 如果等待连接时被中断
     */
    public PooledConnection borrowConnection() throws InterruptedException {
        return borrowConnection(defaultTimeout, defaultTimeUnit);
    }

    /**
     * 从连接池借用连接（从队列头部获取），指定超时时间
     * 这个方法是线程安全的
     *
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 连接对象，使用完后必须调用close()方法归还，null表示超时
     * @throws InterruptedException 如果等待连接时被中断
     */
    public PooledConnection borrowConnection(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long remainingTimeout = unit.toNanos(timeout);

        poolLock.lock();
        try {
            while (remainingTimeout > 0) {
                // 如果队列为空但未达到最大连接数，创建新连接
                if (connectionQueue.isEmpty() && activeConnections.get() < poolSize) {
                    addNewConnectionToPool();
                }

                // 等待获取连接，使用剩余超时时间
                long currentTimeout = Math.max(remainingTimeout, TimeUnit.MILLISECONDS.toNanos(100)); // 最少100ms
                PooledConnection connection = connectionQueue.poll(currentTimeout, TimeUnit.NANOSECONDS);

                if (connection != null) {
                    // 检查连接是否仍然有效
                    if (!connection.isValid()) {
                        // 连接无效，销毁并减少计数，然后继续循环获取新连接
                        activeConnections.decrementAndGet();
                        connection.destroy();
                    } else {
                        // 连接有效，返回
                        return connection;
                    }
                } else {
                    // 获取连接超时，返回null
                    return null;
                }

                // 计算剩余超时时间
                long elapsed = System.nanoTime() - startTime;
                remainingTimeout -= elapsed;
                startTime = System.nanoTime(); // 重置开始时间
            }

            // 所有重试都超时，返回null
            return null;
        } finally {
            poolLock.unlock();
        }
    }

    /**
     * 将连接返回连接池（放回队列尾部，内部方法，由PooledConnection.close()调用）
     */
    public void returnConnection(PooledConnection connection) {
        if (connection == null) {
            return;
        }

        // 在归还前测试连接是否仍然有效
        boolean isValid = connection.isValid();

        poolLock.lock();
        try {
            if (!isValid) {
                // 连接无效，销毁并减少计数
                connection.destroy();
                activeConnections.decrementAndGet();
                return;
            }

            connection.setReturned(false); // 重置标志
            // 如果队列已满，销毁这个连接
            if (!connectionQueue.offer(connection)) {
                connection.destroy();
                activeConnections.decrementAndGet();
            }
        } finally {
            poolLock.unlock();
        }
    }

    /**
     * 销毁连接并创建新连接替换
     * 这个方法是线程安全的
     */
    public void replaceConnection(PooledConnection connection) {
        if (connection != null) {
            connection.markInvalid();
            connection.destroy();
            activeConnections.decrementAndGet();

            poolLock.lock();
            try {
                addNewConnectionToPool();
            } finally {
                poolLock.unlock();
            }
        }
    }

    /**
     * 获取连接池状态信息
     */
    public PoolStatus getPoolStatus() {
        poolLock.lock();
        try {
            return new PoolStatus(
                    activeConnections.get(),
                    connectionQueue.size(),
                    poolSize
            );
        } finally {
            poolLock.unlock();
        }
    }

    /**
     * 清理并关闭连接池
     */
    public void shutdown() {
        poolLock.lock();
        try {
            PooledConnection connection;
            while ((connection = connectionQueue.poll()) != null) {
                connection.destroy();
                activeConnections.decrementAndGet();
            }

            connectionManager.close();
        } catch (Exception e) {
            // 忽略关闭时的异常
        } finally {
            poolLock.unlock();
        }
    }
}
