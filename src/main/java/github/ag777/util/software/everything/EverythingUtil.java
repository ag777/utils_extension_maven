package github.ag777.util.software.everything;

import com.sun.jna.WString;
import github.ag777.util.software.everything.model.EveryThingSearchOptions;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * everything调用工具(windows系统)
 * <a href="https://www.voidtools.com/zh-cn/downloads/">这里下载本体和sdk</a>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/08/09 17:04
 */
public class EverythingUtil {
    private final File exeFile;
    @Getter
    private final EverythingDll dll;

    public EverythingUtil(File exeFile, EverythingDll dll) {
        this.exeFile = exeFile;
        this.dll = dll;
    }

    /**
     * 加载Everything工具类实例
     *
     * 此方法首先检查指定的可执行文件和动态链接库文件是否存在，如果文件不存在，则抛出IllegalArgumentException
     * 如果文件存在，方法将加载动态链接库，并使用可执行文件和动态链接库创建Everything工具类实例
     *
     * @param exeFile Everything可执行文件的路径
     * @param dllFile Everything动态链接库文件的路径
     * @return 返回一个EverythingUtil实例，用于操作Everything
     * @throws IllegalArgumentException 如果可执行文件或动态链接库文件不存在，则抛出此异常
     */
    public static EverythingUtil load(File exeFile, File dllFile) throws IllegalArgumentException {
        // 检查可执行文件是否存在
        if (!exeFile.exists() || !exeFile.isFile()) {
            throw new IllegalArgumentException("Everything.exe not found: "+exeFile.getAbsolutePath());
        }
        // 检查动态链接库文件是否存在
        if (!dllFile.exists() || !dllFile.isFile()) {
            throw new IllegalArgumentException("Everything64.dll not found: "+dllFile.getAbsolutePath());
        }
        // 加载动态链接库
        EverythingDll dll = EverythingUtils.loadDll(dllFile);
        // 创建并返回Everything工具类实例
        return new EverythingUtil(exeFile, dll);
    }


    /**
     * 安装服务
     *
     * @throws IOException 如果在与外部服务交互过程中出现输入输出异常
     * @throws InterruptedException 如果线程在等待外部服务响应时被中断
     */
    public void installService() throws IOException, InterruptedException {
        EverythingUtils.installService(exeFile);
    }

    /**
     * 卸载服务
     *
     * @throws IOException 如果在与外部服务交互过程中出现输入输出异常
     * @throws InterruptedException 如果线程在等待外部服务响应时被中断
     */
    public void uninstallService() throws IOException, InterruptedException {
        EverythingUtils.uninstallService(exeFile);
    }

    /**
     * 启动服务异步操作
     *
     * @return 代表异步操作的Future对象
     * @throws IOException 如果在与外部服务交互过程中出现输入输出异常
     * @throws InterruptedException 如果线程在等待外部服务响应时被中断
     */
    public Future<Void> startService() throws IOException, InterruptedException {
        return EverythingUtils.startService(dll, exeFile);
    }

    /**
     * 关闭服务管理资源，如打开的文件或网络连接
     */
    public void closeService() {
        EverythingUtils.closeService(dll);
    }

    /**
     * 使用给定的关键字和选项进行搜索
     *
     * @param keyword 要搜索的关键字
     * @param options 搜索选项，用于定制搜索行为
     * @return 返回匹配文件的路径列表
     */
    public List<String> search(String keyword, EveryThingSearchOptions options) {
        // 根据选项设置是否匹配路径
        dll.Everything_SetMatchPath(options.isMatchPath());
        // 根据选项设置是否区分大小写
        dll.Everything_SetMatchCase(options.isMatchCase());
        // 根据选项设置是否全字匹配
        dll.Everything_SetMatchWholeWord(options.isMatchWholeWord());
        // 根据选项设置是否使用正则表达式
        dll.Everything_SetRegex(options.isUseRegex());
        // 设置最大返回数量
        dll.Everything_SetMax(options.getMax());
        // 设置偏移量，用于分页
        dll.Everything_SetOffset(options.getOffset());
        // 设置排序方式
        dll.Everything_SetSort(options.getSort());
        // 设置请求标志
        dll.Everything_SetRequestFlags(options.getRequestFlags());
        // 设置搜索关键字
        dll.Everything_SetSearchW(new WString(keyword));
        // 执行搜索并等待返回结果
        dll.Everything_QueryW(true);
        // 使用EverythingUtils工具类处理搜索结果
        return EverythingUtils.search(dll);
    }
}
