package github.ag777.util.software.idm;

import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.StringUtils;
import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.lang.thread.AsyncTaskManager;
import github.ag777.util.software.idm.model.DownLoadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/8 下午4:27
 */
public class IDMDownLoader {
    private final String idmExePath;
    // 定时任务执行器，用于检查文件存在性和超时
    private final ScheduledExecutorService monitorExecutor;
    // 监听器状态控制
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    private final AsyncTaskManager<File, DownLoadTask> taskManager;

    private IDMDownLoader(String idmExePath) {
        this.idmExePath = idmExePath;
        taskManager = new AsyncTaskManager<>();
        // 创建检测线程
        monitorExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "download-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 加载IDM下载器
     * @param idmExePath IDM.exe路径
     * @return IDM下载器
     * @throws FileNotFoundException 文件未找到异常
     */
    public static IDMDownLoader load(String idmExePath) throws FileNotFoundException {
        File exeFile = new File(idmExePath);
        if (!exeFile.exists() || !exeFile.isFile()) {
            throw new FileNotFoundException("IDM.exe not found: " + idmExePath);
        }
        return new IDMDownLoader(idmExePath);
    }

    /**
     * 下载文件
     * @param url 下载地址
     * @param localFile 本地文件
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 下载文件
     * @throws ValidateException 验证异常
     * @throws InterruptedException 中断异常
     */
    public File downLoadForFile(String url, File localFile, long timeout, TimeUnit unit) throws InterruptedException, ValidateException {
        DownLoadTask task = downloadForTask(url, localFile);
        try {
            task.get(timeout, unit);
            return task.getLocalFile();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof ValidateException) {
                throw (ValidateException)t;
            }
            throw new ValidateException("下载发生未知异常:"+t.getMessage(), t);
        } catch (TimeoutException e) {
            cancelMonitor(task.getTaskId());
            throw new ValidateException("下载超时", e);
        }
    }


    /**
     * 下载文件，并返回下载任务
     * @param url 下载地址
     * @param localFile 本地文件
     * @return 下载任务
     */
    public DownLoadTask downloadForTask(String url, File localFile) {
        String taskId = StringUtils.uuid();
        try {
            download(url, localFile);
            startMonitoring();
        } catch (ValidateException e) {
            return DownLoadTask.failed(taskId, url, localFile, e);
        } catch (InterruptedException e) {
            return DownLoadTask.cancelled(taskId, url, localFile);
        }
        return taskManager.addTask(taskId, new DownLoadTask(taskId, url, localFile));

    }


    /**
     * 下载文件
     * @param url 下载地址
     * @param localFile 本地文件
     * @throws ValidateException 验证异常
     * @throws InterruptedException 中断异常
     */
    public void download(String url, File localFile) throws ValidateException, InterruptedException {
        if (localFile.exists()) {
            return;
        }
        // 构建IDM命令行参数并启动进程，返回是否执行成功，并读取命令行输出的报错信息
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    idmExePath,
                    "/n",
                    "/p", localFile.getParent(),
                    "/d", url,
                    "/f", localFile.getName()
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 读取命令行的错误输出
            String errorMsg = IOUtils.readText(process.getErrorStream(), StandardCharsets.UTF_8);
            if (exitCode != 0) {
                throw new ValidateException("命令行执行失败，错误信息：" + errorMsg);
            }
        } catch (IOException e) {
            throw new ValidateException("命令行执行失败", e);
        }

    }

    /**
     * 取消监听任务
     * @param taskId 任务ID
     */
    public void cancelMonitor(String taskId) {
        taskManager.cancel(taskId);
    }

    /**
     * 启动监听器
     */
    private void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            // 每2秒检查一次文件状态
            monitorExecutor.scheduleWithFixedDelay(this::checkDownloadStatus, 0, 2, TimeUnit.SECONDS);
        }
    }
    
    /**
     * 停止监听器
     */
    private void stopMonitoring() {
        isMonitoring.compareAndSet(true, false);
    }

    /**
     * 检查下载状态
     */
    private void checkDownloadStatus() {
        if (taskManager.getTaskMap().isEmpty()) {
            stopMonitoring();
            return;
        }
        taskManager.forEachTask((id, task)->{
            // 检查文件是否存在
            File file = task.getLocalFile();
            if (file.exists() && file.isFile()) {
                task.complete(file);
            }
        });
    }
}
