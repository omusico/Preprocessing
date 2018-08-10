package evaluation.preprocessing.ontology;

import java.util.LinkedHashMap;
import java.util.Map;

public class RawData {
	
	public int serialID;
	
	public Map<String, String> columnValueMap;
	
	public RawData() {
		this.serialID = 0;
		this.columnValueMap = new LinkedHashMap<String, String>();
	}

}
