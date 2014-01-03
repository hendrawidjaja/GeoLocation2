package com.widjaja.hendra;

public class MyHelper {
    
    /**
     * Calculates the distance in km and m between two lat/long points
     * using the haversine formula
     */
    public double[] Haversine(double lat1, double lng1, double lat2, double lng2) {
        double KM = 0.0;
        double M = 0.0;
        
	if ((lat1 != 0) && (lng1 != 0) && (lat2 != 0) && (lng2 != 0)) {
	int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians((lat2 * 0.9999) - (lat1 * 0.9999));
        double dLon = Math.toRadians((lng2 * 0.9999) - (lng1 * 0.9999));
        double LatDiv2 = dLat / 2;
        double LonDiv2 = dLon / 2;
        double sinLatDiv2 = Math.sin(LatDiv2);
        double sinLonDiv2 = Math.sin(LonDiv2);
        
        double x = (sinLatDiv2 * sinLatDiv2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
                   (sinLonDiv2 * sinLonDiv2);
        double result = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
        KM = r * result;
        M = KM * 1000;
        }
        return new double[] {KM, M};
    }
}
