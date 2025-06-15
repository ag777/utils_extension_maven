package github.ag777.util.file.compress;

import github.ag777.util.file.FileUtils;
import github.ag777.util.file.compress.base.BaseApacheCompressUtils;
import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.exception.Assert;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * 有关tar.gz文件的压缩和解压的工具基类,java原生库的二次封装
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月12日
 */
public class GzUtils {

	private GzUtils() {}
	
	/*============压缩==================*/
	/**
	 * 将文件压缩成gz包
	 * 
	 * @param filePath 需要压缩的文件路径
	 * @param gzPath 生成的gz文件路径
	 * @return 生成的gz文件对象
	 * @throws IOException 如果文件不存在或压缩过程中发生IO异常
	 */
	public static File gz(String filePath, String gzPath) throws IOException {
		Assert.notExisted(filePath, "需要压缩成.gz的文件不存在:"+filePath);
		GZIPOutputStream gos = null;
		InputStream is = null;
		try {
			is = FileUtils.getInputStream(filePath);
			gos = new GZIPOutputStream(FileUtils.getOutputStream(gzPath));
	
			int count;
			byte data[] = new byte[BaseApacheCompressUtils.BUFFER];
			while ((count = is.read(data, 0, BaseApacheCompressUtils.BUFFER)) != -1) {
				gos.write(data, 0, count);
			}
			
			gos.finish();
			return new File(gzPath);
		} catch(Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(is, gos);
		}
		
	}
	
	
	/*============解压==================*/
	/**
	 * 将gz包解压成tar包
     * 
	 * @param gzPath 需要解压的gz文件路径
	 * @param tarPath 生成的tar文件路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unGz(String gzPath, String tarPath) throws IOException {
		Assert.notExisted(gzPath, "需要解压的文件不存在:"+gzPath);
		GzipCompressorInputStream gcis = null;
		BufferedOutputStream bos = null;
		try {
			bos = FileUtils.getBufferedOutputStream(tarPath);  
	        gcis = new GzipCompressorInputStream(
	        		new BufferedInputStream(FileUtils.getInputStream(gzPath)));
	        
	        IOUtils.write(gcis, bos, BaseApacheCompressUtils.BUFFER);
		} catch(Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(gcis, bos);
		}

	}
}
