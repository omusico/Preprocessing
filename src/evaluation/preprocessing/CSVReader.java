package evaluation.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import evaluation.preprocessing.ontology.RawData;
import evaluation.preprocessing.ontology.XLSFileData;

public class CSVReader {
	
	private static Logger log = Logger.getLogger(CSVReader.class);
	
	private XLSFileData returnXLSFileData;
	
	public CSVReader() {
		
	}
	
	public XLSFileData readFile(String filePath) {
		
		this.returnXLSFileData = new XLSFileData();
		
		try {
			File fileDir = new File(filePath);
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(fileDir), "UTF8"));
			
			int rowCount = 0;
			boolean isTitleLine = true;
			String targetLine;
			while ((targetLine = in.readLine()) != null) {
				if("".equals(targetLine.trim())) {
					continue;
				}
				
				String[] columns = targetLine.split(",");
				
				if(isTitleLine) {
					for(String targetColumnValue : columns) {
						returnXLSFileData.columnTitleList.add(targetColumnValue);
					}
					isTitleLine= false;
				}else {
					
					if(this.returnXLSFileData.columnTitleList.size() != columns.length) {
						continue;
					}
					
					rowCount++;
					
					RawData newRowData = new RawData();
					newRowData.serialID = rowCount;					
					
					for(int mover=0; mover < this.returnXLSFileData.columnTitleList.size() ; mover++) {
						String totleColumnValue = this.returnXLSFileData.columnTitleList.get(mover);
						newRowData.columnValueMap.put(totleColumnValue, columns[mover]);
					}
					
					this.returnXLSFileData.rowsList.add(newRowData);
					
				}
				
			}
			        
            in.close();
        }catch (Exception e)
	    {
        	log.fatal("", e);
	    }
		
		return this.returnXLSFileData;
	}		
	
	public static void main(String[] args) {
		CSVReader newCSVReader = new CSVReader();
		newCSVReader.readFile("/Users/sunjiancheng/Downloads/highwayData.csv");
	}
		
	
}
