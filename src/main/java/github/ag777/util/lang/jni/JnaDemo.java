package github.ag777.util.lang.jni;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * jna调用示例
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/10/8 17:15
 */
public class JnaDemo {

    public interface CLibrary extends Library {
        int sum(int a, int b);
    }

    public static void main(String[] args) {
        CLibrary instance = Native.load("d:/a.so", CLibrary.class);
        int sum = instance.sum(3, 6);
        System.out.println(sum);
    }
}
