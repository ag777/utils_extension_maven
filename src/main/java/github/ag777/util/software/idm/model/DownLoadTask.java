package github.ag777.util.software.idm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/8 下午5:00
 */
@Data
@AllArgsConstructor
public class DownLoadTask extends CompletableFuture<File> {
    private String taskId;
    private String url;
    private File localFile;

    /**
     * 下载失败
     * @param taskId 任务ID
     * @param url 下载地址
     * @param localFile 本地文件
     * @param cause 异常
     * @return 下载任务
     */
    public static DownLoadTask failed(String taskId, String url, File localFile, Throwable cause) {
        DownLoadTask task = new DownLoadTask(taskId, url, localFile);
        task.completeExceptionally(cause);
        return task;
    }

    /**
     * 下载取消
     * @param taskId 任务ID
     * @param url 下载地址
     * @param localFile 本地文件
     * @return 下载任务
     */
    public static DownLoadTask cancelled(String taskId, String url, File localFile) {
        DownLoadTask task = new DownLoadTask(taskId, url, localFile);
        task.cancel(true);
        return task;
    }
}
