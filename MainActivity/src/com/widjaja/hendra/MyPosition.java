package com.widjaja.hendra;


/* 
 *  This is a class from MainActivity - Augmented Reality
 *  This class has been modified and adjusted to my project
 *  All rights are reserved. Copyright(c) 2013 Hendra Widjaja
 */
public class MyPosition {
    // Main TAG 
    private static final String APPTAG = "MyPosition";
    private String copyright = APPTAG + "; Copyright(c) 2013, Hendra Widjaja.  eMail: hendrawidjaja@live.de";
	
    private int id;
    private String latitude;
    private String longitude;
    private String locationnumber;
    
    public MyPosition(String latitude, String longitude, String locationnumber) {
	super();
	this.latitude = latitude;
	this.longitude = longitude;
	this.locationnumber = locationnumber;
    }	
    public int getId() {
	return id;
    }
    public void setId(int id) {
	this.id = id;
    }
    public String getLatitude() {
	return latitude;
    }
    public void setLatitude(String latitude) {
	this.latitude = latitude;
    }
    public String getLongitude() {
	return longitude;
    }
    public void setLongitude(String longitude) {
	this.longitude = longitude;
    }
    public String getLocationnumber() {
	return locationnumber;
    }
    public void setLocationnumber(String locationnumber) {
	this.locationnumber = locationnumber;
    }
    public String showCopyright() {
	return copyright;
    }
    @Override
    public String toString() {
	return "Location [id=" + id + 
	       ", Lat: " + latitude + 
	       ", long: " + longitude + 
	       ", Number: " + locationnumber + "]" +
	       "\n" + showCopyright();
    }
}