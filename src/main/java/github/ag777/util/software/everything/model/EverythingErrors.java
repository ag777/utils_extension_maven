package github.ag777.util.software.everything.model;

/**
 * everything 错误枚举
 * <a href="https://www.voidtools.com/zh-cn/support/everything/sdk/everything_getlasterror/">错误枚举</a>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2022/11/22 9:33
 */
public class EverythingErrors {
    // The operation completed successfully
    public static final int EVERYTHING_OK=0;
    // Failed to allocate memory for the search query
    public static final int EVERYTHING_ERROR_MEMORY=1;
    // IPC is not available
    public static final int EVERYTHING_ERROR_IPC=2;
    // Failed to register the search query window class
    public static final int EVERYTHING_ERROR_REGISTERCLASSEX=3;
    // Failed to create the search query window
    public static final int EVERYTHING_ERROR_CREATEWINDOW=4;
    // Failed to create the search query thread
    public static final int EVERYTHING_ERROR_CREATETHREAD=5;
    // Invalid index. The index must be greater or equal to 0 and less than the number of visible results
    public static final int EVERYTHING_ERROR_INVALIDINDEX=6;
    // Invalid call
    public static final int EVERYTHING_ERROR_INVALIDCALL=7;
}
