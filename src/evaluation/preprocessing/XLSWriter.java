package evaluation.preprocessing;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import evaluation.preprocessing.ontology.XLSFileData;

public class XLSWriter {
	
	private static Logger log = Logger.getLogger(XLSWriter.class);
	
	public XLSWriter() {
		
	}
	
	/**
	 * 寫出資料到xlsx
	 * Reference : https://gist.github.com/madan712/3912272
	 * @param excelFileName
	 * @param sheetName
	 * @param outXLSFileData
	 * @throws IOException 
	 */
	public void writeOutToFile(String excelFileName, String sheetName, XLSFileData outXLSFileData) throws IOException {
		
		//String excelFileName = "C:/Test.xlsx";//name of excel file
		//String sheetName = "Sheet1";//name of sheet

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheetName) ;
		
		XSSFRow titleRow = sheet.createRow(0);
		for (int columnNumber=0; columnNumber < outXLSFileData.columnTitleList.size() ; columnNumber++ ) {
			XSSFCell cell = titleRow.createCell(columnNumber);
			cell.setCellValue(outXLSFileData.columnTitleList.get(columnNumber));		
		}
		
		//iterating r number of rows
		for (int rowNumber = 0; rowNumber < outXLSFileData.rowsList.size(); rowNumber++)		
		{
			
			XSSFRow row = sheet.createRow(rowNumber+1);

			//iterating c number of columns
			for (int columnNumber=0; columnNumber < outXLSFileData.columnTitleList.size() ; columnNumber++ )
			{
				String columnTitle = outXLSFileData.columnTitleList.get(columnNumber);
				String columnValue = outXLSFileData.rowsList.get(rowNumber).columnValueMap.get(columnTitle);
				
				//if("交通機能".equals(columnTitle)) {
				//	log.debug(rowNumber + ".  交通機能 ==>" + columnValue);
				//}
				
				XSSFCell cell = row.createCell(columnNumber);
				cell.setCellValue(columnValue);
			}
		}

		FileOutputStream fileOut = new FileOutputStream(excelFileName);

		//write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();		
	}

}
