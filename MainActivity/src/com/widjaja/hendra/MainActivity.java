/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.widjaja.hendra;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.model.people.Person.Collection;

/**
 * This the app's main Activity. It provides buttons for requesting the various features of the
 * app, displays the current location, the current address, and the status of the location client
 * and updating services.
 *
 * {@link #getLocation} gets the current location using the Location Services getLastLocation()
 * function. {@link #getAddress} calls geocoding to get a street address for the current location.
 * {@link #startUpdates} sends a request to Location Services to send periodic location updates to
 * the Activity.
 * {@link #stopUpdates} cancels previous periodic update requests.
 *
 * The update interval is hard-coded to be 5 seconds.
 */
@SuppressWarnings("unused")
/* 
 *  This is My Main class for Augmented reality
 *  This class has been modified and adjusted to my project
 *  Some code has been adjusted and modified due to my Project
 */

public class MainActivity extends Activity implements			
    LocationListener, 
    OnSeekBarChangeListener,
    GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener {

    // Main TAG 
    private static final String APPTAG = "MainActivity";
	 
    // A request to connect to Location Services
    private LocationRequest locReq;

    // Stores the current instantiation of the location client in this object
    private LocationClient locClient;

    // Handles to UI widgets
    private static TextView latLng;
    private static ImageView imageView;
    
    // My Library
    private Location location;
    private SeekBar radSeek;
    private TextView radText;
    private LatLng listlatlng;
    private Paint paint;
    private Canvas canvas;
    private Path path;
    private Context context = this; 
    private Bitmap workingBitmap;
    private Bitmap mutableBitmap;
    private Bitmap bitmap;
    private BitmapFactory.Options myOptions;
    
    private MySQLiteHelper MyDB;
    private SQLiteDatabase SQLDB;
    private MyPosition myposition;
    private List<MyPosition> listOfPosition;
    private int circleRadius;
    private int radius;
    private boolean radiusTriggered;
    private float[] distance = new float[2];
    private static final int RAD_SEEKBAR_MIN = 00;
    private static final int RAD_SEEKBAR_MAX = 20;
    private double latitude, longitude;
    int counter = 0;
    
    // Handle to SharedPreferences for this app
    private SharedPreferences sharePref;

    // Handle to a SharedPreferences editor
    private SharedPreferences.Editor editor;
    
    /*
     * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver.
     *
     */
    boolean updateReq = false;
    
    /*
     * Initialize the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);          
        
        // Initializing
        initialize();
     	initializeDB();
     	initializeLocReq();
     	initializeRadSeeker();
    }
    
    private void initialize() {
	// id TAG 
	final String idTAG = "Initialize";
		
	// Get handles to the UI view objects
        latLng = (TextView) findViewById(R.id.lat_lng);
        radSeek = (SeekBar) findViewById(R.id.radSeek);
        imageView = (ImageView)findViewById(R.id.imageView);
        radText = (TextView) findViewById(R.id.radText);
        
        myOptions = new BitmapFactory.Options();
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important       
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map, myOptions);
        
        paint = new Paint();
        workingBitmap = Bitmap.createBitmap(bitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true); 
       
        if ((latitude >= 0.001) || (longitude >= 0.001)) {
     	    initializeRadSeeker();
     	} else {
     	    radSeek.setEnabled(false);
     	}
    }
    
    /*
     * Database must be initialized before it can be used. This will ensure
     * that the database exists and is the current version.
     */
    private void initializeDB() {
	// id TAG 
	final String idTAG = "initializeDB";
	MyDB = new MySQLiteHelper(this);       
	SQLDB = null;
        MyDB.prepareDB();

        try {
        // A reference to the database can be obtained after initialization.
        SQLDB = MyDB.getWritableDatabase();
        } catch (Exception e) {
            Log.d(idTAG, "" + e);
        } finally {
            try {
        	// If database get copied over to client side, then try to close it
        	MyDB.close();
            } catch (Exception e) {
                Log.d(idTAG, "MyDB close" + e);
            } finally {
        	// Finally close all
                SQLDB.close();
            }
        }        
        // Database is now set and ready
        MyDB = new MySQLiteHelper(this);
        // Put all the coordinates to a List
        listOfPosition = MyDB.getAllPositions();  
   }
	
    /*
     * radBar Initilization
     */
    protected void initializeRadSeeker() {
	// id TAG 
	final String idTAG = "initializeRadSeeker";
	
	// Set a ChangeListener
        radSeek.setOnSeekBarChangeListener(this);       
        radText.setText("R: " + radius);
        // Set an adjustment to it
	radiusTriggered = false;
	radSeek.setProgress(RAD_SEEKBAR_MIN);
	radSeek.setMax(RAD_SEEKBAR_MAX);
    }
    
    protected void initializeLocReq() {
	// id TAG 
	final String idTAG = "initializeLocReq";
        
	// Create a new global location parameters object
        locReq = LocationRequest.create();
        
        /*
         * Set the update interval
         */
        locReq.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        locReq.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        updateReq = false;
        
        // Open Shared Preferences
        sharePref = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        editor = sharePref.edit();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        locClient = new LocationClient(this, this, this);
    }
    
    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {
        // If the client is connected
        if (locClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        locClient.disconnect();
        super.onStop();
    }
    
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {
        // Save the current setting for updates
        editor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, updateReq);
        editor.commit();
        super.onPause();
    }
    
    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
	// id TAG 
    	final String idTAG = "onStart";
        super.onStart();
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        locClient.connect();
        if (locClient != null && locClient.isConnected()) {
            location = locClient.getLastLocation();	
            latitude = location.getLatitude();
            longitude = location.getLongitude();	
        }
    }
    
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        // If the app already has a setting for getting location updates, get it
        if (sharePref.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            updateReq = sharePref.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            editor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            editor.commit();
        }
    }
    
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns herevityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {
            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
            switch (resultCode) {
       
            	// If Google Play services resolved the problem
            	case Activity.RESULT_OK: break;

                // If any other result was returned by Google Play services
                default: 
                {
                    Toast.makeText(this, "Google Play Service Error " + resultCode, Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                }
                    break;
            }

            // If any other request code was received
            default:
        	// Report that this Activity received an unknown requestCode
        	Log.d(LocationUtils.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
        	break;
        }
    }

    /*
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) { }
            return false;
        }
    }

    /*
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getLocation(View v) {
	// id TAG 
    	final String idTAG = "getLocation";
        // If Google Play Services is available, get the current location
    	
        if (servicesConnected()) {
            // Get latitude and longitude 
            location = locClient.getLastLocation();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            // Display the current location in the UI
            latLng.setText(LocationUtils.getLatLng(this, location));
            whereAmI();
        }
        radSeek.setEnabled(true);
        initializeRadSeeker();
        startUpdates(latLng);
    }
    
    /**
     * Invoked by the "Start Updates" button
     * Sends a request to start location updates
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void startUpdates(View v) {
        updateReq = true;
        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * Invoked by the "Stop Updates" button
     * Sends a request to remove location updates
     * request them.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void stopUpdates(View v) {
        updateReq = false;
        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }
    
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
	// id TAG 
    	final String idTAG = "onConnected";
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() { 
	// id TAG 
    	final String idTAG = "onDisconnected";
    	locClient.disconnect();
    }
    
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	// id TAG 
    	final String idTAG = "onConnectionFailed";
    	/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
        	// Start an Activity that tries to resolve the error
        	connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else { 
            // If no resolution is available, display a dialog to the user with the error.
            Log.d(idTAG, "" + connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
	// id TAG 
    	final String idTAG = "onLocationChanged";
        
    	// if Geo Location pressed
    	if (updateReq) {
    	    // fetch the value from location Client
    	    // Pack the value into latitude and longitude
    	    // then display it
    	    location = locClient.getLastLocation();
    	    latitude = location.getLatitude();
    	    longitude = location.getLongitude();
    	    
    	    // In the UI, set the latitude and longitude to the value received
    	    latLng.setText(LocationUtils.getLatLng(this, location));
        
    	    // Updates my current position on the picture
    	    // according to the nearest position compareTo the database
    	    // It will deliver the nearest but not the exact
    	    whereAmI(); 
    	}
    }
    
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        locClient.requestLocationUpdates(locReq, this);
    }
    
    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        locClient.removeLocationUpdates(this);
    }
    
    /*
     * drawingDots Initialization
     * This will display the next closest point
     * The distance is adjusted within radius of meters
     */
    public void drawDots(int locationNumber) {
	// id TAG 
	final String idTAG = "drawDots";

	// Paint component initilization
	paint.setAntiAlias(true);
	// Set the paint color to Blue
	paint.setColor(Color.BLUE);	
	// Set ready the imageView
	imageView.setAdjustViewBounds(true);
	imageView.setImageBitmap(mutableBitmap);
	// The radius of the circle on those points
	circleRadius = 7;
	
	// If incase locationNumber equals same to the database
	// from 1 to 18, then draw a Circle
	switch (locationNumber) {
 	case 1: canvas.drawCircle(87, 41, circleRadius, paint); break;
 	case 2: canvas.drawCircle(141, 88, circleRadius, paint); break;	
 	case 3: canvas.drawCircle(230, 92, circleRadius, paint); break;
 	case 4: canvas.drawCircle(395, 112, circleRadius, paint); break;
 	case 5: canvas.drawCircle(83, 233, circleRadius, paint); break;
 	case 6: canvas.drawCircle(175, 179, circleRadius, paint); break;
 	case 7: canvas.drawCircle(228, 254, circleRadius, paint); break;
 	case 8: canvas.drawCircle(176, 350, circleRadius, paint); break;
 	case 9: canvas.drawCircle(228, 349, circleRadius, paint); break;
 	case 10: canvas.drawCircle(312, 349, circleRadius, paint); break;
 	case 11: canvas.drawCircle(402, 350, circleRadius, paint); break;
 	case 12: canvas.drawCircle(77, 471, circleRadius, paint); break;
 	case 13: canvas.drawCircle(228, 467, circleRadius, paint); break;
 	case 14: canvas.drawCircle(315, 467, circleRadius, paint); break;
 	case 15: canvas.drawCircle(400, 467, circleRadius, paint); break;
 	case 16: canvas.drawCircle(74, 534, circleRadius, paint); break;
 	case 17: canvas.drawCircle(158, 549, circleRadius, paint); break;
 	case 18: canvas.drawCircle(256, 565, circleRadius, paint); break;
 	default: break;
 	}
    }
    
    /*
     * drawingMe Initialization
     * This will display the actual closest but not the exact 
     */
    public void whereAmI() {
	// id TAG 
	final String idTAG = "whereAmI";

	// Paint component initilization
	paint.setAntiAlias(true);
	// Set the paint color to Green
	paint.setColor(Color.GREEN);	
	mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
	// Set a new canvas, it will get throw if not use anymore
	canvas = new Canvas (mutableBitmap);
	// Set ready the imageView
	imageView.setAdjustViewBounds(true);
	imageView.setImageBitmap(mutableBitmap);
	
	// The radius of the circle on those points
	circleRadius = 5;
	// Initialize the locationNumber and set it to Zero
	int locationNumber = 0;
	// Set active the Radius Seeker as user has pressed Get Location button
	initializeRadSeeker();

	// Set up a new HashMap as Temporary Map
        HashMap <Double, Double> hashmap = new HashMap <Double, Double>();
        // Set another Map but to store the real value
        Map <Double, Double> map = new HashMap <Double, Double>();
         
        // For all position in the List
        for (MyPosition myposition : listOfPosition) {
            // Set a new variable to hold latitude and longitude
	    listlatlng = new LatLng(Double.parseDouble(myposition.getLatitude()), Double.parseDouble(myposition.getLongitude()));
	    // CompareTo the List 
	    Location.distanceBetween(latitude, longitude, listlatlng.latitude, listlatlng.longitude, distance);
	    // If incase the distance is below 25
	    if (distance[0] < 25) {    
		// Place it in the hashmap
	    	hashmap.put(Double.parseDouble(myposition.getLocationnumber()), (double) distance[0]);
	    } else { 
		// If it is above 25
		// Ignore it
	    }
	}

        // Now, incase we have or not the position
        try {
            // It will try to sort it based on those values
            map = ValueComparator.sortOnValues(hashmap);
            // Get the first Key from sorted-Map
            locationNumber = map.entrySet().iterator().next().getKey().intValue();
        } catch(Exception e) {
            // HashMap is empty because none is below 25
        }
        
        // Now as we got the locationNumber
        // We could draw it out to the picture
	switch (locationNumber) {
	     // Our first 18 positions
	     case 1: canvas.drawCircle(87, 41, circleRadius, paint); break;
	     case 2: canvas.drawCircle(141, 88, circleRadius, paint); break;	
	     case 3: canvas.drawCircle(230, 92, circleRadius, paint); break;
	     case 4: canvas.drawCircle(395, 112, circleRadius, paint); break;
	     case 5: canvas.drawCircle(83, 233, circleRadius, paint); break;
	     case 6: canvas.drawCircle(175, 179, circleRadius, paint); break;	
	     case 7: canvas.drawCircle(228, 254, circleRadius, paint); break;
	     case 8: canvas.drawCircle(176, 350, circleRadius, paint); break;
	     case 9: canvas.drawCircle(228, 349, circleRadius, paint); break;
	     case 10: canvas.drawCircle(312, 349, circleRadius, paint); break;
	     case 11: canvas.drawCircle(402, 350, circleRadius, paint); break;
	     case 12: canvas.drawCircle(77, 471, circleRadius, paint); break;
	     case 13: canvas.drawCircle(228, 467, circleRadius, paint); break;
	     case 14: canvas.drawCircle(315, 467, circleRadius, paint); break;
	     case 15: canvas.drawCircle(400, 467, circleRadius, paint); break;
	     case 16: canvas.drawCircle(74, 534, circleRadius, paint); break;
	     case 17: canvas.drawCircle(158, 549, circleRadius, paint); break;
	     case 18: canvas.drawCircle(256, 565, circleRadius, paint); break;
	     
	     // Point 1 to point 2
	     case 19: canvas.drawCircle(93.75f, 46.875f, circleRadius, paint);  break;
	     case 20: canvas.drawCircle(100.5f, 52.75f, circleRadius, paint); break;
	     case 21: canvas.drawCircle(114.0f, 64.5f, circleRadius, paint); break; 
	     case 22: canvas.drawCircle(127.5f, 76.25f, circleRadius, paint);  break;
	     case 23: canvas.drawCircle(134.25f, 82.125f, circleRadius, paint);  break;
	     	
	     // Point 2 to point 3
	     case 24: canvas.drawCircle(152.5f, 88.5f, circleRadius, paint); break;
	     case 25: canvas.drawCircle(163.25f, 89, circleRadius, paint); break;
	     case 26: canvas.drawCircle(185.5f, 90, circleRadius, paint); break;
	     case 27: canvas.drawCircle(207.75f, 91, circleRadius, paint); break;
	     case 28: canvas.drawCircle(218.875f, 91.5f, circleRadius, paint); break;  	
	     	
	     // point 3 to 4
	     case 29: canvas.drawCircle(240.3125f, 93.25f, circleRadius, paint); break;
	     case 30: canvas.drawCircle(250.625f, 94.5f, circleRadius, paint); break;
	     case 31: canvas.drawCircle(271.25f, 97f, circleRadius, paint); break;
	     case 32: canvas.drawCircle(312.5f, 102f, circleRadius, paint); break;
	     case 33: canvas.drawCircle(353.75f, 107f, circleRadius, paint); break;
	     case 34: canvas.drawCircle(374.375f, 109.5f, circleRadius, paint); break;
	     case 35: canvas.drawCircle(384.675f, 110.75f, circleRadius, paint); break;
	     	
	     // point 1 to 5
	     case 36: canvas.drawCircle(86.75f, 52.875f, circleRadius, paint); break;
	     case 37: canvas.drawCircle(86.5f, 64.75f, circleRadius, paint); break;
	     case 38: canvas.drawCircle(86, 64.75f, circleRadius, paint); break;
	     case 39: canvas.drawCircle(85, 88.5f, circleRadius, paint); break;
	     case 40: canvas.drawCircle(84, 185, circleRadius, paint); break;
	     case 41: canvas.drawCircle(83.5f, 209, circleRadius, paint); break;
	     case 42: canvas.drawCircle(83.25f, 221f, circleRadius, paint); break;
	     	
	     // point 5 to 6
	     case 43: canvas.drawCircle(94.5f, 233, circleRadius, paint); break;
	     case 44: canvas.drawCircle(106, 233, circleRadius, paint); break;
	     case 45: canvas.drawCircle(129, 233, circleRadius, paint); break;
	     case 46: canvas.drawCircle(175, 233, circleRadius, paint); break;
	     case 47: canvas.drawCircle(175, 206, circleRadius, paint); break;
	     case 48: canvas.drawCircle(175, 192.5f, circleRadius, paint); break;
	     	
	     // point 6 to 7
	     case 49: canvas.drawCircle(181.5f, 188.375f, circleRadius, paint); break;
	     case 50: canvas.drawCircle(188.25f, 197.25f, circleRadius, paint); break;
	     case 51: canvas.drawCircle(201.5f, 216.5f, circleRadius, paint); break;
	     case 52: canvas.drawCircle(214.75f, 235.25f, circleRadius, paint); break;
	     case 53: canvas.drawCircle(221.375f, 244.625f, circleRadius, paint); break;
	     	
	     // point 6 to 8
	     case 54: canvas.drawCircle(175.125f, 189.6875f, circleRadius, paint); break;
	     case 55: canvas.drawCircle(175.175f, 200.375f, circleRadius, paint); break;
	     case 56: canvas.drawCircle(175.25f, 221.75f, circleRadius, paint); break;
	     case 57: canvas.drawCircle(175.5f, 264.5f, circleRadius, paint); break;
	     case 58: canvas.drawCircle(175.75f, 307.25f, circleRadius, paint); break;
	     case 59: canvas.drawCircle(175.825f, 328.625f, circleRadius, paint); break;
	     case 60: canvas.drawCircle(175.925f, 339.3125f, circleRadius, paint); break;
	     	
	     // point 2 to 6
	     case 61: canvas.drawCircle(229.125f, 102.125f, circleRadius, paint); break;
	     case 62: canvas.drawCircle(229.25f, 112.25f, circleRadius, paint); break;
	     case 63: canvas.drawCircle(229.5f, 132.5f, circleRadius, paint); break;
	     case 64: canvas.drawCircle(229f, 173f, circleRadius, paint); break;
	     case 65: canvas.drawCircle(229.5f, 213.5f, circleRadius, paint); break;
	     case 66: canvas.drawCircle(228.425f, 233.75f, circleRadius, paint); break;
	     case 67: canvas.drawCircle(228.25f, 243.875f, circleRadius, paint); break;
	     
	     // point 3 to 9
	     case 68: canvas.drawCircle(393.703125f, 115.71875f, circleRadius, paint); break;
	     case 69: canvas.drawCircle(392.40625f, 119.4375f, circleRadius, paint); break;
	     case 70: canvas.drawCircle(389.8125f, 126.875f, circleRadius, paint); break;
	     case 71: canvas.drawCircle(384.625f, 141.75f, circleRadius, paint); break;
	     case 72: canvas.drawCircle(374.25f, 171.5f, circleRadius, paint); break;
	     case 73: canvas.drawCircle(353.5f, 231f, circleRadius, paint); break;
	     case 74: canvas.drawCircle(332.75f, 290.5f, circleRadius, paint); break;
	     case 75: canvas.drawCircle(322.375f, 320.25f, circleRadius, paint); break;
	     case 76: canvas.drawCircle(317.1875f, 335.125f, circleRadius, paint); break;
	     case 77: canvas.drawCircle(314.59375f, 342.5625f, circleRadius, paint); break;
	     case 78: canvas.drawCircle(313.296875f, 346.28125f, circleRadius, paint); break;
	     
	     // point 3 to 10
	     case 79: canvas.drawCircle(395.21875f, 119.4375f, circleRadius, paint); break;
	     case 80: canvas.drawCircle(395.4375f, 126.875f, circleRadius, paint); break;
	     case 81: canvas.drawCircle(395.875f, 141.75f, circleRadius, paint); break;
	     case 82: canvas.drawCircle(396.75f, 171.5f, circleRadius, paint); break;
	     case 83: canvas.drawCircle(398.5f, 231f, circleRadius, paint); break;
	     case 84: canvas.drawCircle(400.25f, 290.5f, circleRadius, paint); break;
	     case 85: canvas.drawCircle(401.125f, 320.25f, circleRadius, paint); break;
	     case 86: canvas.drawCircle(401.5625f, 335.125f, circleRadius, paint); break;
	     case 87: canvas.drawCircle(401.78125f, 342.5625f, circleRadius, paint); break;
	     		
	     // point 6 to 8
	     case 88: canvas.drawCircle(228f, 237.25f, circleRadius, paint); break;
	     case 89: canvas.drawCircle(228f, 246.5f, circleRadius, paint); break;
	     case 90: canvas.drawCircle(228f, 265f, circleRadius, paint); break;
	     case 91: canvas.drawCircle(228f, 302f, circleRadius, paint); break;
	     case 92: canvas.drawCircle(228f, 326f, circleRadius, paint); break;
	     case 93: canvas.drawCircle(228f, 338f, circleRadius, paint); break;
	     case 94: canvas.drawCircle(228f, 344f, circleRadius, paint); break;
	     
	     // point 7 to 8
	     case 95: canvas.drawCircle(189f, 350f, circleRadius, paint); break;
	     case 96: canvas.drawCircle(202f, 350f, circleRadius, paint); break;
	     case 97: canvas.drawCircle(430f, 350f, circleRadius, paint); break;
		
	     // point 8 to 9
	     case 98: canvas.drawCircle(238.5f, 350f, circleRadius, paint); break;
	     case 99: canvas.drawCircle(249f, 350f, circleRadius, paint); break;
	     case 100: canvas.drawCircle(270f, 350f, circleRadius, paint); break;
	     case 101: canvas.drawCircle(291f, 350f, circleRadius, paint); break;
	     case 102: canvas.drawCircle(301.5f, 350f, circleRadius, paint); break;
		
	     // point 9 to 10
	     case 103: canvas.drawCircle(323.25f,350f, circleRadius, paint); break;
	     case 104: canvas.drawCircle(334.5f,350f, circleRadius, paint); break;
	     case 105: canvas.drawCircle(357f, 350f, circleRadius, paint); break;
	     case 106: canvas.drawCircle(379.5f, 350f, circleRadius, paint); break;
	     case 107: canvas.drawCircle(390.75f, 350f, circleRadius, paint); break;
	     
	     // point 8 to 12
	     case 108: canvas.drawCircle(228f, 357.3125f, circleRadius, paint); break;
	     case 109: canvas.drawCircle(228f, 364.625f, circleRadius, paint); break;
	     case 110: canvas.drawCircle(228f, 379.25f, circleRadius, paint); break;
	     case 111: canvas.drawCircle(228f, 408.5f, circleRadius, paint); break;
	     case 112: canvas.drawCircle(228f, 437.75f, circleRadius, paint); break;
	     case 113: canvas.drawCircle(228f, 452.375f, circleRadius, paint); break;
	     case 114: canvas.drawCircle(228f, 459.6875f, circleRadius, paint); break;
		
	     // point 9 to 13
	     case 115: canvas.drawCircle(312.1875f, 357.3125f, circleRadius, paint); break;
	     case 116: canvas.drawCircle(312.375f, 364.625f, circleRadius, paint); break;
	     case 117: canvas.drawCircle(312.75f, 379.25f, circleRadius, paint); break;
	     case 118: canvas.drawCircle(313.5f, 408.5f, circleRadius, paint); break;
	     case 119: canvas.drawCircle(314.25f, 437.75f, circleRadius, paint); break;
	     case 120: canvas.drawCircle(314.625f, 452.375f, circleRadius, paint); break;
	     case 121: canvas.drawCircle(314.8125f, 459.6875f, circleRadius, paint); break;
		
	     // point 10 to 14
	     case 122: canvas.drawCircle(401.875f, 357.3125f, circleRadius, paint); break;
	     case 123: canvas.drawCircle(401.75f, 364.625f, circleRadius, paint); break;
	     case 124: canvas.drawCircle(401.5f, 379.25f, circleRadius, paint); break;
	     case 125: canvas.drawCircle(401f, 408.5f, circleRadius, paint); break;
	     case 126: canvas.drawCircle(400.5f, 437.75f, circleRadius, paint); break;
	     case 127: canvas.drawCircle(400.25f, 452.375f, circleRadius, paint); break;
	     case 128: canvas.drawCircle(400.125f, 459.6875f, circleRadius, paint); break;
		
	     // point 11 to 12
	     case 129: canvas.drawCircle(86.4375f, 470.75f, circleRadius, paint); break;
	     case 130: canvas.drawCircle(95.875f, 470.5f, circleRadius, paint); break;
	     case 131: canvas.drawCircle(114.75f, 470f, circleRadius, paint); break;
	     case 132: canvas.drawCircle(152.5f, 469f, circleRadius, paint); break;
	     case 133: canvas.drawCircle(190.25f, 468f, circleRadius, paint); break;
	     case 134: canvas.drawCircle(209.125f, 467.5f, circleRadius, paint); break;
	     case 135: canvas.drawCircle(218.5625f, 467.25f, circleRadius, paint); break;
	     
	     // point 12 to 13
	     case 136: canvas.drawCircle(238.875f, 467f, circleRadius, paint); break;
	     case 137: canvas.drawCircle(249.75f, 467f, circleRadius, paint); break;
	     case 138: canvas.drawCircle(271.5f, 467f, circleRadius, paint); break;
	     case 139: canvas.drawCircle(293.25f, 467f, circleRadius, paint); break;
	     case 140: canvas.drawCircle(304.125f, 467f, circleRadius, paint); break;
	     	
	     // point 13 to 14
	     case 141: canvas.drawCircle(320.3125f, 467f, circleRadius, paint); break;
	     case 142: canvas.drawCircle(325.625f, 467f, circleRadius, paint); break;
	     case 143: canvas.drawCircle(336.25f, 467f, circleRadius, paint); break;
	     case 144: canvas.drawCircle(357.5f, 467f, circleRadius, paint); break;
	     case 145: canvas.drawCircle(378.75f, 467f, circleRadius, paint); break;
	     case 146: canvas.drawCircle(389.375f, 467f, circleRadius, paint); break;
	     case 147: canvas.drawCircle(394.6875f, 467f, circleRadius, paint); break;
	     	     	
	     // point 11 to 15
	     case 148: canvas.drawCircle(76.25f, 486.25f, circleRadius, paint); break;
	     case 149: canvas.drawCircle(75.5f, 502.5f, circleRadius, paint); break;
	     case 150: canvas.drawCircle(74.5f, 518.25f, circleRadius, paint); break;
	     	
	     // point 12 to 16
	     case 151: canvas.drawCircle(219f, 477.25f, circleRadius, paint); break;
	     case 152: canvas.drawCircle(210.5f, 487.5f, circleRadius, paint); break;
	     case 153: canvas.drawCircle(193f, 508f, circleRadius, paint); break;
	     case 154: canvas.drawCircle(175.5f, 528.5f, circleRadius, paint); break;
	     case 155: canvas.drawCircle(166.75f, 538.75f, circleRadius, paint); break;
	     	
	     // point 12, 13 to point 17	
	     case 156: canvas.drawCircle(256f, 479.25f, circleRadius, paint); break;
	     case 157: canvas.drawCircle(256f, 491.5f, circleRadius, paint); break;
	     case 158: canvas.drawCircle(256f, 516f, circleRadius, paint); break;
	     case 159: canvas.drawCircle(256f, 540.5f, circleRadius, paint); break;
	     case 160: canvas.drawCircle(256f, 552.75f, circleRadius, paint); break;
	     	
	     // 16 to 17
	     case 161: canvas.drawCircle(84.5f, 535.875f, circleRadius, paint); break;
	     case 162: canvas.drawCircle(95f, 537.75f, circleRadius, paint); break;
	     case 163: canvas.drawCircle(116f, 541.5f, circleRadius, paint); break;
	     case 164: canvas.drawCircle(137f, 545.25f, circleRadius, paint); break;
	     case 165: canvas.drawCircle(147.5f, 547.125f, circleRadius, paint); break;
	     	
	     // 17 to 18
	     case 166: canvas.drawCircle(170.25f, 551f, circleRadius, paint); break;
	     case 167: canvas.drawCircle(182.5f, 553f, circleRadius, paint); break;
	     case 168: canvas.drawCircle(207f, 557f, circleRadius, paint); break;
	     case 169: canvas.drawCircle(231f, 561f, circleRadius, paint); break;
	     default: break;
	}
    }
    
    @Override 
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	// id TAG 
	final String idTAG = "onProgressChanged";
	// Put the progress value to radius
	radius = progress;
	
	// Print value on the screen
	radText.setText("R: " + radius);
	mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
	// Set a new canvas, it will get throw if not use anymore
	canvas = new Canvas (mutableBitmap);
	// Set ready the imageView
	imageView.setAdjustViewBounds(true);
	imageView.setImageBitmap(mutableBitmap);
	
	// For all position in the List
	for (MyPosition myposition : listOfPosition) {
	    // Set a new variable to hold latitude and longitude
	    listlatlng = new LatLng(Double.parseDouble(myposition.getLatitude()), Double.parseDouble(myposition.getLongitude()));
	    // CompareTo the List
	    Location.distanceBetween(latitude, longitude, listlatlng.latitude, listlatlng.longitude, distance);
	    // If incase the distance is below 20
	    if (distance[0] < progress) {
		// Call drawdots method
		drawDots(Integer.parseInt(myposition.getLocationnumber()));
	    } else { 
		// Ignore it
	    }	
	}
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }
	
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
	// id TAG 
    	final String idTAG = "onStopTrackingTouch";
	radSeek.setSecondaryProgress(seekBar.getProgress());	
    }
    
    @Override
    public void onBackPressed() {
	// id TAG 
	final String idTAG = "onBackPressed";
	try {
	    	// Try to stop the location client 
	    	locClient.removeLocationUpdates(this);
	    } catch(Exception e) {
		Log.d(idTAG, "" + e);
	    } finally {
		// Close the database
		MyDB.close();		
		// Set end result to be successful
	 	setResult(0);
	 	// Our program is finish without error
	 	finish();
	    }
    }
}