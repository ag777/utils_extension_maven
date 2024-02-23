package github.ag777.util.lang.system;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 全局的键盘监听工具类,通过开源库jnativehook实现
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/23 11:27
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
     * 将自定义的键盘监听器添加到全局屏幕事件监听中。
     * <p>
     * 此方法允许用户添加一个自定义的 {@link NativeKeyListener} 实例，
     * 以便在全局范围内监听键盘事件。添加的监听器将接收所有键盘事件，
     * 包括按键按下和释放，无论应用窗口是否处于焦点状态。
     * <p>
     * 使用此方法可以实现对全局键盘事件的自定义处理逻辑，例如实现自定义的热键功能。
     * 注意，添加过多的监听器可能会对性能产生影响，因此应当谨慎使用。
     *
     * @param listener 要添加的 {@link NativeKeyListener} 实例。不应该是 {@code null}。
     *                 监听器应该已经实现了适当的事件处理方法。
     * @throws IllegalArgumentException 如果 {@code listener} 是 {@code null}，
     *                                  则抛出此异常。
     * @see NativeKeyListener
     */
    public void addCustomListener(NativeKeyListener listener) {
        GlobalScreen.addNativeKeyListener(listener);
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
     * 获取当前注册的所有键盘监听器数据的集合。
     * <p>
     * 该方法返回一个包含所有 {@link KeyListenerData} 实例的集合。
     * 每个 {@link KeyListenerData} 实例代表一个注册的键盘监听器，其中包含了
     * 监听器的名称、修饰符键、非修饰符键以及当对应的键盘事件发生时需要触发的消费者动作。
     * </p>
     * <p>
     * 此集合的返回允许外部调用者访问和遍历当前所有注册的键盘监听器信息，可以用于
     * 监控、日志记录或用户界面显示等目的。请注意，返回的集合是当前监听器状态的快照，
     * 对集合的修改不会影响实际的监听器注册状态。
     * </p>
     *
     * @return 当前注册的所有键盘监听器数据的不可修改集合。
     */
    public Collection<KeyListenerData> getKeyListenerDataList() {
        return listeners.values();
    }

    /**
     * 获取所有注册的热键文本表示。
     * <p>
     * 此方法遍历所有已注册的键盘监听器，并为每个监听器生成一个包含其热键文本表示的字符串数组。
     * 热键被分为修饰符键和非修饰符键，分别获取它们的文本表示后合并成一个数组。
     * </p>
     *
     * @return 返回一个列表，每个元素是一个字符串数组，对应一个监听器的所有热键的文本表示。
     *         每个字符串数组首先包含修饰符键的文本表示，然后是非修饰符键的文本表示。
     */
    public List<String[]> getKeys() {
        return listeners.values().stream()
                .map(KeyListenerData::getKeyTexts).collect(Collectors.toList());
    }

    /**
     * 获取所有注册的热键组合。
     * @return 所有热键组合的列表。
     */
    public List<int[]> getAllHotkeys() {
        List<int[]> hotkeys = new ArrayList<>();
        listeners.values().forEach(listenerData -> {
            // 可以选择仅添加修饰符键或包括所有键
            // 这里我们添加的是包含修饰符键和非修饰符键的完整组合
            int[] completeKeyCombination = new int[listenerData.getModifierKeys().length + listenerData.getNonModifierKeys().length];
            System.arraycopy(listenerData.getModifierKeys(), 0, completeKeyCombination, 0, listenerData.getModifierKeys().length);
            System.arraycopy(listenerData.getNonModifierKeys(), 0, completeKeyCombination, listenerData.getModifierKeys().length, listenerData.getNonModifierKeys().length);
            hotkeys.add(completeKeyCombination);
        });
        return hotkeys;
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

    /**
     * 生成基于给定键码数组的唯一键ID。
     * <p>
     * 此方法首先对键码数组进行排序，以确保键ID的唯一性不受键码顺序的影响。
     * 然后，使用数组的哈希码作为键ID，因为相同的键码数组（无论其元素顺序）将产生相同的哈希码，
     * 这确保了每个键码组合都有一个独特的ID。这个ID可以用于标识和检索特定的键盘快捷键或热键组合。
     * </p>
     *
     * @param keyCodes 一个包含键码的整型数组，每个键码代表一个特定的键盘按键。
     * @return 一个基于提供的键码数组的哈希码生成的唯一整型ID。
     */
    public int generateUniqueKeyId(int[] keyCodes) {
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

    /**
     *
     * @param keyCode 按键
     * @return 给定的keyCode是否为修饰符键
     */
    public static boolean isModifierKey(int keyCode) {
        return keyCode == NativeKeyEvent.CTRL_MASK || keyCode == NativeKeyEvent.SHIFT_MASK ||
                keyCode == NativeKeyEvent.ALT_MASK || keyCode == NativeKeyEvent.META_MASK;
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

        public String[] getKeyTexts() {
            // 获取修饰符键文本数组
            String[] modifiersText = Arrays.stream(modifierKeys)
                    .mapToObj(NativeKeyEvent::getModifiersText)
                    .toArray(String[]::new);

            // 获取非修饰符键文本数组
            String[] nonModifiersText = Arrays.stream(nonModifierKeys)
                    .mapToObj(NativeKeyEvent::getKeyText)
                    .toArray(String[]::new);

            // 合并修饰符和非修饰符键的文本数组
            String[] allKeys = new String[modifiersText.length + nonModifiersText.length];
            System.arraycopy(modifiersText, 0, allKeys, 0, modifiersText.length);
            System.arraycopy(nonModifiersText, 0, allKeys, modifiersText.length, nonModifiersText.length);

            return allKeys;
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
