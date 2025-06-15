package github.ag777.util.file.csv;

import github.ag777.util.lang.Console;
import github.ag777.util.lang.IOUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 对commons-csv的二次封装
 * @author ag777 <837915770@vip.qq.com>
 * @version  2025/1/22 20:56
 */
public class CsvUtils {

    /**
     *
     * @param file csv文件
     * @param titles map对应的key
     * @param skipFirstRow 是否跳过条记录
     * @return 列表
     * @throws IOException io异常
     */
    public static List<Map<String, Object>> read(File file, String[] titles, boolean skipFirstRow) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        read(file, record->{
            if (record.getRecordNumber() == 1) {
                return;
            }
            Map<String, Object> item = new HashMap<>();
            int i=skipFirstRow?0:1;
            for (; i < titles.length; i++) {
                item.put(titles[i], record.get(i));
            }
            list.add(item);
        });
        return list;
    }

    /**
     *
     * @param file csv文件
     * @param lineReader 没跳记录的读取器
     * @throws IOException io异常
     */
    public static void read(File file, Consumer<CSVRecord> lineReader) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
            for (CSVRecord record : records) {
                lineReader.accept(record);
            }
        } finally {
            IOUtils.close(reader);
        }
    }

    /**
     * 将数据列表转换为CSV格式字符串
     *
     * @param dataList 数据列表，每个元素是一个Object数组，包含清单名和标签列表
     * @param headers 标题
     * @return CSV格式的内容
     * @throws IOException IO异常
     */
    public static String toStr(List<Object[]> dataList, String[] headers) throws IOException {
        StringWriter writer = new StringWriter();
        write(dataList, headers, writer);
        return writer.toString();
    }

    /**
     * 将数据列表写入CSV文件
     *
     * @param dataList 数据列表，每个元素是一个Object数组，包含清单名和标签列表
     * @param headers 标题
     * @param filePath 输出文件路径
     * @throws IOException IO异常
     */
    public static void write(List<Object[]> dataList, String[] headers, String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            write(dataList, headers, writer);
        }
    }

    /**
     * 将数据写入Writer
     *
     * @param dataList 数据列表
     * @param headers 标题
     * @param writer 输出writer
     * @throws IOException IO异常
     */
    public static void write(List<Object[]> dataList, String[] headers, Writer writer) throws IOException {
        CSVFormat.Builder builder = CSVFormat.DEFAULT.builder();
        if (headers != null) {
            builder.setHeader(headers);
        }
        CSVFormat csvFormat = builder.build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            for (Object[] data : dataList) {
                if (data.length < 2) {
                    continue;
                }
                csvPrinter.printRecord(data);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        String filePath = "D:\\temp\\程序测试\\111.csv";
        String[] titles = {"name","url","username","password"};
        List<Map<String, Object>> list = read(new File(filePath), titles, true);
        Console.prettyLog(list);
    }

}
