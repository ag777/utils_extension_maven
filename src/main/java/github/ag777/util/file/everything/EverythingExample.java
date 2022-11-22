package github.ag777.util.file.everything;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * everything 调用示例
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/21 15:39
 */
public class EverythingExample {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        String baseDir = "D:\\project\\idea\\test\\mixed\\src\\main\\resources\\everything\\";
        String exePath = baseDir+"Everything.exe";
        String dllPath = baseDir+"Everything64.dll";
        EverythingUtil u = EverythingUtil.load(exePath, dllPath);
        FutureTask<Void> task = u.startService();
        System.out.println("启动中...");
        task.get();
        System.out.println("启动完成");
        try {
            List<String> list = u.search(".\\\\doc\\\\readme\\.md$", false, true);
            if (list.isEmpty()) {
                System.err.println("没结果");
            }
            list.forEach(System.out::println);
        } finally {
            System.out.println("准备关闭");
            u.closeService();
            System.out.println("关闭");
        }
    }
}
