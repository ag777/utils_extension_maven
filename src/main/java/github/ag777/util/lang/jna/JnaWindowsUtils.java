package github.ag777.util.lang.jna;

import com.ag777.util.lang.StringUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 窗口句柄相关工具类
 * 对jna以及jna-platform的二次封装
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/23 17:57
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
     * 如果找不到进程，请以管理员模式执行
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
     * 展示窗口
     * @param hWnd 窗口句柄
     * @param nCmdShow nCmdShow 展示的形式.可以传WinUser.SW_XXX
     */
    public static void showWindow(WinDef.HWND hWnd, int nCmdShow) {
        User32.INSTANCE.ShowWindow(hWnd, nCmdShow);
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

    public static WinDef.RECT getWindowRect(WinDef.HWND hWnd) {
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return rect;
    }

    public static Rectangle getWindowRectangle(WinDef.HWND hWnd) {
        WinDef.RECT rect = getWindowRect(hWnd);
        // 窗口左上角的X坐标
        int x = rect.left;
        // 窗口左上角的Y坐标
        int y = rect.top;
        // 窗口的宽度
        int width = rect.right - rect.left;
        // 窗口的高度
        int height = rect.bottom - rect.top;
        return new Rectangle(x, y, width, height);
    }

    /**
     * 将指定窗口捕获为图像。
     * <p>
     * 该方法通过获取窗口的设备上下文（DC），然后创建一个与之兼容的内存DC来捕获窗口内容。
     * 最后，将捕获的内容转换为Java的BufferedImage对象。
     * 注意，此方法还考虑了高DPI设置下的坐标换算，确保在高分辨率屏幕上也能正确捕获窗口。
     * </p>
     * @param hWnd 窗口的句柄（HWND），是要捕获的窗口的标识。
     * @return 捕获的窗口内容，作为BufferedImage对象返回。如果过程中发生任何错误，可能返回null。
     */
    public static BufferedImage captureWindowToImage(WinDef.HWND hWnd) {
        WinDef.HDC windowDC = null;
        WinDef.HDC memDC = null;
        WinDef.HBITMAP hBitmap = null;
        try {
            // 获取指定窗口的设备上下文
            windowDC = User32.INSTANCE.GetDC(hWnd);
            // 获取窗口的矩形区域
            Rectangle rect = getWindowRectangle(hWnd);
            // 调整矩形区域以适应高DPI设置,只处理宽高
            adjustRectangleForHighDPI(rect, true);
            // 创建与给定窗口设备上下文兼容的内存设备上下文
            memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC);
            // 创建一个兼容位图
            hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, rect.width, rect.height);
            // 将位图选入到内存设备上下文，并保存旧位图的句柄
            WinNT.HANDLE oldBitmap = GDI32.INSTANCE.SelectObject(memDC, hBitmap);
            // 从窗口设备上下文拷贝图像到内存设备上下文
            GDI32.INSTANCE.BitBlt(memDC, 0, 0, rect.width, rect.height, windowDC, 0, 0, GDI32.SRCCOPY);
            // 将旧位图选回到内存设备上下文中
            GDI32.INSTANCE.SelectObject(memDC, oldBitmap);

            // 创建一个BufferedImage来接收从内存设备上下文中拷贝的图像
            BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
            WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
            bmi.bmiHeader.biWidth = rect.width;
            bmi.bmiHeader.biHeight = -rect.height; // 图像不倒置
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

            // 准备接收位图的像素数据
            DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
            int[] pixels = buffer.getData();
            Pointer pixelPointer = new Memory(pixels.length * 4); // 每个像素4字节
            // 从内存设备上下文中的位图中提取图像数据
            GDI32.INSTANCE.GetDIBits(memDC, hBitmap, 0, rect.height, pixelPointer, bmi, WinGDI.DIB_RGB_COLORS);
            // 将提取的数据写入BufferedImage
            pixelPointer.read(0, pixels, 0, pixels.length);

            return image;
        } finally {
            // 清理创建的GDI资源
            if (hBitmap != null) {
                GDI32.INSTANCE.DeleteObject(hBitmap);
            }
            if (memDC != null) {
                GDI32.INSTANCE.DeleteDC(memDC);
            }
            if (windowDC != null) {
                User32.INSTANCE.ReleaseDC(hWnd, windowDC);
            }
        }
    }

    /**
     * 将指定窗口置于前台，如果失败则执行指定的回调函数并等待指定的时间后再次尝试。
     *
     * @param hWnd 需要置于前台的窗口句柄。
     * @param whenFail 当置于前台失败时执行的回调函数，该函数接收一个整数参数（表示失败的尝试次数）并返回一个布尔值（表示是否应继续尝试）。
     * @param sleepMillsOnFail 失败后重新尝试前的等待时间，单位为毫秒。
     * @return 如果窗口成功置于前台则返回true，否则返回false。
     * @throws InterruptedException 如果在等待过程中线程被中断，将抛出此异常。
     */
    public static boolean bringWindowToFront(WinDef.HWND hWnd, Predicate<Integer> whenFail, long sleepMillsOnFail) throws InterruptedException {
        boolean success = false;
        int attempts = 0;
        while (!success) {
            success = bringWindowToFront(hWnd);
            if (!success) {
                attempts++;
                if (!whenFail.test(attempts)) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(sleepMillsOnFail);
            }
        }
        return success;
    }

    /**
     * 根据窗口句柄将窗口置于前台
     *
     * @param hWnd 窗口句柄
     * @return 执行后当前窗口是否是目标窗口
     */
    public static boolean bringWindowToFront(WinDef.HWND hWnd) {
        // 首先 SW_RESTORE 激活并显示窗口。如果窗口被最小化或最大化，Windows恢复它到原来的大小和位置。
        showWindow(hWnd, WinUser.SW_RESTORE);
        if (!isForegroundWindow(hWnd)) {
            // 将窗口置于前台
            User32.INSTANCE.SetForegroundWindow(hWnd);

            // 如果 SetForegroundWindow 调用失败，尝试其他方法
            if (!isForegroundWindow(hWnd)) {
                // 附加到前台窗口线程以提升权限，然后再次尝试
                WinDef.HWND hForegroundWnd = User32.INSTANCE.GetForegroundWindow();
                int dwCurrentThread = Kernel32.INSTANCE.GetCurrentThreadId();
                int dwForegroundThread = User32.INSTANCE.GetWindowThreadProcessId(hForegroundWnd, null);

                User32.INSTANCE.AttachThreadInput(new WinDef.DWORD(dwForegroundThread), new WinDef.DWORD(dwCurrentThread), true);
                User32.INSTANCE.SetForegroundWindow(hWnd);
                User32.INSTANCE.AttachThreadInput(new WinDef.DWORD(dwForegroundThread), new WinDef.DWORD(dwCurrentThread), false);

                // 如果窗口仍然不在前台，使用 ShowWindow 尝试强制显示
                if (!isForegroundWindow(hWnd)) {
                    showWindow(hWnd, WinUser.SW_SHOW);
                    return isForegroundWindow(hWnd);
                }
            }
        }

        return true;
    }

    /**
     * @param hWnd 窗口
     * @return 窗口是否在前台
     */
    public static boolean isForegroundWindow(WinDef.HWND hWnd) {
        return hWnd.equals(User32.INSTANCE.GetForegroundWindow());
    }

    /**
     * 获取主显示器的物理分辨率。
     * <p>
     * 此方法返回主显示器的实际分辨率，考虑了可能的DPI缩放设置。
     * 与 {@code Toolkit.getDefaultToolkit().getScreenSize()} 方法不同，
     * 它返回的是操作系统报告的屏幕实际使用分辨率，而非Java程序可能看到的逻辑分辨率。
     * </p>
     * <p>
     * 注意：如果系统连接了多个显示器，此方法只返回主显示器的分辨率。
     * </p>
     * @return 主显示器的物理分辨率。
     * @throws IllegalArgumentException 如果没有找到显示屏。
     */
    private static Dimension getPhysicalScreenResolution() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        for (GraphicsDevice curGs : gs) {
            DisplayMode dm = curGs.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            return new Dimension(screenWidth, screenHeight);
        }
        throw new IllegalArgumentException("没有找到显示屏");
    }

    /**
     * 根据屏幕的DPI设置调整矩形区域的尺寸。
     * 这个方法用于确保在具有不同DPI设置的显示屏上，矩形区域能够保持其在物理尺寸上的一致性。
     *
     * @param r 待调整的矩形区域，调整后的尺寸将直接反映在此对象上。
     * @param onlyWidthAndHeight 是否只调整矩形的宽度和高度，而不调整x和y坐标。
     */
    private static void adjustRectangleForHighDPI(Rectangle r, boolean onlyWidthAndHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension realSize = getPhysicalScreenResolution();
        if (screenSize.width != realSize.width) {
            double widthRatio = realSize.width * 1d / screenSize.width;
            if (!onlyWidthAndHeight) {
                r.x = (int) Math.ceil(r.x * widthRatio);
            }
            r.width = (int) Math.ceil(r.width * widthRatio);
        }
        if (screenSize.height != realSize.height) {
            double heightRatio = realSize.height * 1d / screenSize.height;
            if (!onlyWidthAndHeight) {
                r.y = (int) Math.ceil(r.y * heightRatio);
            }
            r.height = (int) Math.ceil(r.height * heightRatio);
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
