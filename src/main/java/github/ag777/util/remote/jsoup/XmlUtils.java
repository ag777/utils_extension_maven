package github.ag777.util.remote.jsoup;

import github.ag777.util.file.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 有关 <code>Jsoup</code> 爬虫工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>jsoup-1.10.2.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2017年09月26日,last modify at 2017年09月26日
 */
public class XmlUtils {
	
	public static Document load(String filePath) throws IOException {
		String content = FileUtils.readText(filePath);
		return Jsoup.parse(content);
	}
	
	
	public static Map<String,Object> getMap(String filePath, String tagName, String attr) throws IOException {
		Document doc = load(filePath);
		Map<String,Object> map = new HashMap<>(10);
		
		Elements elements = doc.getElementsByTag(tagName);
		for (Element element : elements) {
			if(element.hasAttr(attr)) {
				String key = element.attr(attr);
				String value = element.text();
				map.put(key, value);
			}
		}
		return map;
	}
	
}
