package github.ag777.util.remote.http.apache;

import github.ag777.util.remote.http.apache.model.ConnectionPoolListener;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单的定长HTTP连接池
 * 支持轮询使用连接：从队列头部获取，从队列尾部归还
 */
public class SimpleHttpConnectionPool {
    private final BlockingQueue<PooledConnection> connectionQueue;
    private final int poolSize;
    private final PoolingHttpClientConnectionManager connectionManager;
//    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final ReentrantLock poolLock = new ReentrantLock();

    // 默认超时配置
    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;

    // 代理配置
    private volatile HttpHost proxyHost;

    // 请求配置构建器
    private final RequestConfig.Builder requestConfigBuilder;

    // 连接池监听器
    private volatile ConnectionPoolListener listener;

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
     * 设置连接池监听器
     * @param listener 监听器实例，null表示移除监听器
     */
    public SimpleHttpConnectionPool setListener(ConnectionPoolListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 获取当前监听器
     * @return 当前监听器实例，可能为null
     */
    public ConnectionPoolListener getListener() {
        return listener;
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
                // 连接创建成功，通知监听器
                if (listener != null) {
                    listener.onConnectionCreated(this, newConnection);
                }
            } else {
                // 队列已满，销毁刚创建的连接
                newConnection.destroy();
                // 通知监听器连接被销毁
                if (listener != null) {
                    listener.onConnectionDestroyed(this, newConnection, "队列已满，连接被丢弃");
                }
            }
        } catch (Exception e) {
            // 创建连接失败，清理资源
            if (newConnection != null) {
                newConnection.destroy();
                // 通知监听器连接创建失败被销毁
                if (listener != null) {
                    listener.onConnectionDestroyed(this, newConnection, "连接创建失败: " + e.getMessage());
                }
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

        while (remainingTimeout > 0) {
            // 先尝试快速获取连接（非阻塞）
            PooledConnection connection = connectionQueue.poll();

            if (connection != null) {
                // 检查连接是否仍然有效
                if (!connection.isValid()) {
                    // 连接无效，销毁并补充新连接
                    connection.destroy();
                    // 通知监听器连接因无效而被销毁
                    if (listener != null) {
                        listener.onConnectionDestroyed(this, connection, "借用时发现连接无效");
                    }
                    // 补充新连接（加锁保护，避免并发创建多余连接）
                    poolLock.lock();
                    try {
                        // 检查队列大小，如果队列已满或接近满，说明已经有足够的连接，不需要补充
                        if (connectionQueue.size() < poolSize) {
                            addNewConnectionToPool();
                        }
                    } finally {
                        poolLock.unlock();
                    }
                    // 继续循环获取连接
                    // 重新计算剩余时间
                    long elapsed = System.nanoTime() - startTime;
                    remainingTimeout = unit.toNanos(timeout) - elapsed;
                    continue;
                } else {
                    // 标记借出
                    connection.markBorrowed();
                    // 连接有效，通知监听器连接被借用
                    if (listener != null) {
                        listener.onConnectionBorrowed(this, connection);
                    }
                    // 返回连接
                    return connection;
                }
            }

            // 队列为空，等待一段时间后重试
            // 修复：使用 Math.min 而不是 Math.max，确保不会等待超过剩余时间
            long currentTimeout = Math.min(remainingTimeout, TimeUnit.MILLISECONDS.toNanos(100));

            poolLock.lock();
            try {
                // 使用阻塞方式等待连接
                connection = connectionQueue.poll(currentTimeout, TimeUnit.NANOSECONDS);

                if (connection != null) {
                    // 检查连接是否仍然有效
                    if (!connection.isValid()) {
                        // 连接无效，销毁并补充新连接
                        connection.destroy();
                        // 通知监听器连接因无效而被销毁
                        if (listener != null) {
                            listener.onConnectionDestroyed(this, connection, "借用时发现连接无效");
                        }
                        // 补充新连接（已经在锁内，检查队列大小避免创建多余连接）
                        if (connectionQueue.size() < poolSize) {
                            addNewConnectionToPool();
                        }
                        // 继续循环获取连接
                    } else {
                        // 标记借出
                        connection.markBorrowed();
                        // 连接有效，通知监听器连接被借用
                        if (listener != null) {
                            listener.onConnectionBorrowed(this, connection);
                        }
                        // 返回连接
                        return connection;
                    }
                }
                // 如果 connection == null，继续下一次循环
            } finally {
                poolLock.unlock();
            }

            // 计算剩余超时时间（从最初开始计算总的已用时间）
            long elapsed = System.nanoTime() - startTime;
            remainingTimeout = unit.toNanos(timeout) - elapsed;
        }

        // 所有重试都超时，返回null
        return null;
    }

    /**
     * 将连接返回连接池（放回队列尾部，内部方法，由PooledConnection.close()调用）
     */
    public void returnConnection(PooledConnection connection) {
        if (connection == null) {
            return;
        }

        // 无论连接是否有效，都先通知监听器连接被归还（外部主动处理了连接）
        if (listener != null) {
            listener.onConnectionReturned(this, connection);
        }

        // 在归还前测试连接是否仍然有效
        boolean isValid = connection.isValid();

        poolLock.lock();
        try {
            if (!isValid) {
                // 连接无效，销毁并补充新连接
                connection.destroy();
                // 通知监听器连接因无效而被销毁
                if (listener != null) {
                    listener.onConnectionDestroyed(this, connection, "连接无效");
                }
                // 检查队列大小，避免创建多余连接
                if (connectionQueue.size() < poolSize) {
                    addNewConnectionToPool();
                }
                return;
            }

            // 如果队列已满，销毁这个连接
            if (!connectionQueue.offer(connection)) {
                connection.destroy();
                // 通知监听器连接因队列满而被销毁
                if (listener != null) {
                    listener.onConnectionDestroyed(this, connection, "归还时队列已满，连接被销毁");
                }
            }
            // 如果成功归还到队列，不需要额外操作（已经在上面触发了onConnectionReturned）
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
            // 先通知监听器连接被归还（外部主动处理了连接）
            if (listener != null) {
                listener.onConnectionReturned(this, connection);
            }

            connection.markInvalid();
            connection.destroy();
            // 通知监听器连接被替换销毁
            if (listener != null) {
                listener.onConnectionDestroyed(this, connection, "连接被主动替换");
            }

            poolLock.lock();
            try {
                // 检查队列大小，避免创建多余连接
                if (connectionQueue.size() < poolSize) {
                    addNewConnectionToPool();
                }
            } finally {
                poolLock.unlock();
            }
        }
    }

    /**
     * 获取连接池状态信息
     * 注意：activeCount 是基于 poolSize 和 availableCount 的估算值，
     * 如果连接被销毁但还没补充，这个值可能不准确
     */
    public PoolStatus getPoolStatus() {
        poolLock.lock();
        try {
            // availableConnections: 队列中可用的连接数
            int availableCount = connectionQueue.size();
            // activeConnections: 当前被借出使用的连接数 = 总连接数 - 队列中可用连接数
            // 注意：这是估算值，实际值可能因为连接被销毁但还没补充而略有偏差
            int activeCount = Math.max(0, poolSize - availableCount);
            return new PoolStatus(
                    activeCount,        // 活跃连接数（被借出的，估算值）
                    availableCount,     // 可用连接数（队列中的）
                    poolSize           // 连接池目标大小
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
                // 通知监听器连接因连接池关闭而被销毁
                if (listener != null) {
                    listener.onConnectionDestroyed(this, connection, "连接池关闭");
                }
            }

            connectionManager.close();
        } catch (Exception e) {
            // 忽略关闭时的异常
        } finally {
            poolLock.unlock();
        }
    }
}
