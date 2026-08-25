package github.ag777.util.lang.kryo;

import github.ag777.util.lang.thread.KeyReadWriteLock;

import java.io.*;
import java.util.*;

/**
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/8/24 16:58
 */
public class KryoFileUtils {
    // 按文件路径加读写锁：读可并发，写与读写互斥
    private static volatile KeyReadWriteLock<String> lock;

    private static KeyReadWriteLock<String> lock() {
        if (lock == null) {
            synchronized (KryoFileUtils.class) {
                if (lock == null) {
                    lock = new KeyReadWriteLock<>();
                }
            }
        }
        return lock;
    }

    /** 用绝对路径做 key，避免相对路径与绝对路径变成两把锁 */
    private static String lockKey(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            // 降级：使用绝对路径，并记录警告日志
            // 绝对路径至少能保证同一路径字符串的一致性，但符号链接等场景下可能不唯一
            return file.getAbsolutePath();
        }
    }

    public static void serialize(int version, Object data, File file) throws IOException, InterruptedException {
        if (data == null) {
            return;
        }
        String key = lockKey(file);
        lock().lockWrite(key);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            KryoUtils.serialize(version, data, out);
        } finally {
            lock().unlockWrite(key);
        }
    }

    public static <T> Optional<T> deserialize(File file, Class<T> type, int version) throws IOException, InterruptedException {
        String key = lockKey(file);
        long lastModified = 0;
        lock().lockRead(key);
        try {
            if (!file.exists()) {
                return Optional.empty();
            }
            lastModified = file.lastModified();
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                Optional<T> result = KryoUtils.deserialize(in, type, version);
                if (result.isPresent()) {
                    return result;
                }
            }
        } finally {
            lock().unlockRead(key);
        }
        // 执行到这里说明反序列化失败，删除文件
        lock().lockWrite(key);
        try {
            // 由于重新获取了锁，需要再次判断文件是否存在
            if (!file.exists()) {
                return Optional.empty();
            }
            // 如果锁间隙期间文件没被改过，则直接删除
            if (file.lastModified() == lastModified) {
                file.delete();
            }
        } finally {
            lock().unlockWrite(key);
        }
        return Optional.empty();
    }

    /**
     * 反序列化成 List
     * @param <T> 列表类型
     * @param file 文件
     * @param version 版本
     * @return 反序列化结果
     * @throws IOException 如果输入流关闭失败
     * @throws InterruptedException 如果线程中断
     */
    public static <T> Optional<List<T>> deserializeList(File file, int version) throws IOException, InterruptedException {
        Optional<ArrayList> result = deserialize(file, ArrayList.class, version);
        return result.map(list -> (List<T>) list);
    }

    /**
     * 反序列化成 Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @param file 文件
     * @param version 版本
     * @return 反序列化结果
     * @throws IOException 如果输入流关闭失败
     * @throws InterruptedException 如果线程中断
     */
    public static <K, V> Optional<Map<K, V>> deserializeMap(File file, int version) throws IOException, InterruptedException {
        Optional<HashMap> result = deserialize(file, HashMap.class, version);
        return result.map(map -> (Map<K, V>) map);
    }
}
