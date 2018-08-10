package evaluation.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import evaluation.preprocessing.CoordinateTransform.TransformedPosition;
import evaluation.preprocessing.ontology.RatingInfo;
import evaluation.preprocessing.ontology.RawData;
import evaluation.preprocessing.ontology.RawRatingContainer;
import evaluation.preprocessing.ontology.XLSFileData;

public class Preprocessor {
	
	private static Logger log = Logger.getLogger(Preprocessor.class);
	
	private File rawExcelFilePath;
	private File poiExcelFilePath;
	private File outExcelFilePath;
	private String rawExcelSheet;
	private String poiExcelSheet;
	private String outExcelSheet;
	private boolean isRawXlsx;
	private boolean isPOIXlsx;
	
	private XLSFileData rawData;
	private XLSFileData poiData;
	private XLSFileData additionalData;
	
	private File additionalPOIFilePath;

	/**
	 * 各式項目評分值
	 * : 捷運	國道	鐵路	公園	小學國高中	百貨購物中心
	 * : 寺廟、神壇	變電所、焚化爐	高壓電塔	加油站、瓦斯槽	殯儀館、墓地	垃圾場	特種營業	工業區
	 */
	private List<RawRatingContainer> rawDataRatingInfoList;
	
	/**
	 * 交通機能
	 * 
	 */
	private List<Integer> transportationScoreList;
	
	/**
	 * 生活機能
	 */
	private List<Integer> lifeComforatbleScoreList;
	
	/**
	 * 嫌惡設施
	 */
	private List<Integer> negativeScoreList;
	
	
	/**
	 * Constructor
	 * @param rawExcelFilePath
	 * @param rawSheet
	 * @param poiExcelFilePath
	 * @param poiSheet
	 */
	public Preprocessor(String rawExcelFilePath, String rawSheet, 
						String poiExcelFilePath, String poiSheet,
						String additionalPOIFilePath,
						String outExcelFilePath, String outSheet
	) {
		this.rawExcelFilePath = new File(rawExcelFilePath);
		this.poiExcelFilePath = new File(poiExcelFilePath);
		this.outExcelFilePath = new File(outExcelFilePath);
		this.additionalPOIFilePath = new File(additionalPOIFilePath);
		
		this.rawExcelSheet = rawSheet;
		this.poiExcelSheet = poiSheet;
		this.outExcelSheet = outSheet;
	}
	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String rawExcelFilePath = "/Users/sunjiancheng/Downloads/nckugeo01/實價登錄資料.xls";
		String rawSheet = "工作表1";
		String poiExcelFilePath = "/Users/sunjiancheng/Downloads/nckugeo01/POI_list.xlsx";
		String poiSheet = "工作表1";
		
		String additionalPOIFilePath = "/Users/sunjiancheng/Downloads/additionPOIData.csv";
		
		String outExcelFilePath = "/Users/sunjiancheng/Downloads/nckugeo01/output.xlsx";
		String outSheet = "整理後輸出";
		
		Preprocessor usePreprocessor = new Preprocessor(	rawExcelFilePath, rawSheet, 
															poiExcelFilePath, poiSheet, 
															additionalPOIFilePath, 
															outExcelFilePath, outSheet);
		usePreprocessor.preprocess();
	}
	
	/**
	 * 前處理
	 * @throws IOException
	 */
	public void preprocess() throws IOException {
		
		if(this.rawExcelFilePath.toString().endsWith("xlsx")) {
			isRawXlsx = true;
		}else {
			isRawXlsx = false;
		}
		
		if(this.poiExcelFilePath.toString().endsWith("xlsx")) {
			isPOIXlsx = true;
		}else {
			isPOIXlsx = false;
		}
		
		this.transportationScoreList = new ArrayList<Integer>();
		this.lifeComforatbleScoreList = new ArrayList<Integer>();
		this.negativeScoreList = new ArrayList<Integer>();
			
		/*
		 * Raw
		 */
		XLSReader useXLSReader = new XLSReader();
		this.rawData = useXLSReader.readData(rawExcelFilePath.toString(), rawExcelSheet, isRawXlsx);
		
		/*
		 * POI 
		 */
		useXLSReader = new XLSReader();
		this.poiData = useXLSReader.readData(this.poiExcelFilePath.toString(), poiExcelSheet, isPOIXlsx);
		
		/*
		 * 國道資料
		 */
		CSVReader useCSVReader = new CSVReader();
		this.additionalData = useCSVReader.readFile(this.additionalPOIFilePath.toString());
		
		/*
		 * 評分資料
		 */
		this.rawDataRatingInfoList = new ArrayList<RawRatingContainer>(rawData.rowsList.size());
		
		log.debug("start to comput rating ~");
		
		log.debug("roadList.size() ==>" + this.additionalData.rowsList.size());
		/*
		 * 收集評分資料
		 */
		for(RawData rowData : this.rawData.rowsList) {
			computeRating(rowData, poiData.rowsList, this.additionalData.rowsList);
			//System.exit(0);
		}
		
		this.rawData.columnTitleList.add("交通機能");
		this.rawData.columnTitleList.add("生活機能");
		this.rawData.columnTitleList.add("嫌惡設施");
		//DISLIKE_SCORE=-1, 
		this.rawData.columnTitleList.add("DISLIKE_SCORE");
		//HAS_DISLIKE='N', 
		this.rawData.columnTitleList.add("HAS_DISLIKE");
		//TRAFFIC=1, 
		this.rawData.columnTitleList.add("TRAFFIC");
		//LIVING=14, 
		this.rawData.columnTitleList.add("LIVING");
		//MRT_SCORE=0, 
		this.rawData.columnTitleList.add("MRT_SCORE");
		//PARK_SCORE=1, 
		this.rawData.columnTitleList.add("PARK_SCORE");
		//SCHOOL_SCORE=2, 
		this.rawData.columnTitleList.add("SCHOOL_SCORE");
		//STORE_SCORE=2, 
		this.rawData.columnTitleList.add("STORE_SCORE");
		//HWAY_SCORE=1, 
		this.rawData.columnTitleList.add("HWAY_SCORE");
		//RAIL_SCORE=0, 
		this.rawData.columnTitleList.add("RAIL_SCORE");
		//HOSPITAL_SCORE=0, 
		this.rawData.columnTitleList.add("HOSPITAL_SCORE");
		//PHARMACY_SCORE=3, 
		this.rawData.columnTitleList.add("PHARMACY_SCORE");
		//CINEMA_SCORE=2, 
		this.rawData.columnTitleList.add("CINEMA_SCORE");
		//CNVCSTORE_SCORE=2, 
		this.rawData.columnTitleList.add("CNVCSTORE_SCORE");
		//FASTFOOD_SCORE=3, 
		this.rawData.columnTitleList.add("FASTFOOD_SCORE");
		//MARKET_SCORE=1, 
		this.rawData.columnTitleList.add("MARKET_SCORE");
		//OILSTATION_SCORE=
		this.rawData.columnTitleList.add("OILSTATION_SCORE");
		//MRT_DISTANCE_SQL=1825.1602272526907, 
		this.rawData.columnTitleList.add("MRT_DISTANCE_SQL");
		//PARK_DISTANCE_SQL=214.80777890934883, 
		this.rawData.columnTitleList.add("PARK_DISTANCE_SQL");
		//SCHOOL_DISTANCE_SQL=365.60392321291147, 
		this.rawData.columnTitleList.add("SCHOOL_DISTANCE_SQL");
		//STORE_DISTANCE_SQL=448.35970998549783, 
		this.rawData.columnTitleList.add("STORE_DISTANCE_SQL");
		//HWAY_DISTANCE_SQL=1825.1602272526907, 
		this.rawData.columnTitleList.add("HWAY_DISTANCE_SQL");
		//RAIL_DISTANCE_SQL=5624.931424941081, 
		this.rawData.columnTitleList.add("RAIL_DISTANCE_SQL");
		//HOSPITAL_DISTANCE_SQL=2270.637538598054, 
		this.rawData.columnTitleList.add("HOSPITAL_DISTANCE_SQL");
		//PHARMACY_DISTANCE=206.4428752875027, 
		this.rawData.columnTitleList.add("PHARMACY_DISTANCE");
		//CINEMA_DISTANCE=1095.0737001523023, 
		this.rawData.columnTitleList.add("CINEMA_DISTANCE");
		//CNVCSTORE_DISTANCE=102.75128325229747, 
		this.rawData.columnTitleList.add("CNVCSTORE_DISTANCE");
		//FASTFOOD_DISTANCE=155.16789622475508, 
		this.rawData.columnTitleList.add("FASTFOOD_DISTANCE");
		//MARKET_DISTANCE=1443.4529187884052, 
		this.rawData.columnTitleList.add("MARKET_DISTANCE");
		//OILSTATION_DISTANCE=295.13752859043865,		
		this.rawData.columnTitleList.add("OILSTATION_DISTANCE");		
		
		log.debug("rawDataRatingInfoList.size() ==>" + rawDataRatingInfoList.size());
		
//POI_KIND
		
		/*
		 * 計算最終評分資料　＆　輸出
		 */
		for(int mover=0; mover < rawData.rowsList.size() ; mover++) {
			
			RawRatingContainer targetRawRatingContainer = rawDataRatingInfoList.get(mover);
			
			//交通機能評分 = 鐵路評分 + 國道評分
			//生活機能評分 = 2 * 公園評分+ 學校評分 + 5 * 百貨購物評分 + 護理機構 + 電影院 + 便利商店 + 大型連鎖速食店
			//嫌惡設施 = 寺廟、神壇 + 變電所、焚化爐 + 加油站、瓦斯槽 + 殯儀館、墓地 + 垃圾場 + 特種營業 + 工業區
			
			//private List<Integer> transportationScore;
			Integer transportationScore = 0;
			transportationScore +=  targetRawRatingContainer.getRatingScore(true, "鐵路");
			transportationScore +=  targetRawRatingContainer.getRatingScore(true, "國道");
			//if(transportationScore > 0) {
			//	log.debug("國道 ==>" + transportationScore);
			//}			
			
			//private List<Integer> lifeComforatbleScore;
			Integer lifeComforatbleScore = 0;
			lifeComforatbleScore +=  2 * targetRawRatingContainer.getRatingScore(true, "公園");
			lifeComforatbleScore +=  targetRawRatingContainer.getRatingScore(true, "小學國高中");
			lifeComforatbleScore +=  5 * targetRawRatingContainer.getRatingScore(true, "百貨購物");
//自已加的~~~
			lifeComforatbleScore +=  targetRawRatingContainer.getRatingScore(true, "護理機構");
			lifeComforatbleScore +=  targetRawRatingContainer.getRatingScore(true, "電影院");
			lifeComforatbleScore +=  targetRawRatingContainer.getRatingScore(true, "便利商店");
			lifeComforatbleScore +=  targetRawRatingContainer.getRatingScore(true, "大型連鎖速食店");
			
			
			
			//private List<Integer> negativeScore;		
			//嫌惡設施 = 寺廟、神壇 + 變電所、焚化爐 + 加油站、瓦斯槽 + 殯儀館、墓地 + 垃圾場 + 特種營業 + 工業區
			Integer negativeScore = 0;
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "寺廟、神壇");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "變電所、焚化爐");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "加油站、瓦斯槽");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "殯儀館、墓地");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "垃圾場");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "特種營業");
			negativeScore -=  targetRawRatingContainer.getRatingScore(false, "工業區");
			
			this.transportationScoreList.add(transportationScore);
			this.lifeComforatbleScoreList.add(lifeComforatbleScore);
			this.negativeScoreList.add(negativeScore);
			
			rawData.rowsList.get(mover).columnValueMap.put("交通機能", transportationScore.toString());
			rawData.rowsList.get(mover).columnValueMap.put("生活機能", lifeComforatbleScore.toString());
			rawData.rowsList.get(mover).columnValueMap.put("嫌惡設施", negativeScore.toString());
			
			//DISLIKE_SCORE=-1, 
			//HAS_DISLIKE='N', 
			if(negativeScore == 0) {
				rawData.rowsList.get(mover).columnValueMap.put("DISLIKE_SCORE", "0");
				rawData.rowsList.get(mover).columnValueMap.put("HAS_DISLIKE", "N");
			}else {
				rawData.rowsList.get(mover).columnValueMap.put("DISLIKE_SCORE", negativeScore.toString());
				rawData.rowsList.get(mover).columnValueMap.put("HAS_DISLIKE", "Y");
			}
			//TRAFFIC=1, 
			if(transportationScore == 0) {
				rawData.rowsList.get(mover).columnValueMap.put("TRAFFIC", "0");
			}else {
				rawData.rowsList.get(mover).columnValueMap.put("TRAFFIC", transportationScore.toString());
			}
			//LIVING=14, 
			rawData.rowsList.get(mover).columnValueMap.put("LIVING", lifeComforatbleScore.toString());
			//MRT_SCORE=0, 
			rawData.rowsList.get(mover).columnValueMap.put("MRT_SCORE", "0");
			//PARK_SCORE=1, 
			rawData.rowsList.get(mover).columnValueMap.put("PARK_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "公園")));
			//SCHOOL_SCORE=2, 
			rawData.rowsList.get(mover).columnValueMap.put("SCHOOL_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "小學國高中")));
			//STORE_SCORE=2, 
			rawData.rowsList.get(mover).columnValueMap.put("STORE_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "百貨購物")));
			//HWAY_SCORE=1, 
			rawData.rowsList.get(mover).columnValueMap.put("HWAY_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "國道")));
			//RAIL_SCORE=0, 
			rawData.rowsList.get(mover).columnValueMap.put("RAIL_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "鐵路")));
			//HOSPITAL_SCORE=0, 
			rawData.rowsList.get(mover).columnValueMap.put("HOSPITAL_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "護理機構")));
			//PHARMACY_SCORE=3, 
			rawData.rowsList.get(mover).columnValueMap.put("PHARMACY_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "藥房")));
			//CINEMA_SCORE=2, 
			rawData.rowsList.get(mover).columnValueMap.put("CINEMA_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "電影院")));
			//CNVCSTORE_SCORE=2, 
			rawData.rowsList.get(mover).columnValueMap.put("CNVCSTORE_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "便利商店")));
			//FASTFOOD_SCORE=3, 
			rawData.rowsList.get(mover).columnValueMap.put("FASTFOOD_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "大型連鎖速食店")));
			//MARKET_SCORE=1, 
			rawData.rowsList.get(mover).columnValueMap.put("MARKET_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(true, "傳統市場")));
			//OILSTATION_SCORE=
			rawData.rowsList.get(mover).columnValueMap.put("OILSTATION_SCORE", String.valueOf(targetRawRatingContainer.getRatingScore(false, "加油站、瓦斯槽")));
			
			//MRT_DISTANCE_SQL=1825.1602272526907, 
			rawData.rowsList.get(mover).columnValueMap.put("MRT_DISTANCE_SQL", "0");
			//PARK_DISTANCE_SQL=214.80777890934883, 
			rawData.rowsList.get(mover).columnValueMap.put("PARK_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "公園")));
			//SCHOOL_DISTANCE_SQL=365.60392321291147, 
			rawData.rowsList.get(mover).columnValueMap.put("SCHOOL_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "小學國高中")));
			//STORE_DISTANCE_SQL=448.35970998549783, 
			rawData.rowsList.get(mover).columnValueMap.put("STORE_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "百貨購物")));
			//HWAY_DISTANCE_SQL=1825.1602272526907, 
			rawData.rowsList.get(mover).columnValueMap.put("HWAY_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "國道")));
			//RAIL_DISTANCE_SQL=5624.931424941081, 
			rawData.rowsList.get(mover).columnValueMap.put("RAIL_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "鐵路")));
			//HOSPITAL_DISTANCE_SQL=2270.637538598054, 
			rawData.rowsList.get(mover).columnValueMap.put("HOSPITAL_DISTANCE_SQL", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "護理機構")));
			//PHARMACY_DISTANCE=206.4428752875027, 
			rawData.rowsList.get(mover).columnValueMap.put("PHARMACY_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "藥房")));
			//CINEMA_DISTANCE=1095.0737001523023, 
			rawData.rowsList.get(mover).columnValueMap.put("CINEMA_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "電影院")));
			//CNVCSTORE_DISTANCE=102.75128325229747, 
			rawData.rowsList.get(mover).columnValueMap.put("CNVCSTORE_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "便利商店")));
			//FASTFOOD_DISTANCE=155.16789622475508, 
			rawData.rowsList.get(mover).columnValueMap.put("FASTFOOD_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "大型連鎖速食店")));
			//MARKET_DISTANCE=1443.4529187884052, 
			rawData.rowsList.get(mover).columnValueMap.put("MARKET_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(true, "傳統市場")));
			//OILSTATION_DISTANCE=295.13752859043865,			
			rawData.rowsList.get(mover).columnValueMap.put("OILSTATION_DISTANCE", String.valueOf(targetRawRatingContainer.getRatingDistance(false, "加油站、瓦斯槽")));
			
		}
		
		XLSWriter newXLSWriter = new XLSWriter();
		newXLSWriter.writeOutToFile(this.outExcelFilePath.toString(), this.outExcelSheet, rawData);
	}

	/**
	 * 計算各類Score
	 * @param targetRowData
	 * @param poiList
	 */
	private void computeRating(RawData targetRowData, List<RawData> poiList, List<RawData> additionalList) {
		
		//鐵路 : 汽車轉運站, 火車站
		//公園 : 公園, 體育場, 廣場, 綠園道
		//小學國高中 : 小學, 國中, 高中, 大專院校
		//百貨購物 : 大型休閒遊樂場所, 大型連鎖購物中心
		//護理機構 : 護理機構, 大型醫院
		//電影院 : 電影院
		//便利商店 : 7 11, OK, 全家, 萊爾富, 福客多		
		//大型連鎖速食店 : 大型連鎖速食店, 大型餐飲樓
		
		//嫌惡設施 --->
		//寺廟、神壇 : 寺廟	
		//變電所、焚化爐 : 變電所
		//加油站 : 加油站
		//殯儀館、墓地 : 殯儀館
		//垃圾場 : 垃圾掩埋場, 垃圾焚化廠, 垃圾分類廠, 垃圾處理場
		
		//log.debug("targetRowData.columnValueMap.keySet() ==>" + targetRowData.columnValueMap.keySet());
		double rawDataX = Double.valueOf(targetRowData.columnValueMap.get("Response_X"));
		double rawDataY = Double.valueOf(targetRowData.columnValueMap.get("Response_Y"));
		
		CoordinateTransform newCoordinateTransform = new CoordinateTransform();
		TransformedPosition returnCoordinateTransform = newCoordinateTransform.TWD97_To_lonlat(rawDataX, rawDataY);
		//log.debug("after rawDataX ==>" + rawDataX + "->" + returnCoordinateTransform.x);
		//log.debug("after rawDataY ==>" + rawDataY + "->" + returnCoordinateTransform.y);		
		rawDataX = returnCoordinateTransform.x;
		rawDataY = returnCoordinateTransform.y;
		
		RawRatingContainer newRawRatingContainer = new RawRatingContainer();
		this.rawDataRatingInfoList.add(newRawRatingContainer);
		
		/*
		 * 國道計算
		 */
		for(RawData roadData : additionalList) {

			String poiKind = roadData.columnValueMap.get("POI_KIND");
			if("國道".equals(poiKind) == false && 
				"特種營業".equals(poiKind) == false &&
				"工業區".equals(poiKind) == false &&
				"藥房".equals(poiKind) == false &&
				"傳統市場".equals(poiKind) == false
			) {
				continue;
			}
			
			double roadDataX = Double.valueOf(roadData.columnValueMap.get("X"));
			double roadDataY = Double.valueOf(roadData.columnValueMap.get("Y"));
			
			newCoordinateTransform = new CoordinateTransform();
			returnCoordinateTransform = newCoordinateTransform.TWD97_To_lonlat(roadDataX, roadDataY);
			roadDataX = returnCoordinateTransform.x;
			roadDataY = returnCoordinateTransform.y;
			double distance = DistanceComputer.getDistance(rawDataX, rawDataY, roadDataX, roadDataY);			
			
			if("國道".equals(poiKind)) {
				//國道分數  
				//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
				int score;
				if (distance < 500.0) {
					score = 3;
				} else if (distance < 1000.0) {
					score = 2;
				} else if (distance < 2000.0) {
					score = 1;
				} else {
					score = 0;
				}				
				
				//if(score > 0) {
				//	log.debug("國道 ==>" + score);
				//}
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("國道");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("國道", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
			}
			
			
			if("藥房".equals(poiKind)) {
				//國道分數  
				//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
				int score;
				if (distance <= 500.0) {
					score = 3;
				} else if (distance <= 1000.0) {
					score = 2;
				} else if (distance <= 1500.0) {
					score = 1;
				} else {
					score = 0;
				}					
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("藥房");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("藥房", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
			}			
			
			if("傳統市場".equals(poiKind)) {
				//國道分數  
				//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
				int score;
				if (distance <= 500.0) {
					score = 3;
				} else if (distance <= 1000.0) {
					score = 2;
				} else if (distance <= 1500.0) {
					score = 1;
				} else {
					score = 0;
				}					
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("傳統市場");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("傳統市場", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
			}			
			
			//特種營業
			//100公尺內(3)
			//100-200公尺(2)
			//200-500公尺(1)
			//(0)
			if("特種營業".equals(poiKind)) {

				//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
				//工業區
				//100公尺內(3)
				//100-200公尺(2)
				//200-500公尺(1)
				//(0)
				
				int score;
				if (distance < 100.0) {
					score = 3;
				} else if (distance < 200.0) {
					score = 2;
				} else if (distance < 500.0) {
					score = 1;
				} else {
					score = 0;
				}				
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("特種營業");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.positiveInfoMap.put("特種營業", saveRatingInfo);
				}
				
				if(score > 0) {
					saveRatingInfo.levelScore += score;
				}
				
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				
			}
			
			
			if("工業區".equals(poiKind)) {

				//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
				//工業區
				//100公尺內(3)
				//100-200公尺(2)
				//200-500公尺(1)
				//(0)
				
				int score;
				if (distance < 100.0) {
					score = 3;
				} else if (distance < 200.0) {
					score = 2;
				} else if (distance < 500.0) {
					score = 1;
				} else {
					score = 0;
				}				
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("工業區");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.positiveInfoMap.put("工業區", saveRatingInfo);
				}
				
				if(score > 0) {
					saveRatingInfo.levelScore += score;
				}
				
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				
			}
			
			
			
		}
		
		
		for(RawData poiData : poiList) {
			//log.debug("targetRowData.columnValueMap.keySet() ==>" + targetRowData.columnValueMap.keySet());
			String minorClassification = poiData.columnValueMap.get("地標小分類");
			
			if(minorClassification.indexOf("殯儀館") != -1) {
				minorClassification = "殯儀館";
			}
			if(minorClassification.indexOf("公墓") != -1) {
				minorClassification = "墓地";
			}
			
			if(minorClassification.indexOf("高壓電塔") != -1) {
				minorClassification = "高壓電塔";
			}
			
			if(	minorClassification.indexOf("瓦斯槽") != -1) {
				minorClassification = "瓦斯槽";
			}
			
			if(	minorClassification.indexOf("變電所") != -1	) {
				minorClassification = "變電所";
			}
			if(	minorClassification.indexOf("焚化爐") != -1	) {
				minorClassification = "焚化爐";
			}
			
			if(	minorClassification.indexOf("垃圾掩埋場") != -1 || 
				minorClassification.indexOf("垃圾焚化廠") != -1 ||
				minorClassification.indexOf("垃圾分類廠") != -1 ||
				minorClassification.indexOf("垃圾處理場") != -1
			) {
				minorClassification = "垃圾場";
			}
			
			//臺灣臺中女子監獄
			//臺灣臺中戒治所
			//臺灣臺中看守所
			//臺灣臺中監獄			
			if(	minorClassification.indexOf("監獄") != -1 || 
				minorClassification.indexOf("戒治所") != -1 ||
				minorClassification.indexOf("看守所") != -1
			) {
				minorClassification = "監獄";
			}
			
			
			double poiDataX = Double.valueOf(poiData.columnValueMap.get("X坐標"));
			double poiDataY = Double.valueOf(poiData.columnValueMap.get("Y坐標"));
			
			newCoordinateTransform = new CoordinateTransform();
			returnCoordinateTransform = newCoordinateTransform.TWD97_To_lonlat(poiDataX, poiDataY);
			//log.debug("after poiDataX ==>" + poiDataX + "->" + returnCoordinateTransform.x);
			//log.debug("after poiDataY ==>" + poiDataY + "->" + returnCoordinateTransform.y);
			poiDataX = returnCoordinateTransform.x;
			poiDataY = returnCoordinateTransform.y;
			
			
			double distance = DistanceComputer.getDistance(rawDataX, rawDataY, poiDataX, poiDataY);
			//log.debug("Distance ==>" + distance);
			
////國道分數  
//Double HWAY = MapUtils.getDouble(map, "HWAY_DISTANCE_SQL", 3000.0);
//int HWAY_Score;
//if (HWAY < 500.0) {
//    HWAY_Score = 3;
//} else if (HWAY < 1000.0) {
//    HWAY_Score = 2;
//} else if (HWAY < 2000.0) {
//    HWAY_Score = 1;
//} else {
//    HWAY_Score = 0;
//}
						
			if(	"汽車轉運站".equals(minorClassification) || 
				"火車站".equals(minorClassification)) {
				
				////鐵路分數  
				//Double RAIL = MapUtils.getDouble(map, "RAIL_DISTANCE_SQL", 3000.0);
				//int RAIL_Score;
				//if (RAIL < 500.0) {
				//    RAIL_Score = 3;
				//} else if (RAIL < 1000.0) {
				//    RAIL_Score = 2;
				//} else if (RAIL < 1500.0) {
				//    RAIL_Score = 1;
				//} else {
				//    RAIL_Score = 0;
				//}
				
				int score;
				if (distance < 500.0) {
				    score = 3;
				} else if (distance < 1000.0) {
				    score = 2;
				} else if (distance < 1500.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("鐵路");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("鐵路", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}			
			
			if(		"公園".equals(minorClassification)  
			//	||  "停車場".equals(minorClassification)  
			//  ||  "市場".equals(minorClassification)  
				||  "體育場".equals(minorClassification)  
				||  "廣場".equals(minorClassification) 
				||  "綠園道".equals(minorClassification)
			) {
				
				////公園分數  
				//Double PARK = MapUtils.getDouble(map, "PARK_DISTANCE_SQL", 3000.0);
				//int PARK_Score;
				//if (PARK < 50.0) {
				//    PARK_Score = 3;
				//} else if (PARK < 200.0) {
				//    PARK_Score = 2;
				//} else if (PARK < 500.0) {
				//    PARK_Score = 1;
				//} else {
				//    PARK_Score = 0;
				//}
				
				int score;
				if (distance < 50.0) {
				    score = 3;
				} else if (distance < 200.0) {
				    score = 2;
				} else if (distance < 500.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("公園");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("公園", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}				
			
			
			if(	//	"托兒所".equals(minorClassification)  
				//|| "幼稚園".equals(minorClassification)  
				"小學".equals(minorClassification)  
				|| "國中".equals(minorClassification)  
				|| "高中".equals(minorClassification)  
				|| "大專院校".equals(minorClassification)  
				//|| "博物館".equals(minorClassification)  
				//|| "圖書館".equals(minorClassification)  
				//|| "美術館".equals(minorClassification)  
				//|| "文化中心".equals(minorClassification) 
				//|| "古蹟文物".equals(minorClassification)
			) {
				
				////小學國高中分數  
				//Double SCHOOL = MapUtils.getDouble(map, "SCHOOL_DISTANCE_SQL", 3000.0);
				//int SCHOOL_Score;
				//if (SCHOOL < 100.0) {
				//    SCHOOL_Score = 3;
				//} else if (SCHOOL < 500.0) {
				//    SCHOOL_Score = 2;
				//} else if (SCHOOL < 1000.0) {
				//    SCHOOL_Score = 1;
				//} else {
				//    SCHOOL_Score = 0;
				//}
				
				int score;
				if (distance < 100.0) {
				    score = 3;
				} else if (distance < 500.0) {
				    score = 2;
				} else if (distance < 1000.0) {
				    score = 1;
				} else {
				    score = 0;
				}				
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("小學國高中");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("小學國高中", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}				
			
			if(	//"連鎖物流中心".equals(minorClassification) || 
					"大型休閒遊樂場所".equals(minorClassification)
				|| 	"大型連鎖購物中心".equals(minorClassification)) {
				
				////百貨購物中心分數  
				//Double STORE = MapUtils.getDouble(map, "STORE_DISTANCE_SQL", 3000.0);
				//int STORE_Score;
				//if (STORE < 100.0) {
				//    STORE_Score = 4;
				//} else if (STORE < 200.0) {
				//    STORE_Score = 3;
				//} else if (STORE < 500.0) {
				//    STORE_Score = 2;
				//} else if (STORE < 1000.0) {
				//    STORE_Score = 1;
				//} else {
				//    STORE_Score = 0;
				//}
					
				int score;
				if (distance < 100.0) {
				    score = 4;
				} else if (distance < 200.0) {
				    score = 3;
				} else if (distance < 500.0) {
				    score = 2;
				} else if (distance < 1000.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("百貨購物");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("百貨購物", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}				
			
			////20171217新增參數        
			if(	"護理機構".equals(minorClassification) ||
				"大型醫院".equals(minorClassification)	
			) {
				
				////		醫院分數HOSPITAL_Score
				//Double HOSPITAL_DISTANCE_SQL = MapUtils.getDouble(map, "HOSPITAL_DISTANCE_SQL", 3000.0);
				//int HOSPITAL_Score;
				//if (HOSPITAL_DISTANCE_SQL <= 250.0) {
				//    HOSPITAL_Score = 4;
				//} else if (HOSPITAL_DISTANCE_SQL <= 500.0) {
				//    HOSPITAL_Score = 3;
				//} else if (HOSPITAL_DISTANCE_SQL <= 1000.0) {
				//    HOSPITAL_Score = 2;
				//} else if (HOSPITAL_DISTANCE_SQL <= 2000.0) {
				//    HOSPITAL_Score = 1;
				//} else {
				//    HOSPITAL_Score = 0;
				//}
						
				int score;
				if (distance <= 250.0) {
				    score = 4;
				} else if (distance <= 500.0) {
				    score = 3;
				} else if (distance <= 1000.0) {
				    score = 2;
				} else if (distance <= 2000.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("護理機構");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("護理機構", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}				
			
			
			
			
			if(	"電影院".equals(minorClassification) ) {
				
				////		電影院分數CINEMA_Score
				//Double CINEMA_DISTANCE = MapUtils.getDouble(map, "CINEMA_DISTANCE", 9000.0);
				//int CINEMA_Score;
				//if (CINEMA_DISTANCE <= 3000.0) {
				//    CINEMA_Score = 2;
				//} else if (CINEMA_DISTANCE <= 5000.0) {
				//    CINEMA_Score = 1;
				//} else {
				//    CINEMA_Score = 0;
				//}
				
				int score;
				if (distance <= 3000.0) {
				    score = 2;
				} else if (distance <= 5000.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("電影院");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("電影院", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}				
			
			if(	"7 11".equals(minorClassification) ||
				"OK".equals(minorClassification) ||
				"全家".equals(minorClassification) ||
				"萊爾富".equals(minorClassification) ||
				"福客多".equals(minorClassification) 
			) {
				
				////		便利商店分數CNVCSTORE_Score
				//Double CNVCSTORE_DISTANCE = MapUtils.getDouble(map, "CNVCSTORE_DISTANCE", 3000.0);
				//int CNVCSTORE_Score;
				//if (CNVCSTORE_DISTANCE <= 100.0) {
				//    CNVCSTORE_Score = 3;
				//} else if (CNVCSTORE_DISTANCE <= 200.0) {
				//    CNVCSTORE_Score = 2;
				//} else if (CNVCSTORE_DISTANCE <= 500.0) {
				//    CNVCSTORE_Score = 1;
				//} else {
				//    CNVCSTORE_Score = 0;
				//}
				
				int score;
				if (distance <= 100.0) {
				    score = 3;
				} else if (distance <= 200.0) {
				    score = 2;
				} else if (distance <= 500.0) {
				    score = 1;
				} else {
				    score = 0;
				}				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("便利商店");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("便利商店", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}			
			
			if(	"大型連鎖速食店".equals(minorClassification) 
				|| "大型餐飲樓".equals(minorClassification) ) {
				
				////		速食店分數FASTFOOD_Score
				//Double FASTFOOD_DISTANCE = MapUtils.getDouble(map, "FASTFOOD_DISTANCE", 3000.0);
				//int FASTFOOD_Score;
				//if (FASTFOOD_DISTANCE <= 500.0) {
				//    FASTFOOD_Score = 3;
				//} else if (FASTFOOD_DISTANCE <= 1000.0) {
				//    FASTFOOD_Score = 2;
				//} else if (FASTFOOD_DISTANCE <= 1500.0) {
				//    FASTFOOD_Score = 1;
				//} else {
				//    FASTFOOD_Score = 0;
				//}
				int score;
				if (distance <= 500.0) {
				    score = 3;
				} else if (distance <= 1000.0) {
				    score = 2;
				} else if (distance <= 1500.0) {
				    score = 1;
				} else {
				    score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.positiveInfoMap.get("大型連鎖速食店");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.positiveInfoMap.put("大型連鎖速食店", saveRatingInfo);
				}
				
				if(saveRatingInfo.levelScore < score) {
					saveRatingInfo.levelScore = score;
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}			
			
////		傳統市場分數MARKET_Score
//Double MARKET_DISTANCE = MapUtils.getDouble(map, "MARKET_DISTANCE", 3000.0);
//int MARKET_Score;
//if (MARKET_DISTANCE <= 500.0) {
//    MARKET_Score = 3;
//} else if (MARKET_DISTANCE <= 1000.0) {
//    MARKET_Score = 2;
//} else if (MARKET_DISTANCE <= 1500.0) {
//    MARKET_Score = 1;
//} else {
//    MARKET_Score = 0;
//}			
			
			//---------------------------------------------//
			//-------
			
			if(	"寺廟".equals(minorClassification) || "神壇".equals(minorClassification)) {
				
				
				//寺廟、神壇
				//20公尺內(3)
				//20-50公尺(2)
				//50-100公尺(1)
				//(0)
				int score;
				if (distance <= 20.0) {
					score = 3;
				} else if (distance <= 50.0) {
					score = 2;
				} else if (distance <= 100.0) {
					score = 1;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("寺廟、神壇");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.negativeInfoMap.put("寺廟、神壇", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				saveRatingInfo.trueDistance += distance;
				continue;
			}						
			
			if(	"變電所".equals(minorClassification)  || 
				"焚化爐".equals(minorClassification)	
			) {
				
				//變電所、焚化爐
				//100公尺內(3)
				//100-200公尺(2)
				//200-500公尺(1)
				//(0)
				int score;
				if (distance <= 100.0) {
					score = 3;
				} else if (distance <= 200.0) {
					score = 2;
				} else if (distance <= 500.0) {
					score = 1;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("變電所、焚化爐");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					newRawRatingContainer.negativeInfoMap.put("變電所、焚化爐", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				saveRatingInfo.trueDistance += distance;
				continue;
			}						
			
			
			if(	"加油站".equals(minorClassification) || 
				"瓦斯槽".equals(minorClassification)) {
				
				//20公尺內(4)
				//20-50公尺(3)
				//50-100公尺(2)
				//100-200公尺(1)
				//(0)
				int score;
				if (distance <= 20.0) {
					score = 4;
				} else if (distance <= 50.0) {
					score = 3;
				} else if (distance <= 100.0) {
					score = 2;
				} else if (distance <= 200.0) {
					score = 1;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("加油站、瓦斯槽");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.negativeInfoMap.put("加油站、瓦斯槽", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				
				continue;
			}						
			
			
			if(	"殯儀館".equals(minorClassification) || 
				"墓地".equals(minorClassification)) {
				
				//殯儀館、墓地
				//100公尺內(3)
				//100-200公尺(2)
				//200-500公尺(1)
				//(0)
				int score;
				if (distance <= 100.0) {
					score = 3;
				} else if (distance <= 200.0) {
					score = 2;
				} else if (distance <= 500.0) {
					score = 1;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("殯儀館、墓地");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.negativeInfoMap.put("殯儀館、墓地", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				continue;
			}									
			
			
			if(	"高壓電塔".equals(minorClassification) ) {
				
				//高壓電塔
				//20公尺內(4)
				//20-50公尺(3)
				//50-100公尺(2)
				//100-200公尺(1)
				//(0)

				int score;
				if (distance <= 20.0) {
					score = 4;
				} else if (distance <= 50.0) {
					score = 3;
				} else if (distance <= 100.0) {
					score = 2;
				} else if (distance <= 200.0) {
					score = 1;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("高壓電塔");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.negativeInfoMap.put("高壓電塔", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				continue;
			}				
			
			
			if(	"垃圾場".equals(minorClassification) ) {
				
				//20公尺內(3)
				//20-50公尺(2)
				//50-100公尺(1)
				//(0)
				int score;
				if (distance <= 100.0) {
					score = 4;
				} else if (distance <= 200.0) {
					score = 3;
				} else if (distance <= 500.0) {
					score = 2;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("垃圾場");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.negativeInfoMap.put("垃圾場", saveRatingInfo);
				}
				
				//if(saveRatingInfo.levelScore < TEMPLE_Score) {
				//	saveRatingInfo.levelScore = TEMPLE_Score;
				//	saveRatingInfo.trueDistance = TEMPLE_DISTANCE;
				//}
				
				saveRatingInfo.levelScore += score;
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				continue;
			}						
			
			
			if(	"監獄".equals(minorClassification) ) {
				
				//20公尺內(3)
				//20-50公尺(2)
				//50-100公尺(1)
				//(0)
				int score;
				if (distance <= 100.0) {
					score = 4;
				} else if (distance <= 200.0) {
					score = 3;
				} else if (distance <= 500.0) {
					score = 2;
				} else {
					score = 0;
				}
				
				RatingInfo saveRatingInfo = newRawRatingContainer.negativeInfoMap.get("監獄");
				if(saveRatingInfo == null) {
					saveRatingInfo = new RatingInfo();
					saveRatingInfo.trueDistance = 999999;
					newRawRatingContainer.negativeInfoMap.put("監獄", saveRatingInfo);
				}
				
				saveRatingInfo.levelScore += score;
				if(saveRatingInfo.trueDistance < distance) {
					saveRatingInfo.trueDistance = distance;
				}
				continue;
			}						
			
//			log.debug("newRawRatingContainer.positiveInfoMap ==>" + newRawRatingContainer.positiveInfoMap);
//			log.debug("newRawRatingContainer.negativeInfoMap ==>" + newRawRatingContainer.negativeInfoMap);
			
		}
		
	}

}
