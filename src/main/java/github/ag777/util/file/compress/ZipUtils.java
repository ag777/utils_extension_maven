package github.ag777.util.file.compress;

import github.ag777.util.file.compress.base.BaseApacheCompressUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 有关zip文件的压缩和解压的工具基类,commons-compress二次封装
 * <p>
 * 为什么不用原生的zip库:https://blog.csdn.net/yk614294861/article/details/78961013
 * 简单来说:Windows 压缩的时候使用的是系统的编码 GB2312，而 Mac 系统默认的编码是 UTF-8，于是出现了乱码。
 * Apache commons-compress 解压 zip 文件是件很幸福的事，可以解决 zip 包中文件名有中文时跨平台的乱码问题，不管文件是在 Windows 压缩的还是在 Mac，Linux 压缩的，解压后都没有再出现乱码问题了。
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.17.jar</li>
 * </ul>
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月16日
 */
public class ZipUtils extends BaseApacheCompressUtils {

	private static ZipUtils mInstance;
	
	/**
	 * 获取ZipUtils单例实例（双重检查锁定模式）
	 * 
	 * @return ZipUtils的单例实例
	 */
	public static ZipUtils getInstance() {
		if(mInstance == null) {
			synchronized (ZipUtils.class) {
				if(mInstance == null) {
					mInstance = new ZipUtils();
				}
			}
		}
		return mInstance;
	}
	
	private ZipUtils() {}
	
	/*============压缩==================*/
	/**
	 * 将文件数组压缩成zip包
	 * 
	 * @param files 需要压缩的文件数组
	 * @param zipPath 生成的zip文件路径
	 * @return 生成的zip文件对象
	 * @throws IOException 如果压缩过程中发生IO异常
	 */
	public  File zip(File[] files, String zipPath) throws IOException {
		return compress(files, zipPath);	//调用父类方法压缩文件
	}
	
	/*============解压==================*/
	/**
	 * 解压zip包到指定路径
	 * 
	 * @param zipPath 需要解压的zip文件路径
	 * @param targetPath 解压的目标路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public  void unZip(String zipPath, String targetPath) throws IOException {
		decompress(zipPath, targetPath);	//调用父类方法解压文件
	}
	
	/*============实现父类方法==================*/
	
	/**
	 * 获取归档条目对象
	 * 
	 * @param filePath 文件在压缩包内的路径
	 * @param file 源文件对象
	 * @param isFile 是否为文件（true为文件，false为目录）
	 * @return ZipArchiveEntry归档条目对象
	 */
	@Override
	public ArchiveEntry getArchiveEntry(String filePath, File file, boolean isFile) {
		ZipArchiveEntry entry = new ZipArchiveEntry(filePath);
		return entry;
	}

	/**
	 * 获取归档输出流
	 * 
	 * @param filePath zip文件的路径
	 * @return ZipArchiveOutputStream归档输出流
	 * @throws FileNotFoundException 如果文件路径无效
	 * @throws IOException 如果创建输出流时发生IO异常
	 */
	@Override
	public ArchiveOutputStream getArchiveOutputStream(String filePath) throws FileNotFoundException, IOException {
		ZipArchiveOutputStream stream = new ZipArchiveOutputStream(new File(filePath));
		stream.setUseZip64(Zip64Mode.AsNeeded); 
		return stream;
	}

	/**
	 * 获取归档输入流
	 * 
	 * @param is 输入流
	 * @return ZipArchiveInputStream归档输入流
	 * @throws FileNotFoundException 如果输入流无效
	 */
	@Override
	public ArchiveInputStream getArchiveInputStream(InputStream is) throws FileNotFoundException {
		return new ZipArchiveInputStream(is);
	}
	
	public static void main(String[] args) throws Exception {
		ZipUtils.getInstance().zip(new File[]{new File("f:\\临时")}, "f:\\a.zip");
//		ZipUtils.getInstance().unZip("f:\\a.zip", "e:\\");
		
//		FileUtils.delete("f:\\a.zip");
//		FileUtils.delete("e:\\a\\");
	}
	
}
