package github.ag777.util.file.excel;

import github.ag777.util.file.FileUtils;
import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.StringUtils;
import github.ag777.util.lang.collection.CollectionAndMapUtils;
import github.ag777.util.lang.exception.Assert;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * Excel文件读取工具类。
 *
 * @see <a href="https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/">Apache POI官方文档</a>
 * @see <a href="https://segmentfault.com/a/1190000012165530">Apache POI 3.17版本更新内容</a>
 * @see <a href="https://www.cnblogs.com/zhangchengbing/p/6340036.html">POI相关Jar包说明</a>
 * @author ag777
 * @version last modify at 2023年03月22日
 */
public class ExcelReadUtils {

	public static void main(String[] args) throws IOException, InvalidFormatException {
		try (Workbook workBook = getWorkBook("d:/a.xls")) {
			workBook.getSheetAt(0).getSheetName();

		}
	}

	/**
	 * 移除角标对应的sheet
	 * @param wb 工作簿
	 * @param index sheet下标
	 * @throws IllegalArgumentException 角标超出范围等等
	 */
	public static void removeSheet(Workbook wb, int index) throws IllegalArgumentException {
		wb.removeSheetAt(index);
	}
	
	/**
	 * 获取sheet名称列表
	 * @param wb 工作簿
	 * @return sheet名称列表
	 */
	public static List<String> getSheetNameList(Workbook wb) {
		List<String> list = new ArrayList<>(5);
		Iterator<Sheet> itor = wb.sheetIterator();
		while(itor.hasNext()) {
			list.add(itor.next().getSheetName());
		}
		return list;
	}
		
	/**
	 * 获取工作簿对象
	 * @param filePath 文件路径
	 * @return 工作簿
	 * @throws IllegalArgumentException 参数异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 * @throws IOException 读取异常
	 */
	public static Workbook getWorkBook(String filePath) throws IllegalArgumentException, EncryptedDocumentException, IOException, InvalidFormatException {
		Assert.notBlank(filePath, "文件路径不能为空");
		return getWorkBook(FileUtils.getInputStream(filePath));
	}
	
	/**
	 * 获取工作簿对象
	 * @param inputStream 流
	 * @return 工作簿
	 * @throws IllegalArgumentException 参数异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 * @throws IOException 读取异常
	 */
	public static Workbook getWorkBook(InputStream inputStream) throws IllegalArgumentException, EncryptedDocumentException, IOException, InvalidFormatException {
		Assert.notNull(inputStream, "文件流不能为空");
		try {
			return WorkbookFactory.create(inputStream);
		} finally {	//测试直接关闭输入流是不影响后续读取的,而且如果想保存回源文件必须先关闭输入流停止文件占用
			IOUtils.close(inputStream);
		}
	}
	
	/**
	 * 获取sheet名称及对应的数据list
	 * @param filePath 文件录进
	 * @param isIgnoreFirstRow 是否跳过第一行(标题)
	 * @return {sheet的名字:[{"a","第一行第一列","b":"第一行第二列"},{"a":"第二行第一列"}]}
	 * @throws IllegalArgumentException 参数异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 * @throws InvalidFormatException 工作簿格式不正确
	 * @throws IOException 读取异常
	 */
	public static Map<String, List<Map<String, String>>> readSheetMap(String filePath, boolean isIgnoreFirstRow) throws IllegalArgumentException, EncryptedDocumentException, InvalidFormatException, IOException {
		Assert.notEmpty(filePath, "参数文件路径不能为空");
		Workbook workBook = getWorkBook(filePath);
		return readSheetMap(workBook, isIgnoreFirstRow);
	}
	
	/**
	 * 从工作簿中获取sheet名称及对应的数据list
	 * @param workBook 工作簿
	 * @param isIgnoreFirstRow 是否跳过第一行(标题)
	 * @return {sheet的名字:[{"a","第一行第一列","b":"第一行第二列"},{"a":"第二行第一列"}]}
	 */
	public static Map<String, List<Map<String, String>>> readSheetMap(Workbook workBook, boolean isIgnoreFirstRow) {
		Map<String, List<Map<String, String>>> sheetMap = new LinkedHashMap<>(workBook.getNumberOfSheets());
		Iterator<Sheet> itorSheet = workBook.sheetIterator();
		while(itorSheet.hasNext()) {
			Sheet sheet = itorSheet.next();
			sheetMap.put(
					sheet.getSheetName(), 
					getRowList(sheet, isIgnoreFirstRow));
		}
		return sheetMap;
	}
	
	/**
	 * 读取excel文件
	 * @param filePath 				excel文件路径
	 * @param sheetTitles			转为map时对应的key
	 * @param isIgnoreFirstRow	是否忽略第一行(有时候第一行是标题栏)
	 * @return 路径为空或者为null时返回null
	 * @throws IOException 读取异常
	 * @throws EncryptedDocumentException 解密工作簿异常
	 */
	public static List<List<Map<String, String>>> read(String filePath, List<List<String>> sheetTitles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<List<Map<String, String>>> read(String filePath, String[][] sheetTitles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<Map<String, String>> read(String filePath, String[] titles, boolean isIgnoreFirstRow) throws IOException, EncryptedDocumentException, InvalidFormatException {//读取excel,不论什么版本
		Workbook workBook = WorkbookFactory.create(new File(filePath));
		List<List<Map<String, String>>> list = readWorkBook(workBook, titles, isIgnoreFirstRow);
		if(CollectionAndMapUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		return list.get(0);
	}
	
	/**
	 * 读取工作簿,用的是抽象类的方法,不区分版本(已去除空数据行)
	 * @param workBook 工作簿
	 * @param sheetTitleList 每个sheet对应的标题列表
	 * @param isIgnoreFirstRow 是否跳过第一行(标题)的读取
	 * @return 所有工作簿中的记录
	 */
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, List<List<String>> sheetTitleList, boolean isIgnoreFirstRow) {
		/*
		 * 二维列表转二维数组
		 */
		String[][] sheetTitles = null;
		if(sheetTitleList != null) {
			sheetTitles = new String[sheetTitleList.size()][];
			for (int i = 0; i < sheetTitleList.size(); i++) {
				List<String> titleList = sheetTitleList.get(i);
				String[] titles = new String[titleList.size()];
				for (int j = 0; j < titleList.size(); j++) {
					String title = titleList.get(j);
					titles[j] = title;
				}
				sheetTitles[i] = titles;
			}
			
		}
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, String[] titles, boolean isIgnoreFirstRow) {
		String[][] sheetTitles = new String[][] {titles};
		return readWorkBook(workBook, sheetTitles, isIgnoreFirstRow);
	}
	
	/**
	 * 读取工作簿,用的是抽象类的方法,不区分版本
	 * @param workBook 工作簿
	 * @param sheetTitles 每个sheet对应的标题列表
	 * @param isIgnoreFirstRow 是否跳过第一行(标题)的读取
	 * @return 有工作簿中的记录
	 */
	public static List<List<Map<String, String>>> readWorkBook(Workbook workBook, String[][] sheetTitles, boolean isIgnoreFirstRow) {
		List<List<Map<String, String>>> sheetList = new ArrayList<>();	//最终结果集
		try {
			// Read the Sheet
			for (int numSheet=0; numSheet < workBook.getNumberOfSheets(); numSheet++) {
				if(sheetTitles.length < numSheet+1) {	//定义标题超出页数则结束遍历
					break;
				}
				String[] titles = sheetTitles[numSheet];
				List<Map<String, String>> rows = new ArrayList<>();	//每个sheet中的内容
				
				Sheet sheet = workBook.getSheetAt(numSheet);
				if (sheet == null) {
					continue;
				}
				
				int rowNum = 0;
				if(isIgnoreFirstRow) {	//忽略第一行则从第二行开始读
					rowNum = 1;
				}
				
				// Read the Row
				for (; rowNum <= sheet.getLastRowNum(); rowNum++) {
					
					Row row = sheet.getRow(rowNum);
					if (row != null) {	
						Map<String, String> item = new HashMap<>();	//每行中的内容
						boolean flag = false;	//排除空行
						int maxColNum = row.getLastCellNum();
						for(int index=0; index<titles.length; index++) {
							String title = titles[index];
							if(index >= maxColNum) {	//标题项目数大于excel中当前行的列数
								item.put(title, null);
							}else {
								Cell xssfCell = row.getCell(index);
								String value = getValue(xssfCell);
								if(value != null) {
									value = value.trim();
									if(!value.isEmpty()) {	//一行当中只要有一个单元格数据不为空则视为有效行(非空行)
										flag = true;
									}
								}
								item.put(title, value);
								
							}
						}//每个标题(titles)或者说列(col)遍历完成
						if(flag) {	//排除空行
							rows.add(item);
						}
					}
					
				}	//行(row)遍历完成
				sheetList.add(rows);
			}	//sheet遍历完成
		} finally {
			IOUtils.close(workBook);	//关闭流，防止文件被占用
		}
		return sheetList;
	}
	
	protected static List<Map<String, String>> getRowList(Sheet sheet, boolean isIgnoreFirstRow) {
		List<Map<String, String>> rowList = new ArrayList<>(sheet.getPhysicalNumberOfRows());
//		String sheetName = sheet.getSheetName();
		Iterator<Row> itorRow = sheet.rowIterator();
		while(itorRow.hasNext()) {
			Row row = itorRow.next();
			Map<String, String> rowMap = new LinkedHashMap<>(row.getLastCellNum());
			if(isIgnoreFirstRow && row.getRowNum() == 0) {
				continue;
			}
			Iterator<Cell> itorCell = row.cellIterator();
			while(itorCell.hasNext()) {
				Cell cell = itorCell.next();
				char key = (char)('a' + cell.getColumnIndex());
				String value = getValue(cell);
				rowMap.put(String.valueOf(key), value);
			}
			rowList.add(rowMap);
		}
		return rowList;
	}
	
	/**
	 * 获取单元格里面的值
	 * @param cell 单元格
	 * @return 单元格中的内容
	 */
	public static String getValue(Cell cell) {
		if(cell == null){
			return null;
		}
		String result;
		if (cell.getCellType() == CellType.BOOLEAN) {
			result =  String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
			Double num = cell.getNumericCellValue();
			if(num.longValue() == num){
				result = String.valueOf(num.longValue());
			} else {
				result = String.valueOf(num);
			}
		} else {
			result =  cell.getStringCellValue();
		}
		return !StringUtils.isEmpty(result)?result:null;
	}
	
	/** 
     * 判断是否为excel2007及以上 
	 * @throws IOException 读取异常
	 * @throws InvalidFormatException 工作簿格式不正确
     */  
    public static  boolean isExcel2007(String filePath) throws IOException, InvalidFormatException {
    	
    	InputStream is = null;
        try {  
        	is = Files.newInputStream(Paths.get(filePath));
        	return isExcel2007(is);
        } finally {
        	IOUtils.close(is);
        }
        
    }  
    
    /**
     * @param is 输入流
     * @return 是否为excel2007及以上(是否是xlsx)
     * @throws IOException 读取异常
     * @throws InvalidFormatException 工作簿格式不正确
     */
	public static  boolean isExcel2007(InputStream is) throws IOException, InvalidFormatException {
    	// If clearly doesn't do mark/reset, wrap up  
        if(! is.markSupported()) {  //关于该方法:https://blog.csdn.net/jektonluo/article/details/49588673
        	is = new PushbackInputStream(is, 8);  
        } 
        /*
         * 官方文档:
         * Get the file magic of the supplied InputStream (which MUST support mark and reset).
			If unsure if your InputStream does support mark / reset, use prepareToCheckMagic(InputStream) to wrap it and make sure to always use that, and not the original!
			Even if this method returns UNKNOWN it could potentially mean, that the ZIP stream has leading junk bytes
         */
        FileMagic fileMagic = FileMagic.valueOf(FileMagic.prepareToCheckMagic(is));
        /*
         * 官方文档:
         * 	hasOOXMLHeader(java.io.InputStream inp)
			Deprecated. 
         * in 3.17-beta2, use FileMagic.valueOf(InputStream) == FileMagic.OOXML instead
         */
        if(fileMagic == FileMagic.OOXML) {  
    		//Excel版本为excel2007及以上  
    		return true;
        } else if(fileMagic == FileMagic.OLE2) {
        	return false;
        }
    	
    	throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream"); 
    }
   
}
