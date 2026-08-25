package github.ag777.util.lang.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/8/24 16:31
 */
public class KryoUtils {
    // 用于辨认当前数据是不是基于工具类生成的
    private static final int MAGIC = 0x4B525931;

    private static final Pool<Kryo> kryoPool = new Pool<>(true, false, 8) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(true);
            return kryo;
        }
    };

    /**
     * 序列化任意对象到输出流（会关闭 out）
     * @param version 版本
     * @param data 数据
     * @param out 输出流
     * @throws IOException 如果输出流关闭失败
     */
    public static void serialize(int version, Object data, OutputStream out) throws IOException {
        if (data == null) {
            // 目标为空，不进行序列化
            return;
        }
        Kryo kryo = kryoPool.obtain();
        try (Output output = new Output(out)) {
            // 写入版本头
            output.writeInt(MAGIC);
            output.writeInt(version);
            // 写入数据
            kryo.writeObject(output, data);
        } finally {
            kryoPool.free(kryo);
        }
    }

    /**
     * 从输入流反序列化成 List，自动校验版本（会关闭 in）
     * @param in 输入流
     * @param version 版本
     * @return 反序列化结果
     * @throws IOException 如果输入流关闭失败
     * @param <T> 列表类型
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<List<T>> deserializeList(InputStream in, int version) throws IOException {
        Optional<ArrayList> result = deserialize(in, ArrayList.class, version);
        return result.map(list -> (List<T>) list);
    }

    /**
     * 从输入流反序列化成 Map，自动校验版本（会关闭 in）
     * @param in 输入流
     * @param version 版本
     * @return 反序列化结果
     * @throws IOException 如果输入流关闭失败
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> Optional<Map<K, V>> deserializeMap(InputStream in, int version) throws IOException {
        Optional<HashMap> result = deserialize(in, HashMap.class, version);
        return result.map(map -> (Map<K, V>) map);
    }

    /**
     * 从输入流反序列化，自动校验版本（会关闭 in）
     * @param in 输入流
     * @param type 类型
     * @param version 版本
     * @return 反序列化结果
     * @throws IOException 如果输入流关闭失败
     * @param <T> 类型
     */
    public static <T> Optional<T> deserialize(InputStream in, Class<T> type, int version) throws IOException {
        try (Input input = new Input(new BufferedInputStream(in))) {
            // 校验魔数
            int magic = input.readInt();
            if (magic != MAGIC) {
                return Optional.empty();
            }
            // 校验版本
            int version1 = input.readInt();
            if (version1 != version) {
                return Optional.empty();
            }
            // 反序列化
            Kryo kryo = kryoPool.obtain();
            try {
                return Optional.ofNullable(kryo.readObject(input, type));
            } finally {
                kryoPool.free(kryo);
            }
        }
    }

    /**
     * 从输入流读取版本
     * @param in 输入流
     * @return 版本
     * @throws IOException 如果输入流关闭失败
     */
    public static Optional<Integer> readVersion(InputStream in) throws IOException {
        try (Input input = new Input(new BufferedInputStream(in))) {
            // 校验魔数
            int magic = input.readInt();
            if (magic != MAGIC) {
                return Optional.empty();
            }
            // 读取版本
            return Optional.of(input.readInt());
        }
    }

}
