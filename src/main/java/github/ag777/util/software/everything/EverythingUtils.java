package github.ag777.util.software.everything;

import com.sun.jna.Native;
import github.ag777.util.software.everything.model.EverythingCmdOptions;
import github.ag777.util.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/8/9 下午3:23
 */
public class EverythingUtils {

    /**
     * 加载动态链接库（DLL）文件
     *
     * 此方法使用Native库加载指定的DLL文件，以便在Java应用程序中使用DLL中的本地方法
     * 它提供了一种方便的方法来加载和使用本地库，而不需要显式的JNI（Java本地接口）代码
     *
     * @param dllFile 要加载的DLL文件对象
     * @return 返回一个EverythingDll实例，该实例代表已加载的DLL
     */
    public static EverythingDll loadDll(File dllFile) {
        return Native.load(dllFile.getAbsolutePath(), EverythingDll.class);
    }

    /**
     * 使用EverythingDll搜索结果
     *
     * @param dll EverythingDll实例，用于调用Everything API
     * @return 包含所有搜索结果路径的字符串列表
     */
    public static List<String> search(EverythingDll dll) {
        // 获取搜索结果数量
        int num = dll.Everything_GetNumResults();
        // 如果没有搜索结果，返回空列表
        if (num == 0) {
            return Collections.emptyList();
        }
        // 创建一个链表来存储搜索结果路径
        LinkedList<String> result = new LinkedList<>();
        // 创建一个字符缓冲区用于存储每个搜索结果的路径
        CharBuffer p = CharBuffer.allocate(1024);
        // 创建一个字符数组，用于清空字符缓冲区
        char[] a = new char[1024];
        // 遍历所有搜索结果
        for (int i = 0; i < num; i++) {
            // 获取第i个搜索结果的完整路径名称
            dll.Everything_GetResultFullPathNameW(i, p, 1024);
            // 将缓冲区中的路径转换为字符串，并去除可能的空字符
            String s = p.toString().replace("\0", "");
            // 将路径添加到结果列表中
            result.add(s);
            // 清空缓冲区，以便存储下一个路径
            p.put(a, 0, 1024);
            p.clear();
        }
        // 返回包含所有路径的结果列表
        return result;
    }


    /**
     * 安装Everything服务
     * 该方法通过调用Everything的可执行文件来安装服务
     *
     * @param everythingExeFile Everything的可执行文件路径
     * @throws IOException 如果创建进程失败
     * @throws InterruptedException 如果等待进程结束时线程被中断
     */
    public static void installService(File everythingExeFile) throws IOException, InterruptedException {
        execByExe(everythingExeFile, EverythingCmdOptions.INSTALL_SERVICE, true);
    }

    /**
     * 卸载Everything服务
     * 该方法通过调用Everything的可执行文件来卸载服务
     *
     * @param everythingExeFile Everything的可执行文件路径
     * @throws IOException 如果创建进程失败
     * @throws InterruptedException 如果等待进程结束时线程被中断
     */
    public static void uninstallService(File everythingExeFile) throws IOException, InterruptedException {
        execByExe(everythingExeFile, EverythingCmdOptions.UNINSTALL_SERVICE, true);
    }

    /**
     * 启动Everything服务
     * 如果数据库已经加载，则无需执行任何操作
     * 否则，通过调用相应的命令行指令启动服务，并确保数据库成功加载
     *
     * @param dll Everything的DLL接口，用于检查数据库是否加载
     * @param everythingExeFile Everything工具的可执行文件路径
     * @return 一个Future对象，可用于跟踪异步任务的完成状态
     * @throws IOException 如果执行过程中发生I/O错误
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public static Future<Void> startService(EverythingDll dll, File everythingExeFile) throws IOException, InterruptedException {
        // 检查数据库是否已经加载，如果已加载则直接返回完成的Future
        if (dll.Everything_IsDBLoaded()) {
            return CompletableFuture.completedFuture(null);
        }
        System.out.println("everything启动中...");
        // 执行命令行指令以启动服务
        execByExe(everythingExeFile, EverythingCmdOptions.START_SERVICE, true);
        // 执行命令行指令以启动Everything
        execByExe(everythingExeFile, EverythingCmdOptions.STARTUP, false);

        // 创建一个FutureTask，其中的任务是等待数据库加载
        FutureTask<Void> task = new FutureTask<>(() -> {
            // 循环检查数据库是否加载，直到加载完成
            while (!dll.Everything_IsDBLoaded()) {
                // 短暂休眠，避免高CPU占用
                TimeUnit.MILLISECONDS.sleep(100);
            }
            System.out.println("everything启动完成...");
            return null;
        });
        // 启动一个新的线程来执行任务
        new Thread(task).start();
        // 返回任务的Future，以便调用者可以跟踪任务的完成状态
        return task;
    }

    /**
     * 关闭Everything服务
     *
     * @param dll Everything的DLL接口实例，用于调用Everything的相关方法
     * @return 如果服务关闭失败（即exitValue不为0），返回错误码的Optional；否则返回空的Optional
     */
    public static Optional<Integer> closeService(EverythingDll dll) {
        // 调用Everything_Exit方法来尝试关闭服务，获取返回的退出码
        int exitValue = dll.Everything_Exit();
        // 检查退出码，如果不为0，则表示关闭服务失败
        if (exitValue != 0) {
            // 返回错误码，通过调用Everything_GetLastError方法获取
            return Optional.of(dll.Everything_GetLastError());
        }
        // 服务关闭成功，返回空的Optional
        return Optional.empty();
    }



    /**
     * 使用指定的可执行文件和选项执行命令行操作
     * 此方法专为通过可执行文件调用命令行工具而设计，例如“everything”搜索工具
     * 它提供了一种灵活的方式来执行命令行操作，并可以选择是否等待操作完成
     *
     * @param everythingExeFile 可执行文件路径，用于启动命令行操作
     * @param options           传递给可执行文件的命令行参数
     * @param waitFor           指示是否等待命令行操作完成
     * @return                  如果执行成功，返回true；否则返回false
     * @throws IOException          如果在启动命令行操作时发生I/O错误
     * @throws InterruptedException 如果等待命令行操作完成时线程被中断
     */
    private static boolean execByExe(File everythingExeFile, String options, boolean waitFor) throws IOException, InterruptedException {
        return execByExe(everythingExeFile, options, null, waitFor);
    }


    /**
     * 使用Everything工具执行搜索操作
     *
     * @param everythingExeFile Everything工具的可执行文件对象
     * @param options           搜索选项，用于控制搜索行为
     * @param searchText        要搜索的文本
     * @param waitFor           是否等待完成
     * @return                  如果执行成功，返回true；否则返回false
     * @throws IOException      如果启动进程时发生I/O错误
     * @throws InterruptedException    如果在等待线程被中断
     */
    private static boolean execByExe(File everythingExeFile, String options, String searchText, boolean waitFor) throws IOException, InterruptedException {
        StringBuilder cmd = new StringBuilder(everythingExeFile.getAbsolutePath())
                .append(' ').append(options);
        if (!StringUtils.isEmpty(searchText)) {
            cmd.append(' ').append(searchText);
        }
        Process exec = Runtime.getRuntime().exec(cmd.toString());
        if (waitFor) {
            return exec.waitFor() == 0;
        }
        return true;
    }
}
