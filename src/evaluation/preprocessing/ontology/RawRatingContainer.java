package evaluation.preprocessing.ontology;

import java.util.HashMap;
import java.util.Map;

public class RawRatingContainer {

	//POI
	public Map<String, RatingInfo> positiveInfoMap;
	
	//嫌惡設施評分等級
	public Map<String, RatingInfo> negativeInfoMap;
	
	public RawRatingContainer() {
		this.positiveInfoMap = new HashMap<String, RatingInfo>();
		this.negativeInfoMap = new HashMap<String, RatingInfo>();
	}
	
	public int getRatingScore(boolean positive, String name) {
		RatingInfo targetRatingInfo = null;
		if(positive) {
			targetRatingInfo = this.positiveInfoMap.get(name);
		}else {
			targetRatingInfo = this.negativeInfoMap.get(name);
		}
		
		if(targetRatingInfo == null) {
			return 0;
		}
		return targetRatingInfo.levelScore;
	}
	
	
	public double getRatingDistance(boolean positive, String name) {
		RatingInfo targetRatingInfo = null;
		if(positive) {
			targetRatingInfo = this.positiveInfoMap.get(name);
		}else {
			targetRatingInfo = this.negativeInfoMap.get(name);
		}
		
		if(targetRatingInfo == null) {
			return 0;
		}
		return targetRatingInfo.trueDistance;
	}
	
	
}
