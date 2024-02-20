package github.ag777.util.lang.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进程相关工具类
 * 对jna以及jna-platform的二次封装
 * @author ag777＜ag777@vip.qq.com＞
 * @version 2024/2/9 13:11
 */
public class JnaProcessUtils {

    /**
     * 获取当前系统中所有运行进程的PID（进程标识符）。
     *
     * @return 包含所有进程PID的整型数组。
     */
    public static int[] getAllProcessIds() {
        // 最多支持1024个进程，根据需要调整
        int[] processIds = new int[1024];
        final int size = processIds.length * Native.getNativeSize(Integer.TYPE);
        IntByReference lpcbNeeded = new IntByReference();

        Psapi.INSTANCE.EnumProcesses(processIds, size, lpcbNeeded);

        // 实际返回的进程数量
        int processCount = lpcbNeeded.getValue() / Native.getNativeSize(Integer.TYPE);

        // 调整数组大小以匹配实际进程数量
        return java.util.Arrays.copyOf(processIds, processCount);
    }

    /**
     * 根据给定的进程ID获取其父进程的ID。
     *
     * @param pid 目标进程的PID。
     * @return 父进程的PID。如果找不到父进程，则返回-1。
     */
    public static int getParentProcessId(int pid) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();

        try {
            boolean success = Kernel32.INSTANCE.Process32First(snapshot, processEntry);
            while (success) {
                if (processEntry.th32ProcessID.intValue() == pid) {
                    return processEntry.th32ParentProcessID.intValue();
                }
                success = Kernel32.INSTANCE.Process32Next(snapshot, processEntry);
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }

        return -1; // Parent PID not found
    }

    /**
     * 根据进程ID获取进程的名称。
     *
     * @param pid 目标进程的PID。
     * @return 进程名称。如果找不到指定的进程，则返回null。
     */
    public static String getProcessName(int pid) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();

        try {
            boolean success = Kernel32.INSTANCE.Process32First(snapshot, processEntry);
            while (success) {
                if (processEntry.th32ProcessID.intValue() == pid) {
                    return Native.toString(processEntry.szExeFile);
                }
                success = Kernel32.INSTANCE.Process32Next(snapshot, processEntry);
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }

        return null; // Process name not found
    }

    /**
     * 根据进程ID终止一个进程。
     *
     * @param pid 要终止的进程的PID。
     * @return 如果成功终止进程，返回true；否则返回false。
     */
    public static boolean terminateProcess(int pid) {
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_TERMINATE, false, pid);
        if (processHandle == null) {
            return false;
        }

        try {
            return Kernel32.INSTANCE.TerminateProcess(processHandle, 0);
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }

    /**
     * 构建整个系统的进程树。
     *
     * @return 系统中所有进程组成的树。
     */
    public static Map<Integer, ProcessNode> buildProcessTree() {
        int[] allPids = getAllProcessIds();
        Map<Integer, ProcessNode> processMap = new HashMap<>();
        Map<Integer, ProcessNode> processTree = new HashMap<>();

        // 首先，创建所有进程节点，并映射PID到节点
        for (int pid : allPids) {
            String name = getProcessName(pid);
            processMap.put(pid, new ProcessNode(pid, name));
        }

        // 然后，根据父子关系构建进程树
        for (ProcessNode node : processMap.values()) {
            int parentPid = getParentProcessId(node.pid);
            if (parentPid > 0 && processMap.containsKey(parentPid)) {
                processMap.get(parentPid).addChild(node);
            } else {
                // 如果找不到父进程或父进程PID<=0，认为是顶级进程
                processTree.put(node.pid, node);
            }
        }

        return processTree;
    }


    public static void main(String[] args) {
        Map<Integer, ProcessNode> processTree = buildProcessTree();

        // 打印进程树
        System.out.println("System Process Tree:");
        processTree.values().forEach(System.out::println);
    }

    @Data
    static class ProcessNode {
        int pid;
        String name;
        List<ProcessNode> children;

        public ProcessNode(int pid, String name) {
            this.pid = pid;
            this.name = name;
            children = new ArrayList<>(1);
        }

        // 添加子进程节点
        public void addChild(ProcessNode child) {
            this.children.add(child);
        }
    }
}
