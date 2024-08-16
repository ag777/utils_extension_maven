package github.ag777.util.remote.jsoup.base;

import com.ag777.util.lang.StringUtils;
import github.ag777.util.remote.jsoup.JsoupBuilder;
import github.ag777.util.remote.jsoup.JsoupUtils;

import java.io.IOException;

public abstract class BaseCrawler {

	protected JsoupUtils u;
	
	public BaseCrawler(JsoupUtils u) {
		this.u = u;
		init(u);
	}
	
	public abstract void init(JsoupUtils u);
	
	protected static JsoupBuilder builder(String cookiesStr, Integer retryTimes) throws IOException {
		JsoupBuilder builder = JsoupBuilder.newInstance();
		if(retryTimes != null && retryTimes > 0) {
			builder.retryTimes(retryTimes);
		}
		if(!StringUtils.isBlank(cookiesStr)) {
			builder.cookies(cookiesStr);
		}
		return builder;
	}
}
