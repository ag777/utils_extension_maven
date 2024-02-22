package github.ag777.util.lang.system;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 全局的键盘监听工具类,通过开源库jnativehook实现
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/22 15:17
 */
public class KeyBoardInputListener implements NativeKeyListener {
    private volatile static KeyBoardInputListener INSTANCE;
    private final ConcurrentHashMap<Integer, KeyListenerData> listeners;

    private KeyBoardInputListener() {
        listeners = new ConcurrentHashMap<>();
    }

    private void init() throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);

    }

    public static KeyBoardInputListener getInstance() throws NativeHookException {
        if (INSTANCE == null) {
            synchronized (KeyBoardInputListener.class) {
                if (INSTANCE == null) {
                    KeyBoardInputListener i = new KeyBoardInputListener();
                    i.init();
                    INSTANCE = i;
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 根据按键组合的唯一ID获取对应的KeyListenerData对象。
     * @param keyId 按键组合的唯一ID
     * @return 对应的KeyListenerData对象，如果不存在，则返回null。
     */
    public KeyListenerData getKeyListenerData(int keyId) {
        return listeners.get(keyId);
    }

    /**
     *
     * @param keyCodes 按键组合
     * @return 是否已经被注册过
     */
    public boolean contains(int[] keyCodes) {
        int keyId = generateUniqueKeyId(keyCodes);
        return listeners.containsKey(keyId);
    }

    /**
     * 注册一个按键组合的监听器。
     * @param keyCodes 按键组合，例如 [NativeKeyEvent.CTRL_MASK, NativeKeyEvent.VC_C]
     * @param consumer 当按键组合被触发时执行的操作
     * @return 监听器的唯一ID，如果组合键已存在，返回null
     */
    public Integer register(String name, int[] keyCodes, Consumer<NativeKeyEvent> consumer) {
        int keyId = generateUniqueKeyId(keyCodes);

        KeyListenerData newData = new KeyListenerData(name, keyCodes, consumer);
        // 直接尝试注册监听器，如果keyId已存在，不会覆盖原有的监听器
        KeyListenerData existingData = listeners.putIfAbsent(keyId, newData);
        if (existingData != null) {
            // 如果存在返回null，表示注册失败，因为键已存在
            return null;
        }
        // 否则，注册成功，返回生成的keyId
        return keyId;
    }

    private int generateUniqueKeyId(int[] keyCodes) {
        Arrays.sort(keyCodes);
        return Arrays.hashCode(keyCodes);
    }

    /**
     * 注销一个按键监听器。
     * @param key 监听器的ID
     * @return 是否成功注销
     */
    public boolean unregister(int key) {
        return listeners.remove(key) != null;
    }

    /**
     * 注销所有注册的按键监听器。
     * 这个方法将移除所有当前注册的监听器，清空监听器列表。
     */
    public void unregisterAllListeners() {
        listeners.clear();
    }

    /**
     * 销毁这个类的实例，并注销全局钩子和监听器。
     * 这个方法用于清理资源，注销全局的键盘钩子，并移除之前添加的监听器。
     * 调用此方法后，此实例将不再响应键盘事件。
     */
    public void destroy() throws NativeHookException {
        // 注销全局钩子
        GlobalScreen.unregisterNativeHook();
        // 移除此类作为键盘事件的监听器
        GlobalScreen.removeNativeKeyListener(this);
        // 帮助垃圾收集器回收此实例
        INSTANCE = null;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        listeners.values().forEach(listenerData -> {
            if (listenerData.isCombinationPressed(e)) {
                listenerData.getConsumer().accept(e);
            }
        });
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // 可选：处理按键释放
        listeners.values().forEach(listenerData -> {
            // 对于每个监听器，如果按下的键是监听器中的任一非修饰符键，重置isPressed状态
            for (int nonModifier : listenerData.getNonModifierKeys()) {
                if (e.getKeyCode() == nonModifier) {
                    listenerData.setPressed(false);
                    break; // 找到匹配的非修饰符键，跳出循环
                }
            }
        });
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // 可选：处理按键类型
    }

    @Data
    public static class KeyListenerData {
        private String name;
        private final int[] modifierKeys;
        private final int[] nonModifierKeys;
        private final Consumer<NativeKeyEvent> consumer;
        private boolean isPressed; // 追踪组合键是否被触发

        public KeyListenerData(String name, int[] keyCodes, Consumer<NativeKeyEvent> consumer) {
            this.name = name;
            this.consumer = consumer;
            // 使用临时列表分别存储修饰符和非修饰符键，然后转换为数组
            List<Integer> modifiers = new ArrayList<>();
            List<Integer> nonModifiers = new ArrayList<>();
            for (int keyCode : keyCodes) {
                if (isModifierKey(keyCode)) {
                    modifiers.add(keyCode);
                } else {
                    nonModifiers.add(keyCode);
                }
            }
            this.modifierKeys = modifiers.stream().mapToInt(i -> i).toArray();
            this.nonModifierKeys = nonModifiers.stream().mapToInt(i -> i).toArray();
        }

        public boolean isCombinationPressed(NativeKeyEvent event) {
            if (isPressed) { // 如果已经被触发，避免重复触发
                return false;
            }

            for (int modifier : modifierKeys) {
                if ((event.getModifiers() & modifier) == 0) {
                    return false;
                }
            }
            for (int nonModifier : nonModifierKeys) {
                if (event.getKeyCode() == nonModifier) {
                    isPressed = true; // 标记为已触发
                    return true;
                }
            }
            return false;
        }


        /**
         *
         * @param keyCode 按键
         * @return 给定的keyCode是否为修饰符键
         */
        private boolean isModifierKey(int keyCode) {
            return keyCode == NativeKeyEvent.CTRL_MASK || keyCode == NativeKeyEvent.SHIFT_MASK ||
                    keyCode == NativeKeyEvent.ALT_MASK || keyCode == NativeKeyEvent.META_MASK;
        }

    }

    public static void main(String[] args) throws NativeHookException {
        KeyBoardInputListener kil = KeyBoardInputListener.getInstance();
        kil.register(
                "测试",
                new int[]{NativeKeyEvent.CTRL_MASK, NativeKeyEvent.VC_P, NativeKeyEvent.ALT_MASK},
                e-> System.out.println("Ctrl+P+Alt pressed")
        );
    }
}
