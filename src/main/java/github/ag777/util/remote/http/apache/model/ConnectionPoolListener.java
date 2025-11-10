package github.ag777.util.remote.http.apache.model;

import github.ag777.util.remote.http.apache.SimpleHttpConnectionPool;

/**
 * 连接池监听器接口
 * 用于监听连接池中的连接生命周期事件
 */
public interface ConnectionPoolListener {

    /**
     * 连接创建事件
     * 当连接池创建新连接时调用
     *
     * @param pool 连接池实例
     * @param connection 新创建的连接
     */
    void onConnectionCreated(SimpleHttpConnectionPool pool, PooledConnection connection);

    /**
     * 连接销毁事件
     * 当连接被销毁时调用
     *
     * @param pool 连接池实例
     * @param connection 被销毁的连接
     * @param reason 销毁原因描述
     */
    void onConnectionDestroyed(SimpleHttpConnectionPool pool, PooledConnection connection, String reason);

    /**
     * 连接借用事件
     * 当连接被借用时调用
     *
     * @param pool 连接池实例
     * @param connection 被借用的连接
     */
    default void onConnectionBorrowed(SimpleHttpConnectionPool pool, PooledConnection connection) {
        // 默认空实现，子类可以选择实现
    }

    /**
     * 连接归还事件
     * 当连接被归还时调用
     *
     * @param pool 连接池实例
     * @param connection 被归还的连接
     */
    default void onConnectionReturned(SimpleHttpConnectionPool pool, PooledConnection connection) {
        // 默认空实现，子类可以选择实现
    }
}
