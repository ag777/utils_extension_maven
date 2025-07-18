package github.ag777.util.file.compress;

import github.ag777.util.file.FileUtils;
import github.ag777.util.lang.StringUtils;
import github.ag777.util.lang.exception.Assert;

import java.io.File;
import java.io.IOException;

/**
 * 有关解压的工具基类,commons-compress二次封装
 * <p>
 * ①支持对tar.gz和zip文件的压缩解压
 * ②使用前请对参数文件的非空和存在性做校验，否则会抛出异常
 * ③压缩时空文件夹不会被丢弃,放心食用
 * ④支持密码保护的7z文件解压，完全支持中文密码
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.17.jar</li>
 * <li>xz-1.8.jar</li>
 * 其中xz-1.8.jar是压缩解压7z文件夹用的
 * </ul>
 * commons-compress包的更新日志:http://commons.apache.org/proper/commons-compress/changes-report.html
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2025年06月15日
 */
public class CompressUtils {

	/*============压缩================*/
	//--tar.gz文件压缩
	/**
	 * 将文件列表打包成tar包(临时),并压缩成tar.gz包
	 * <p>
	 * -中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * 
	 * -windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param paths 需要压缩的文件路径数组
	 * @param gzPath 生成的tar.gz文件路径
	 * @return 生成的tar.gz文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File targz(String[] paths, String gzPath) throws IOException {
		return targz(getFiles(paths), gzPath);
	}
	
	/**
	 * 将文件列表打包成tar包(临时),并压缩成tar.gz包
	 * <p>
	 * -中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * 
	 * -windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param files 需要压缩的文件数组
	 * @param gzPath 生成的tar.gz文件路径
	 * @return 生成的tar.gz文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File targz(File[] files, String gzPath) throws IOException {
		String tarPath = StringUtils.concat(new File(gzPath).getParent(), File.separator, StringUtils.uuid(), ".tar.temp");
		try {
			File file = TarUtils.getInstance().tar(files, tarPath);
			return GzUtils.gz(file.getPath(), gzPath);
		} catch(RuntimeException|IOException ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);	//删除临时的tar文件
		}
	}
	
	//--zip压缩
	/**
	 * 将文件或文件夹压缩成zip包
	 * 
	 * @param filePath 需要压缩的文件路径
	 * @param zipPath 生成的zip文件路径
	 * @return 生成的zip文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File zip(String filePath, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(getFiles(filePath), zipPath);
	}
	
	/**
	 * 将文件或文件夹压缩成zip包
	 * 
	 * @param paths 需要压缩的文件路径数组
	 * @param zipPath 生成的zip文件路径
	 * @return 生成的zip文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File zip(String[] paths, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(getFiles(paths), zipPath);
	}
	
	/**
	 * 将文件或文件夹压缩成zip包
	 * 
	 * @param files 需要压缩的文件数组
	 * @param zipPath 生成的zip文件路径
	 * @return 生成的zip文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File zip(File[] files, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(files, zipPath);
	}
	
	//-7z压缩
	/**
	 * 将文件或文件夹压缩成7z包
	 * 
	 * @param filePath 需要压缩的文件路径
	 * @param sevenZPath 生成的7z文件路径
	 * @return 生成的7z文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File sevenZ(String filePath, String sevenZPath) throws IOException {
		return SevenZUtils.compress(getFiles(filePath), sevenZPath);
	}
	
	/**
	 * 将文件或文件夹压缩成7z包
	 * 
	 * @param paths 需要压缩的文件路径数组
	 * @param sevenZPath 生成的7z文件路径
	 * @return 生成的7z文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File sevenZ(String[] paths, String sevenZPath) throws IOException {
		return SevenZUtils.compress(getFiles(paths), sevenZPath);
	}
	
	/**
	 * 将文件或文件夹压缩成7z包
	 * 
	 * @param files 需要压缩的文件数组
	 * @param sevenZPath 生成的7z文件路径
	 * @return 生成的7z文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public static File sevenZ(File[] files, String sevenZPath) throws IOException {
		return SevenZUtils.compress(files, sevenZPath);
	}
	
	/*============解压================*/
	//tar.gz解压
	/**
	 * 解压缩tar.gz包为tar包(临时)，解包tar包到指定路径
	 * <p>
	 * 中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * </p>
	 * 
	 * @param gzPath 需要解压的tar.gz文件路径
	 * @param targetPath 解压的目标路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unTargz(String gzPath, String targetPath) throws IOException {
		Assert.notExisted(gzPath, "需要被解压的文件不存在:"+gzPath);
		String tarPath = StringUtils.concat(new File(gzPath).getParent(), File.separator, StringUtils.uuid(), ".tar.temp");
		try {
			GzUtils.unGz(gzPath, tarPath);
			TarUtils.getInstance().unTar(tarPath, targetPath);
		} catch(Exception ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);
		}
	}
	
	//zip解压
	/**
	 * 解压zip包到指定路径
	 * 
	 * @param zipPath 需要解压的zip文件路径
	 * @param targetPath 解压的目标路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unZip(String zipPath, String targetPath) throws IOException {
		ZipUtils.getInstance().unZip(zipPath, targetPath);
	}
	

	
	//7z解压
	/**
	 * 解压7z包到指定路径
	 * 
	 * @param sevenZPath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unSevenZ(String sevenZPath, String targetPath) throws IOException {
		SevenZUtils.decompress(sevenZPath, targetPath);
	}
	
	/**
	 * 解压密码保护的7z包到指定路径
	 * <p>
	 * 注意：中文密码完全支持，推荐使用char[]参数版本以确保内存安全
	 * </p>
	 * 
	 * @param sevenZPath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @param password 解压密码（支持中文字符）
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unSevenZWithPassword(String sevenZPath, String targetPath, String password) throws IOException {
		SevenZUtils.decompressWithPassword(sevenZPath, targetPath, password);
	}
	
	/**
	 * 解压密码保护的7z包到指定路径（推荐方法）
	 * <p>
	 * 使用char[]传递密码更安全，可以在使用后清空内存中的密码
	 * 完全支持中文密码和其他Unicode字符
	 * </p>
	 * 
	 * @param sevenZPath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @param password 解压密码字符数组（支持中文字符，更安全）
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void unSevenZWithPassword(String sevenZPath, String targetPath, char[] password) throws IOException {
		SevenZUtils.decompressWithPassword(sevenZPath, targetPath, password);
	}
	
	/*============内部方法================*/
	/**
	 * 将单个路径转文件数组(参数传递用)
	 * 
	 * @param filePath 文件路径
	 * @return 包含单个文件对象的数组
	 */
	private static File[] getFiles(String filePath) {
		File f = new File(filePath);
//		Assert.notExisted(f, "需要被压缩的文件不存在:"+filePath);
		return new File[]{f};
	}
	/**
	 * 将路径数组转文件数组(参数传递用)
	 * 
	 * @param paths 文件路径数组
	 * @return 文件对象数组
	 */
	private static File[] getFiles(String[] paths) {
//		Assert.notEmpty(paths, "至少选择压缩一个文件");
		File[] files = new File[paths.length];
		for(int i=0;i<paths.length;i++) {
//			Assert.notExisted(paths[i], "需要被压缩的文件不存在:"+paths[i]);
			files[i] = new File(paths[i]);
		}
		return files;
	}
}
