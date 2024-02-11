package github.ag777.util.lang.jni;

import com.ag777.util.lang.StringUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 窗口句柄相关工具类
 * 对jna以及jna-platform的二次封装
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/9 11:36
 */
public class JnaWindowsUtils {
    /**
     * 获取当前系统中所有的窗口句柄
     *
     * @return 窗口句柄列表
     */
    public static List<WinDef.HWND> getAllWindows() {
        return findWindows(hwd-> hwd);
    }

    /**
     * 查找符合特定条件的所有窗口。
     *
     * @param filter 一个函数，接收一个 {@link WinDef.HWND} 类型的窗口句柄，并返回一个泛型R类型的结果。
     *               如果该窗口符合条件（即filter函数返回非null值），则将其加入结果列表。
     * @param <R>    泛型参数，表示filter函数返回的对象类型。
     * @return 返回一个包含所有符合条件窗口的泛型R类型对象列表。
     */
    public static <R>List<R> findWindows(Function<WinDef.HWND, R> filter) {
        List<R> results = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            R item = filter.apply(hWnd);
            if (item != null) {
                results.add(item);
            }
            return true; // 继续枚举
        }, null);
        return results;
    }

    /**
     * 查找符合特定条件的第一个窗口。
     *
     * @param filter 一个函数，接收一个 {@link WinDef.HWND} 类型的窗口句柄，并返回一个泛型R类型的结果。
     *               此函数用于判断窗口是否符合特定条件。如果符合条件（即filter函数返回非null值），
     *               则停止枚举并返回该窗口。
     * @param <R>    泛型参数，表示filter函数返回的对象类型。
     * @return 返回第一个符合条件的窗口的泛型R类型对象。如果没有找到符合条件的窗口，则返回null。
     */
    public static <R> Optional<R> findFirstWindow(Function<WinDef.HWND, R> filter) {
        final Object[] resultContainer = {null};

        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            R item = filter.apply(hWnd);
            if (item != null) {
                resultContainer[0] = item;
                return false; // 找到第一个非空项，停止枚举
            }
            return true; // 继续枚举
        }, null);

        //noinspection unchecked
        return Optional.ofNullable((R) resultContainer[0]);
    }

    /**
     * 根据窗口句柄获取进程ID
     *
     * @param hWnd 窗口句柄
     * @return 进程ID
     */
    public static int getProcessId(WinDef.HWND hWnd) {
        IntByReference pid = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
        return pid.getValue();
    }

    /**
     * 根据窗口句柄获取窗口名称。
     *
     * @param hWnd 窗口句柄
     * @return 窗口名称字符串。如果获取失败，返回空字符串。
     */
    public static String getWindowTitle(WinDef.HWND hWnd) {
        char[] buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, buffer, buffer.length);
        return Native.toString(buffer).trim();
    }


    /**
     * 根据窗口句柄获取文件
     *
     * @param hWnd 窗口句柄
     * @return 进程对应的文件
     */
    public static Optional<File> getFile(WinDef.HWND hWnd) {
        int processId = getProcessId(hWnd);
        return getFileByProcessId(processId);
    }

    /**
     * 根据进程编号获取文件
     *
     * @param processId 进程编号
     * @return 进程对应的文件
     */
    public static Optional<File> getFileByProcessId(int processId) {
        final int PROCESS_QUERY_INFORMATION = 0x0400;
        final int PROCESS_VM_READ = 0x0010;
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, processId);

        if (processHandle != null) {
            // WCHAR_SIZE 是为了确保字符编码大小正确
            Pointer buffer = new Memory(WinDef.MAX_PATH * Native.WCHAR_SIZE);
            int length = Psapi.INSTANCE.GetModuleFileNameEx(processHandle, null, buffer, WinDef.MAX_PATH);
            Kernel32.INSTANCE.CloseHandle(processHandle);
            if (length == 0) {
                // 获取失败
                return Optional.empty();
            }
            // 使用getWideString来读取宽字符字符串
            String path = buffer.getWideString(0);
            return Optional.of(new File(path));
        }
        return Optional.empty();
    }

    /**
     * 检查窗口是否可见。
     *
     * @param hWnd 窗口句柄
     * @return 如果窗口可见，则返回true；否则返回false。
     */
    public static boolean isWindowVisible(WinDef.HWND hWnd) {
        return User32.INSTANCE.IsWindowVisible(hWnd);
    }

    /**
     * 获取窗口的父窗口句柄。
     *
     * @param hWnd 窗口句柄
     * @return 父窗口的句柄。如果没有父窗口，则返回null。
     */
    public static WinDef.HWND getParentWindow(WinDef.HWND hWnd) {
        return User32.INSTANCE.GetParent(hWnd);
    }

    /**
     * 获取创建窗口的线程ID。
     *
     * @param hWnd 窗口句柄
     * @return 创建窗口的线程ID。
     */
    public static int getWindowThreadId(WinDef.HWND hWnd) {
        return User32.INSTANCE.GetWindowThreadProcessId(hWnd, null);
    }

    /**
     * 根据进程编号获取窗口句柄列表
     *
     * @param processId 进程编号
     * @return 窗口句柄列表
     */
    public static List<WinDef.HWND> getWindowsByProcessId(final int processId) {
        final List<WinDef.HWND> windows = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
            if (pid.getValue() == processId) {
                windows.add(hWnd);
            }
            return true; // 继续枚举
        }, null);
        return windows;
    }

    /**
     * 判断指定的窗口是否可见。
     *
     * @param hWnd 要检查的窗口的句柄（HWND）。
     * @return 如果窗口可见返回 true，否则返回 false。
     */
    private static boolean isVisible(WinDef.HWND hWnd) {
        return User32.INSTANCE.IsWindowVisible(hWnd);
    }

    /**
     * 检查指定的窗口是否被最小化。
     * <p>
     * 此方法通过获取窗口的样式并检查是否包含 WS_MINIMIZE 标志来判断窗口是否最小化。
     *
     * @param hWnd 要检查的窗口的句柄（HWND）。
     * @return 如果窗口已被最小化返回 true，否则返回 false。
     */
    public static boolean isWindowMinimized(WinDef.HWND hWnd) {
        int style = User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_STYLE);
        return (style & WinUser.WS_MINIMIZE) != 0;
    }


    /**
     * 根据窗口句柄将窗口置于前台
     *
     * @param hWnd 窗口句柄
     */
    public static void bringWindowToFront(WinDef.HWND hWnd) {
        // 首先检查窗口是否最小化，如果是，则先恢复窗口
        if (isWindowMinimized(hWnd)) {
            User32.INSTANCE.ShowWindow(hWnd, WinUser.SW_RESTORE);
        }

        // 将窗口置于前台
        User32.INSTANCE.SetForegroundWindow(hWnd);

        // 如果 SetForegroundWindow 调用失败，尝试其他方法
        if (hWnd != User32.INSTANCE.GetForegroundWindow()) {
            // 附加到前台窗口线程以提升权限，然后再次尝试
            WinDef.HWND hForegroundWnd = User32.INSTANCE.GetForegroundWindow();
            int dwCurrentThread = Kernel32.INSTANCE.GetCurrentThreadId();
            int dwForegroundThread = User32.INSTANCE.GetWindowThreadProcessId(hForegroundWnd, null);

            User32.INSTANCE.AttachThreadInput(new WinDef.DWORD(dwForegroundThread), new WinDef.DWORD(dwCurrentThread), true);
            User32.INSTANCE.SetForegroundWindow(hWnd);
            User32.INSTANCE.AttachThreadInput(new WinDef.DWORD(dwForegroundThread), new WinDef.DWORD(dwCurrentThread), false);

            // 如果窗口仍然不在前台，使用 ShowWindow 尝试强制显示
            if (hWnd != User32.INSTANCE.GetForegroundWindow()) {
                User32.INSTANCE.ShowWindow(hWnd, WinUser.SW_SHOW);
            }
        }
    }

    public static void main(String[] args) {

        List<WinDef.HWND> windows = findWindows(hwd -> {
            Optional<File> file = getFile(hwd);
            if (!file.isPresent()) {
                return null;
            }
            if (file.get().getName().endsWith("chrome.exe")) {
                if (getParentWindow(hwd) != null) {
                    return null;
                }
                if (StringUtils.isEmpty(getWindowTitle(hwd))) {
                    return null;
                }
                return hwd;
            }
            return null;
        });
        if (!windows.isEmpty()) {
            bringWindowToFront(windows.get(0));
        } else {
            System.err.println("没找到");
        }
    }
}
