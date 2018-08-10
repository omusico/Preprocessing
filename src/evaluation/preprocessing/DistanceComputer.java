package evaluation.preprocessing;

import org.apache.log4j.Logger;

public class DistanceComputer {
	private static Logger log = Logger.getLogger(DistanceComputer.class);
	
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
//		double EARTH_RADIUS = 6378137;
//		double radLat1 = rad(lat1);
//		double radLat2 = rad(lat2);
//		double a = radLat1 - radLat2;
//		double b = rad(lng1) - rad(lng2);
//		double s = 2 * Math.asin(Math.sqrt(
//				Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
//		s = s * EARTH_RADIUS;
//		s = Math.round(s * 10000) / 10000;
//		return s;
		
//		log.debug("lat1 ==>" + lat1);		
//		log.debug("lng1 ==>" + lng1);		
//		log.debug("lat2 ==>" + lat2);		
//		log.debug("lng2 ==>" + lng2);		
		
	    double earthRadius = 6371000; //meters
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist =  (earthRadius * c);
	    return dist;		
		
	}		
}
