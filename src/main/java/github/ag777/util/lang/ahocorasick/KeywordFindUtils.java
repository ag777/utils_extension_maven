package github.ag777.util.lang.ahocorasick;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.Collection;

/**
 * 关键词查找, ahocorasick调用示例代码
 * {@link <a href="https://github.com/robert-bor/aho-corasick">github地址</a>}
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/1/13 17:04
 */
public class KeywordFindUtils {

    /**
     * 查找关键词
     * @param sentence 句子
     * @param keywords 关键词
     * @return 找到的关键词
     */
    public static Collection<Emit> find(String sentence, String... keywords) {
        Trie trie = Trie.builder().ignoreCase()
                .addKeywords(keywords).build();
        return trie.parseText(sentence);
    }

    public static void main(String[] args) {
        find(
                "张三找李四吃饭，花了33元人民币",
                "李四", "3元"
        ).forEach(System.out::println);
    }

}
