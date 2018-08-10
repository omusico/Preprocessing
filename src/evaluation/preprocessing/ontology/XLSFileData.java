package evaluation.preprocessing.ontology;

import java.util.ArrayList;
import java.util.List;

public class XLSFileData {
	
	public List<String> columnTitleList;
	
	public List<RawData> rowsList;
	
	public XLSFileData() {
		this.columnTitleList = new ArrayList<String>();
		this.rowsList = new ArrayList<RawData>();
	}

}
