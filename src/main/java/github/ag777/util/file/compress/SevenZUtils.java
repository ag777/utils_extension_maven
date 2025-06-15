package github.ag777.util.file.compress;

import github.ag777.util.file.FileUtils;
import github.ag777.util.file.compress.base.BaseApacheCompressUtils;
import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.exception.Assert;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.*;

/**
 * 有关zip文件的压缩和解压的工具基类,commons-compress二次封装
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.17.jar</li>
 * <li>xz-1.8.jar</li>
 * </ul>
 * 缺少xz这个包在压缩有内容的文件时会报java.lang.NoClassDefFoundError: org/tukaani/xz/FilterOptions异常(反之压缩空文件和文件夹时任何毛病)
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月16日,last modify at 2025年06月15日
 */
public class SevenZUtils {
	
	//-压缩
	/**
	 * 打包压缩7z包
	 * 
	 * @param files 需要压缩的文件数组
	 * @param packagePath 生成的7z压缩文件路径
	 * @return 生成的7z文件对象
	 * @throws IOException 如果文件不存在或压缩过程中发生IO异常
	 */
	public static File compress(File[] files, String packagePath) throws IOException {
		Assert.notEmpty(files, "至少选择压缩一个文件");
		for (File f : files) {
			Assert.notExisted(f, "需要压缩的文件不存在:" + f.getAbsolutePath());
		}
		SevenZOutputFile tos = null;
		try {
			tos = getSevenZOutputFile(packagePath);
			for (File fi : files) {
				if (fi.isDirectory()) {
					addDir(tos, fi, null);
				} else {
					addFile(tos, fi, null);
				}
			}
			return new File(packagePath);
		} catch (Exception ex) {
			FileUtils.delete(packagePath);
			throw ex;
		} finally {
			IOUtils.close(tos);
		}
	}

	//-解压
	/**
	 * 解压7z压缩包
	 * 
	 * @param packagePath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void decompress(String packagePath, String targetPath) throws IOException {
		Assert.notExisted(packagePath, "需要解压的文件不存在:" + packagePath);
		SevenZFile tais = null;
		try {
			File sevenZFile = new File(packagePath);
			tais = new SevenZFile(sevenZFile);
			ArchiveEntry entry = null;
			while ((entry = tais.getNextEntry()) != null) {
				
				// 文件
				String dir = targetPath + File.separator + entry.getName();

				File dirFile = new File(dir);

				// 文件检查
//				FileUtils.makeDir(dirFile.getParent(), true);

				if (entry.isDirectory()) {
					dirFile.mkdirs();
				} else {
					BufferedOutputStream bos = null;
					try {	//必须在这层包try-catch并及时关闭输出流，不然会导致输出空文件，而且解压后也无法删除文件(被占用)
						bos = new BufferedOutputStream(new FileOutputStream(dirFile));
	
						int count;
						byte data[] = new byte[BaseApacheCompressUtils.BUFFER];
						while ((count = tais.read(data, 0, BaseApacheCompressUtils.BUFFER)) != -1) {
							bos.write(data, 0, count);
						}
					} catch(Exception ex) {
						throw ex;
					} finally {
						IOUtils.close(bos);
					}
				}

			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(tais);
		}
	}
	
	/**
	 * 解压密码保护的7z压缩包
	 * 
	 * @param packagePath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @param password 解压密码（支持中文字符）
	 * @throws IOException 如果文件不存在或解压过程中发生IO异常
	 */
	public static void decompressWithPassword(String packagePath, String targetPath, String password) throws IOException {
		decompressWithPassword(packagePath, targetPath, password.toCharArray());
	}
	
	/**
	 * 解压密码保护的7z压缩包
	 * 
	 * @param packagePath 需要解压的7z文件路径
	 * @param targetPath 解压的目标路径
	 * @param password 解压密码字符数组（支持中文字符，更安全）
	 * @throws IOException 如果文件不存在或解压过程中发生IO异异常
	 */
	public static void decompressWithPassword(String packagePath, String targetPath, char[] password) throws IOException {
		Assert.notExisted(packagePath, "需要解压的文件不存在:" + packagePath);
		SevenZFile sevenZFile = null;
		try {
			File archiveFile = new File(packagePath);
			// 使用密码创建SevenZFile
			sevenZFile = new SevenZFile(archiveFile, password);
			
			ArchiveEntry entry = null;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				
				// 文件
				String dir = targetPath + File.separator + entry.getName();

				File dirFile = new File(dir);

				// 文件检查
//				FileUtils.makeDir(dirFile.getParent(), true);

				if (entry.isDirectory()) {
					dirFile.mkdirs();
				} else {
					BufferedOutputStream bos = null;
					try {	//必须在这层包try-catch并及时关闭输出流，不然会导致输出空文件，而且解压后也无法删除文件(被占用)
						bos = new BufferedOutputStream(new FileOutputStream(dirFile));
	
						int count;
						byte data[] = new byte[BaseApacheCompressUtils.BUFFER];
						while ((count = sevenZFile.read(data, 0, BaseApacheCompressUtils.BUFFER)) != -1) {
							bos.write(data, 0, count);
						}
					} catch(Exception ex) {
						throw ex;
					} finally {
						IOUtils.close(bos);
					}
				}

			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(sevenZFile);
		}
	}
	
	/*====================内部方法=================*/
	/**
	 * 归档目录到7z压缩包
	 * 
	 * @param os SevenZOutputFile输出文件对象
	 * @param file 需要归档的目录文件对象
	 * @param baseDir 基础目录路径，用于构建相对路径
	 * @throws IOException 如果归档过程中发生IO异常
	 */
	private static void addDir(SevenZOutputFile os, File file, String baseDir) throws IOException {
		/* 先创建文件夹，防止空文件夹不计入tar包 */
		String dir = file.getName()+File.separator;
    	if(baseDir != null) {
    		dir = baseDir + dir;
    	}
    	ArchiveEntry dirEntry = getArchiveEntry(os, dir, file, false);
    	if(dirEntry != null) {
    		os.putArchiveEntry(dirEntry);
    	}
		
		/* 将文件夹的子文件列表放入tar包 */
		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (f.isDirectory()) {
				addDir(os, f, dir);
			} else {
				addFile(os, f, dir);
			}
		}
	}
	
	/**
	 * 归档单个文件到7z压缩包
	 * 
	 * @param os SevenZOutputFile输出文件对象
	 * @param file 需要归档的文件对象
	 * @param baseDir 基础目录路径，用于构建相对路径
	 * @throws IOException 如果归档过程中发生IO异常
	 */
	private static void addFile(SevenZOutputFile os, File file, String baseDir) throws IOException {
		BufferedInputStream bis = null;
    	try {
    		String path = file.getName();
        	if(baseDir != null) {
        		path = baseDir + path;
        	}
        	ArchiveEntry entry = getArchiveEntry(os, path, file, true);	//压缩包内对象
        	if(entry != null) {
        		os.putArchiveEntry(entry);
        	}
	    	bis = FileUtils.getBufferedInputStream(file);
	    	
	
	        byte[] buffer = new byte[BaseApacheCompressUtils.BUFFER];
	        int read = -1;
	        while((read = bis.read(buffer)) != -1){
	            os.write(buffer, 0 , read);
	        }
    	} catch(Exception ex) {
    		throw ex;
    	} finally {
    		IOUtils.close(bis);
    		os.closeArchiveEntry();//这里必须写，否则会失败
    	}
	}
	
	/**
	 * 创建7z归档条目对象
	 * 
	 * @param sevenZFile 7z输出文件对象
	 * @param filePath 文件在压缩包内的路径
	 * @param file 源文件对象
	 * @param isFile 是否为文件（true为文件，false为目录）
	 * @return SevenZArchiveEntry归档条目对象
	 * @throws IOException 如果创建归档条目时发生IO异常
	 */
	private static ArchiveEntry getArchiveEntry(SevenZOutputFile sevenZFile, String filePath, File file, boolean isFile) throws IOException {
		if(!isFile) {	//去除文件夹路径的最后一个文件分隔符，否则在压缩包内会在子路径创建一个空名字的空文件夹
			filePath = filePath.substring(0, filePath.length()-1);
		}
		SevenZArchiveEntry entry = sevenZFile.createArchiveEntry(file, filePath);
		return entry;
	}

	
	/**
	 * 获取7z输出文件对象
	 * 
	 * @param filePath 7z文件路径
	 * @return SevenZOutputFile输出文件对象
	 * @throws FileNotFoundException 如果文件路径无效
	 * @throws IOException 如果创建输出文件时发生IO异常
	 */
	private static SevenZOutputFile getSevenZOutputFile(String filePath) throws FileNotFoundException, IOException {
        return new SevenZOutputFile(new File(filePath));
	}

	
	public static void main(String[] args) throws IOException {
		SevenZUtils.compress(new File[]{new File("f:\\临时\\")}, "f:\\a.7z");
		SevenZUtils.decompress("f:\\a.7z", "e:\\");
	}
}
