package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Apache Fesod 通用辅助方法。
 * <p>
 * 与 {@link github.ag777.util.file.excel.ExcelUtils} 类似，提供与读写无关的公共能力，
 * 例如输出文件准备、动态表头构造、无模型行数据的键名转换等。
 * </p>
 *
 * <pre>{@code
 * // 根据列标题构造动态表头（单层）
 * List<List<String>> head = FesodExcelUtils.headFromTitles("姓名", "年龄", "工资");
 *
 * // 将 Fesod 无模型读取结果转为与 ExcelReadUtils 类似的 a/b/c 键
 * Map<String, String> row = FesodExcelUtils.toLetterKeyMap(mapRow);
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/">Apache Fesod 官方文档</a>
 */
public class FesodExcelUtils {

    private FesodExcelUtils() {}

    /**
     * 确保输出文件的父目录存在。
     *
     * @param outputFile 目标文件
     * @return 入参本身，便于链式调用
     */
    public static File prepareOutputFile(File outputFile) {
        Assert.notNull(outputFile, "输出文件不能为空");
        File parent = outputFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        return outputFile;
    }

    /**
     * 根据列标题构造单层动态表头。
     *
     * @param titles 列标题，顺序即列顺序
     * @return Fesod 动态表头结构 {@code List<List<String>>}
     */
    public static List<List<String>> headFromTitles(String... titles) {
        Assert.notEmpty(titles, "表头不能为空");
        List<List<String>> head = new ArrayList<>(titles.length);
        for (String title : titles) {
            head.add(Collections.singletonList(title));
        }
        return head;
    }

    /**
     * 根据列标题构造单层动态表头。
     */
    public static List<List<String>> headFromTitles(List<String> titles) {
        Assert.notEmpty(titles, "表头不能为空");
        return titles.stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());
    }

    /**
     * 构造多级表头中的一列。
     * <p>
     * 示例：{@code columnHead("主标题", "子标题")} 对应 {@code @ExcelProperty({"主标题", "子标题"})}
     * </p>
     */
    public static List<String> columnHead(String... levels) {
        Assert.notEmpty(levels, "表头层级不能为空");
        return List.of(levels);
    }

    /**
     * 将 Fesod 无模型读取的 {@code Map<列索引, 值>} 转为 {@code Map<列字母, 值>}。
     * <p>
     * 与 {@link github.ag777.util.file.excel.ExcelReadUtils} 中 a/b/c 键风格保持一致，便于迁移旧代码。
     * </p>
     */
    public static Map<String, String> toLetterKeyMap(Map<Integer, String> row) {
        if (row == null || row.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(row.size());
        row.forEach((index, value) -> result.put(columnLetter(index), value));
        return result;
    }

    /**
     * 列索引转 Excel 列字母：0→a，1→b，…，25→z，26→aa，27→ab。
     */
    public static String columnLetter(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException("列索引不能小于0: " + columnIndex);
        }
        // Excel 列字母是"无 0"的 26 进制：每轮先取余得到末位字母，再减 1 进位
        StringBuilder letters = new StringBuilder();
        int index = columnIndex;
        while (index >= 0) {
            letters.append((char) ('a' + index % 26));
            index = index / 26 - 1;
        }
        return letters.reverse().toString();
    }

    /**
     * 构造按行写入的动态数据（每行一个 {@code List<Object>}）。
     */
    public static List<List<Object>> rows(Object[]... rows) {
        Assert.notEmpty(rows, "数据行不能为空");
        List<List<Object>> result = new ArrayList<>(rows.length);
        for (Object[] row : rows) {
            result.add(Arrays.asList(row));
        }
        return result;
    }
}
