package evaluation.preprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import evaluation.preprocessing.ontology.RawData;
import evaluation.preprocessing.ontology.XLSFileData;

public class XLSReader {
	
	private static Logger log = Logger.getLogger(XLSReader.class);
	
	private XLSFileData returnXLSFileData;
	
	public XLSReader() {
		
	}
	
	/**
	 * 收集Title Info
	 * @param filePath
	 * @param workSheet
	 * @throws IOException
	 */
	private void collectColumnTitleInfo(String filePath, String workSheet, boolean xlsx) throws IOException {
		
		log.debug("File Info ==>" + filePath);
		File myFile = new File(filePath);
		FileInputStream fis = new FileInputStream(myFile);
		
		Iterator<Row> rowIterator = null;
		if(xlsx) {
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
			
			XSSFSheet mySheet = myWorkBook.getSheet(workSheet);
			
			rowIterator = mySheet.iterator();
			
		}else {
			
			HSSFWorkbook myWorkBook = new HSSFWorkbook(fis);
			
			HSSFSheet mySheet = myWorkBook.getSheet(workSheet);
			
			rowIterator = mySheet.iterator();
		}
		
		int rowCount = 0;
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next(); // For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();
			
			rowCount++;
			
			int columnCount = 0;
			
			while (cellIterator.hasNext()) {
				
				Cell cell = cellIterator.next();
				
				columnCount++;
				
				String value = null;
				switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						value = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_NUMERIC:
						value = String.valueOf((int)cell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						value = String.valueOf(cell.getBooleanCellValue());
						break;
					default:
						throw new IOException("不預期的型態~~");
				}
				
				if(value == null || "".equals(value.trim())) {
					continue;
				}
				
				//log.debug("Column Title ==>" + value);
				//log.debug("this.returnXLSFileData.columnTitleList ==>" + this.returnXLSFileData.columnTitleList);
				
				this.returnXLSFileData.columnTitleList.add(value);
			}
			
			break;
		}
	}

	/**
	 * 收集xlsx Info
	 * @param filePath
	 * @param workSheet
	 * @return XLSFileData
	 * @throws IOException
	 */
	public XLSFileData readData(String filePath, String workSheet, boolean xlsx) throws IOException {
		
		this.returnXLSFileData = new XLSFileData();
		
		collectColumnTitleInfo(filePath, workSheet, xlsx);
		
		File myFile = new File(filePath);
		FileInputStream fis = new FileInputStream(myFile);
		
		Iterator<Row> rowIterator = null;
		if(xlsx) {
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
			
			XSSFSheet mySheet = myWorkBook.getSheet(workSheet);
			
			rowIterator = mySheet.iterator();
			
		}else {
			
			HSSFWorkbook myWorkBook = new HSSFWorkbook(fis);
			
			HSSFSheet mySheet = myWorkBook.getSheet(workSheet);
			
			rowIterator = mySheet.iterator();
		}
		
		
		int rowCount = 0;
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next(); // For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();
			
			rowCount++;
			
			if(rowCount == 1) {
				continue;
			}

			int columnCount = 0;
			
			RawData newRowData = new RawData();
			newRowData.serialID = rowCount;
			
			while (cellIterator.hasNext()) {
				
				Cell cell = cellIterator.next();
				
				columnCount++;
				
				String value = null;
				switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						value = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_NUMERIC:
						value = String.valueOf((int)cell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						value = String.valueOf(cell.getBooleanCellValue());
						break;
					default:
						value = cell.toString();
						
						if(value == null || "".equals(value)) {
							value = "";
						}else {
							String titleColumnValue = this.returnXLSFileData.columnTitleList.get(columnCount-1);
							throw new IOException("不預期的型態~~" + rowCount + "." + titleColumnValue + " value." + value);
						}
				}
				
				if(value == null || "".equals(value.trim())) {
					continue;
				}
				
				String titleColumnValue = this.returnXLSFileData.columnTitleList.get(columnCount-1);
				
				//log.debug("Column Value ==>" + rowCount + ". "  + titleColumnValue + "(" + value + ")");
				
				newRowData.columnValueMap.put(titleColumnValue, value);
			}
			
			this.returnXLSFileData.rowsList.add(newRowData);
			
		}
		
		return returnXLSFileData;
	}
	
	public static void main(String[] args) {
		//String filePath = "/Users/sunjiancheng/Downloads/nckugeo01/POI_list.xlsx";
		//String workSheet = "工作表1";
		
		String filePath = "/Users/sunjiancheng/Downloads/nckugeo01/實價登錄資料.xls";
		String workSheet = "工作表1";
		
		XLSReader newXLSReader = new XLSReader();
		try {
			newXLSReader.readData(filePath, workSheet, false);
		} catch (IOException e) {
			log.fatal("", e);
		}
	}
	
}
