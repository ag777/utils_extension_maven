package github.ag777.util.file.excel.fesod.listener;

import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 将读取结果收集到内存列表的监听器。
 * <p>
 * 适合数据量中等、希望沿用监听器模式但又需要完整结果的场景。
 * 超大数据量请改用 {@link org.apache.fesod.sheet.read.listener.PageReadListener} 分批处理。
 * </p>
 *
 * <pre>{@code
 * CollectReadListener<DemoExcelData> listener = new CollectReadListener<>();
 * FesodSheet.read("demo.xlsx", DemoExcelData.class, listener).sheet().doRead();
 * List<DemoExcelData> all = listener.getDataList();
 * }</pre>
 *
 * @param <T> 行数据类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class CollectReadListener<T> implements ReadListener<T> {

    private final List<T> dataList = new ArrayList<>();

    @Override
    public void invoke(T data, AnalysisContext context) {
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 读取完成，结果已收集在 dataList 中
    }

    /**
     * 获取已收集的数据。
     * <p>返回内部列表本身（可变），便于调用方继续加工。</p>
     */
    public List<T> getDataList() {
        return dataList;
    }

    public int size() {
        return dataList.size();
    }

    public boolean isEmpty() {
        return dataList.isEmpty();
    }
}
