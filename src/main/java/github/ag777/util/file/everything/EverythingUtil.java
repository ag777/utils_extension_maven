package github.ag777.util.file.everything;

import com.ag777.util.lang.StringUtils;
import com.sun.jna.Native;
import com.sun.jna.WString;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * everything调用工具(windows系统)
 * 这里下载本体和sdk: https://www.voidtools.com/zh-cn/downloads/
 *
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/21 15:18
 */
public class EverythingUtil {
    private String exePath;
    private EverythingDll dll;

    private EverythingUtil(String exePath, EverythingDll dll) {
        this.exePath = exePath;
        this.dll = dll;
    }

    public EverythingDll getDll() {
        return dll;
    }

    /**
     * 加载dll,初始化工具类
     * @param exePath everything.exe文件的路径
     * @param dllPath everything.dll的路径
     * @return 工具类
     */
    public static EverythingUtil load(String exePath, String dllPath) {
        // jni实例化接口对象
        EverythingDll dll = Native.load(dllPath, EverythingDll.class);
        return new EverythingUtil(exePath, dll);
    }

    public void installService() throws IOException, InterruptedException {
        execByExe(EverythingCmdOptions.INSTALL_SERVICE, true);
    }

    public void uninstallService() throws IOException, InterruptedException {
        execByExe(EverythingCmdOptions.UNINSTALL_SERVICE, true);
    }

    /**
     * 启动服务
     * @return 等待服务启动完成的异步任务
     * @throws IOException 调用命令行异常
     * @throws InterruptedException 中断
     */
    public FutureTask<Void> startService() throws IOException, InterruptedException {
        execByExe(EverythingCmdOptions.START_SERVICE, true);
        execByExe(EverythingCmdOptions.STARTUP, false);
        FutureTask<Void> task = new FutureTask<>(() -> {
            while (!dll.Everything_IsDBLoaded()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            return null;
        });
        new Thread(task).start();
        return task;
    }

    /**
     * 关闭服务
     * @throws IOException 调用命令行异常
     * @throws InterruptedException 中断
     */
    public void closeService() throws IOException, InterruptedException {
//        execByExe("-exit", true);
        dll.Everything_Exit();
    }

    private void execByExe(String options, boolean waitFor) throws IOException, InterruptedException {
        execByExe(options, null, waitFor);
    }

    /**
     * 调用命令行接口
     * @param options 方法
     * @param searchText 参数
     * @throws IOException 调用命令行异常
     * @throws InterruptedException 中断
     */
    private void execByExe(String options, String searchText, boolean waitFor) throws IOException, InterruptedException {
        StringBuilder cmd = new StringBuilder(exePath)
                .append(' ').append(options);
        if (!StringUtils.isEmpty(searchText)) {
            cmd.append(' ').append(searchText);
        }
        Process exec = Runtime.getRuntime().exec(cmd.toString());
        if (waitFor) {
            exec.waitFor();
        }
    }

    /**
     * 搜索文件
     * @param fileName 文件名
     * @param caseSensitive 是否忽略大小写
     * @param regex 是否是正则表达式
     * @return 搜索结果列表
     */
    public List<String> search(String fileName, boolean caseSensitive, boolean regex) {
        LinkedList<String> result = new LinkedList<>();
        // 只扫描文件
//        String format = "file: \""+fileName+"\"";
        dll.Everything_SetMatchCase(caseSensitive);
        dll.Everything_SetRegex(regex);
        dll.Everything_SetSearchW(new WString(fileName));
        dll.Everything_QueryW(true);
        int num = dll.Everything_GetNumResults();
//        System.out.println(fileName+" find file "+num);
        CharBuffer p = CharBuffer.allocate(1024);
        char[] a = new char[1024];
        for (int i = 0; i < num; i++) {
            dll.Everything_GetResultFullPathNameW(i, p, 1024);
            String s = p.toString().replace("\0", "");
            result.add(s);
            // 清空buffer数据
            p.put(a, 0, 1024);
            p.clear();
        }
        return result;
    }
}
