package github.ag777.util.file.excel.fesod.listener;

import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 容错读取监听器：某行解析/转换失败时回调错误处理器并跳过该行，而不是中断整个读取。
 * <p>
 * 同时通过 {@link ReadListener#invokeHead} 收集实际表头，可用于校验用户上传的模板列是否正确。
 * </p>
 *
 * <pre>{@code
 * TolerantReadListener<DemoExcelData> listener = new TolerantReadListener<>(
 *         row -> save(row),
 *         (rowIndex, e) -> log.warn("第{}行解析失败", rowIndex + 1, e));
 * FesodSheet.read("demo.xlsx", DemoExcelData.class, listener).sheet().doRead();
 * List<String> heads = listener.getHeads(); // 实际表头，可做模板校验
 * }</pre>
 *
 * @param <T> 行数据类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class TolerantReadListener<T> implements ReadListener<T> {

    private final Consumer<T> rowConsumer;
    /** 错误处理器，入参为行下标（0开始）与异常 */
    private final BiConsumer<Integer, Exception> errorHandler;
    private final List<String> heads = new ArrayList<>();

    public TolerantReadListener(Consumer<T> rowConsumer, BiConsumer<Integer, Exception> errorHandler) {
        this.rowConsumer = rowConsumer;
        this.errorHandler = errorHandler;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        int rowIndex = context.readRowHolder() != null ? context.readRowHolder().getRowIndex() : -1;
        errorHandler.accept(rowIndex, exception);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        int maxIndex = headMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        for (int i = 0; i <= maxIndex; i++) {
            ReadCellData<?> cell = headMap.get(i);
            heads.add(cell == null ? null : cell.getStringValue());
        }
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        rowConsumer.accept(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    /**
     * 获取读取到的实际表头（按列顺序），仅在读取开始后有值。
     */
    public List<String> getHeads() {
        return heads;
    }
}
