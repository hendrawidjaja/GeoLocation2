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

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.location.Location;
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

import com.example.android.location.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
 
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
 *  All rights are reserved. Copyright(c) 2013 Hendra Widjaja
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
    private static TextView mLatLng;
    private static ImageView imageView;
    
    // My Library
    private static final int RAD_SEEKBAR_MIN = 00;
    private static final int RAD_SEEKBAR_MAX = 10;
    // By convention
    // http://wiki.answers.com/Q/What_goes_first_Longitude_or_Latitude?#slide=2
    // latitude, north -> south
    // longitude, east -> west
    private double latitude, longitude, accuracy;
    private int radius;
    private boolean radiusTriggered;
    private float[] distance = new float[2];
    
    private Location location;
    private SeekBar radSeek;
    private TextView radText;
    private MySQLiteHelper MyDB;
    private SQLiteDatabase SQLDB;
    private List<MyPosition> listOfPosition;
    private LatLng listlatlng;
    private Paint paint;
    private Canvas canvas;
    private Path path;
    private Context context = this; 
    private Bitmap workingBitmap;
    private Bitmap mutableBitmap;
    private Bitmap bitmap;
    private Bitmap customBitmap;
    private BitmapFactory.Options myOptions;
    
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
     	initializeLocation();
     	
     	if ((latitude >= 0.01) || (longitude >= 0.01)) {
     	    initializeRadSeeker();
     	} else {
     	    radSeek.setEnabled(false);
     	}
    }
    
    private void initialize() {
	// id TAG 
	final String idTAG = "initialize";
		
	// Get handles to the UI view objects
        mLatLng = (TextView) findViewById(R.id.lat_lng);
        radSeek = (SeekBar) findViewById(R.id.radSeek);
        imageView = (ImageView)findViewById(R.id.imageView);
        radText = (TextView) findViewById(R.id.radText);
   
        myOptions = new BitmapFactory.Options();
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important       
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map, myOptions);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        workingBitmap = Bitmap.createBitmap(bitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);    	
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
        	MyDB.close();
            } catch (Exception e) {
                Log.d(idTAG, "MyDB close" + e);
            } finally {
                SQLDB.close();
            }
        }        
        MyDB = new MySQLiteHelper(this);
        listOfPosition = MyDB.getAllPositions();
   }
	
    /*
     * radBar Initilization
     */
    protected void initializeRadSeeker() {
	// id TAG 
	final String idTAG = "initializeRadSeeker";
	
        radSeek.setOnSeekBarChangeListener(this);       
        radText.setText("R: " + radius);
	radiusTriggered = false;
	radSeek.setProgress(RAD_SEEKBAR_MIN);
	radSeek.setMax(RAD_SEEKBAR_MAX);
    }
    
    protected void initializeLocation() {
	// id TAG 
	final String idTAG = "initializeLocationPreSetting";
	
	/*
         * Set the update interval
         */
        locReq = LocationRequest.create();
        locReq.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  					// Use high accuracy
        locReq.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS); // Set the interval ceiling to one minute
        locReq.setExpirationDuration(300);
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        locClient = new LocationClient(this, this, this);
    }

    /*
     * drawingDots Initialization
     */
    public void drawDots(int locationNumber) {
	// id TAG 
	final String idTAG = "drawDots";
	int circleR = 6;
		
	// 52.4562018, 13.5260123	52.4563944, 13.5255759
	// 52.4549391 , 13.5268336    52.4555439 , 13.527565
	
	paint.setAntiAlias(true);
	paint.setColor(Color.BLUE);
	paint.setStrokeWidth(1);
	paint.setTextSize(16);

	imageView.setAdjustViewBounds(true);
	imageView.setImageBitmap(mutableBitmap);

	// Add a custom
	// just place the file inside the res directory and set the name after the drawable.
	//
	customBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.anton);

	switch (locationNumber) {
	case 1: {
	    	     canvas.drawCircle(87, 41, circleR, paint); break;
		}
	case 2: canvas.drawCircle(141, 88, circleR, paint); break;
	case 3: canvas.drawCircle(230, 92, circleR, paint); break;
	case 4: canvas.drawCircle(395, 112, circleR, paint); break;
	case 5: canvas.drawCircle(83, 233, circleR, paint); break;
	case 6: canvas.drawCircle(175, 179, circleR, paint); break;
	case 7: canvas.drawCircle(228, 254, circleR, paint); break;
	case 8: canvas.drawCircle(176, 350, circleR, paint); break;
	case 9: canvas.drawCircle(228, 349, circleR, paint); break;
	case 10: canvas.drawCircle(312, 349, circleR, paint); break;
	case 11: canvas.drawCircle(402, 350, circleR, paint); break;
	case 12: canvas.drawCircle(77, 471, circleR, paint); break;
	case 13: canvas.drawCircle(228, 467, circleR, paint); break;
	case 14: canvas.drawCircle(315, 467, circleR, paint); break;
	case 15: canvas.drawCircle(400, 467, circleR, paint); break;
	case 16: canvas.drawCircle(74, 534, circleR, paint); break;
	case 17: canvas.drawCircle(158, 549, circleR, paint); break;
	case 18: canvas.drawCircle(256, 565, circleR, paint); break;	
	}
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
		accuracy = location.getAccuracy();	
        }
    }
 
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
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
                default: {
                    Toast.makeText(this, "Google Play Service Error " + resultCode, Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                         }
                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
//               Log.d(LocationUtils.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
               break;
        }
    }

    /**
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
//            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));
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

    /**
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
        locClient.requestLocationUpdates(locReq, this);
    	location = locClient.getLastLocation();
        latitude = location.getLatitude();
    	longitude = location.getLongitude();
    	accuracy = location.getAccuracy();
    		
        // Display the current location in the UI
        mLatLng.setText("" + latitude + "    " + longitude);
        radSeek.setEnabled(true);
        initializeRadSeeker();
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
    	location = locClient.getLastLocation();
	latitude = location.getLatitude();
	longitude = location.getLongitude();
	accuracy = location.getAccuracy();
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
                connectionResult.startResolutionForResult(
                        this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else { }
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
       // In the UI, set the latitude and longitude to the value received
        mLatLng.setText(LocationUtils.getLatLng(this, location));
        location = locClient.getLastLocation();
	latitude = location.getLatitude();
	longitude = location.getLongitude();
	accuracy = location.getAccuracy();
    }
    
    @Override 
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	// id TAG 
	final String idTAG = "onProgressChanged";
	radius = progress;

	radText.setText("R: " + radius);
	mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
	canvas = new Canvas (mutableBitmap);
	imageView.setAdjustViewBounds(true);
	imageView.setImageBitmap(mutableBitmap);
	
	for (MyPosition myposition : listOfPosition) {	        	
	    listlatlng = new LatLng(Double.parseDouble(myposition.getLatitude()), Double.parseDouble(myposition.getLongitude()));
//	    Location.distanceBetween(52.4583809, 13.5267019, listlatlng.latitude, listlatlng.longitude, distance);	
	    Location.distanceBetween(latitude, longitude, listlatlng.latitude, listlatlng.longitude, distance);
	    if (distance[0] < progress) {
		drawDots(Integer.parseInt(myposition.getLocationnumber()));
	    } else { }	
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
	    	locClient.removeLocationUpdates(this);
	    } catch(Exception e) {
		Log.d(idTAG, "" + e);
	    }
		MyDB.close();			
	 	setResult(0);
	 	finish();
    }
}