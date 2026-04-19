package github.ag777.util.remote.ai.http.stream;

import github.ag777.util.http.model.MyCall;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可取消底层HTTP请求的Future。
 * 
 * <p>继承自{@link CompletableFuture}，增加了对底层HTTP请求的取消能力。
 * 当调用cancel方法时，不仅会取消Future本身，还会取消正在执行的HTTP请求。
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpFuture future = client.chatAsync(request);
 * 
 * // 可以取消正在进行的请求
 * if (shouldCancel) {
 *     future.cancel(true);
 * }
 * 
 * // 获取结果
 * AiHttpResponse response = future.get();
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
public class AiHttpFuture extends CompletableFuture<AiHttpResponse> {
    private final AtomicReference<MyCall> callRef = new AtomicReference<>();

    /**
     * 绑定HTTP调用对象到该Future。
     *
     * <p>如果Future在绑定前已经被取消，会立即取消该HTTP调用。
     *
     * @param call HTTP调用对象，用于后续取消操作
     */
    public void bind(MyCall call) {
        if (call == null) {
            return;
        }
        callRef.set(call);
        if (isCancelled()) {
            MyCall boundCall = callRef.getAndSet(null);
            if (boundCall != null) {
                boundCall.cancel();
            }
        }
    }

    /**
     * 取消Future和底层HTTP请求。
     *
     * <p>除了调用父类的取消逻辑外，还会尝试取消绑定的HTTP请求。
     *
     * @param mayInterruptIfRunning 如果正在执行，是否中断线程
     * @return 如果取消成功返回true，否则返回false
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        MyCall call = callRef.getAndSet(null);
        if (call != null) {
            call.cancel();
        }
        return super.cancel(mayInterruptIfRunning);
    }
}
