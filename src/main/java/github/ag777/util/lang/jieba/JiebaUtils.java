package github.ag777.util.lang.jieba;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;

import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 结巴分词二次封装工具类
 * @Date 2022/8/3 10:05
 */
public class JiebaUtils {
    // 标点符号正则
    private final static Pattern P_MARK = Pattern.compile("\\pP+");
    public static void loadDict(String filePath) {
        WordDictionary.getInstance().loadUserDict(Paths.get(filePath));
    }

    /**
     * search分词
     * @param text 文字
     * @return 分词结果(去除标点符号)
     */
    public static List<String> search(String text) {
        List<SegToken> list = split(text, JiebaSegmenter.SegMode.SEARCH);
        return toStrList(list);
    }

    /**
     * index分词
     * @param text 文字
     * @return 分词结果(去除标点符号)
     */
    public static List<String> index(String text) {
        List<SegToken> list = split(text, JiebaSegmenter.SegMode.INDEX);
        return toStrList(list);
    }

    private static List<String> toStrList(List<SegToken> list) {
        return list.stream()
                .map(t->t.word)
                .filter(w->!P_MARK.matcher(w).matches())
                .collect(Collectors.toList());
    }

    private static List<SegToken> split(String text, JiebaSegmenter.SegMode mode) {
        JiebaSegmenter jieba = new JiebaSegmenter();
        return jieba.process(text, mode);
    }

    public static void main(String[] args) {
        // resource/template/jieba.dict.txt
        String dictPath = JiebaUtils.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:/", "")+"template/jieba.dict.txt";
        loadDict(dictPath);
        String text = "可见性是指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值";
        search(text).forEach(System.out::println);
    }
}
