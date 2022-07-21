package github.ag777.util.file.csv;

import com.ag777.util.lang.Console;
import com.ag777.util.lang.IOUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 对commons-csv的二次封装
 * @Date 2022/7/21 14:58
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

    public static void main(String[] args) throws IOException {
        String filePath = "D:\\temp\\程序测试\\111.csv";
        String[] titles = {"name","url","username","password"};
        List<Map<String, Object>> list = read(new File(filePath), titles, true);
        Console.prettyLog(list);
    }

}
