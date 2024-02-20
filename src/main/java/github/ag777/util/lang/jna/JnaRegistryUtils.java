package github.ag777.util.lang.jna;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * 注册表相关工具类
 * 对jna以及jna-platform的二次封装
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/9 13:30
 */
public class JnaRegistryUtils {
    /**
     * 读取指定注册表项的值。
     *
     * @param root      根键，例如 WinReg.HKEY_CURRENT_USER
     * @param keyPath   键路径
     * @param valueName 值名称
     * @return 值字符串，如果未找到则返回 null
     */
    public static String readRegistryValue(WinReg.HKEY root, String keyPath, String valueName) {
        if (Advapi32Util.registryValueExists(root, keyPath, valueName)) {
            return Advapi32Util.registryGetStringValue(root, keyPath, valueName);
        } else {
            return null;
        }
    }

    /**
     * 写入或更新注册表项的值。
     *
     * @param root      根键，例如 WinReg.HKEY_CURRENT_USER
     * @param keyPath   键路径
     * @param valueName 值名称
     * @param value     值字符串
     */
    public static void writeRegistryValue(WinReg.HKEY root, String keyPath, String valueName, String value) {
        Advapi32Util.registrySetStringValue(root, keyPath, valueName, value);
    }

    /**
     * 删除指定的注册表项值。
     *
     * @param root      根键，例如 WinReg.HKEY_CURRENT_USER
     * @param keyPath   键路径
     * @param valueName 值名称
     */
    public static void deleteRegistryValue(WinReg.HKEY root, String keyPath, String valueName) {
        if (Advapi32Util.registryValueExists(root, keyPath, valueName)) {
            Advapi32Util.registryDeleteValue(root, keyPath, valueName);
        }
    }

    /**
     * 创建一个新的注册表键。
     *
     * @param root    根键，例如 WinReg.HKEY_CURRENT_USER
     * @param keyPath 键路径
     */
    public static void createKey(WinReg.HKEY root, String keyPath) {
        if (!Advapi32Util.registryKeyExists(root, keyPath)) {
            Advapi32Util.registryCreateKey(root, keyPath);
        }
    }

    /**
     * 删除一个注册表键及其所有子键和值。
     *
     * @param root    根键，例如 WinReg.HKEY_CURRENT_USER
     * @param keyPath 键路径
     */
    public static void deleteKey(WinReg.HKEY root, String keyPath) {
        if (Advapi32Util.registryKeyExists(root, keyPath)) {
            Advapi32Util.registryDeleteKey(root, keyPath);
        }
    }
}
