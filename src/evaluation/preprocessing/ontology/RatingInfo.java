package evaluation.preprocessing.ontology;

public class RatingInfo {
	
	public String typeName;
	public int level;			// 1
	public int levelScore;		// 5
	public String levelInfo;	// 100公尺內(5)
	public double trueDistance;
	
	public RatingInfo() {
		this.typeName = "";
		this.level = 0;
		this.levelInfo = "";
		this.trueDistance = 0;
	}
	
	public String toString() {
		return String.valueOf(this.levelScore);
	}
	
}
