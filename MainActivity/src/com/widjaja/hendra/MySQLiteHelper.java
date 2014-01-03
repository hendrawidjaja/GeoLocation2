package com.widjaja.hendra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/* 
 *  This is a class from MainActivity - Augmented Reality
 *  This class has been modified and adjusted to my project
 *  All rights are reserved. Copyright(c) 2013 Hendra Widjaja
 */
@SuppressWarnings("unused")
public class MySQLiteHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DB_VERSION = 1;
    // id TAG 
 	private final String mainTAG = "MySQLiteHelper";		
    
    /*
     * The Android's default system path of the application database in internal
     * storage. The package of the application is part of the path of the
     * directory.
     */
   
    private static String DB_DIR = "/data/data/com.widjaja.hendra/databases/";
    private final static String DB_NAME = "MyLocation.DB";
    private static String DB_PATH = DB_DIR + DB_NAME;
    private static String OLD_DB_PATH = DB_DIR + "old_" + DB_NAME;
    private static SQLiteDatabase db;
    private Cursor cursor;
	
    private static final String TABLE_LOCATIONS = "locations";
    private static final String KEY_ID = "_id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LOCATIONNUMBER = "locationnumber";
    private static final String[] COLUMNS = {KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_LOCATIONNUMBER};
    
    private static final String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + " (" + 
    							KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    							KEY_LATITUDE + " TEXT NOT NULL, " + 
    							KEY_LONGITUDE + " TEXT NOT NULL, " + 
    							KEY_LOCATIONNUMBER + " TEXT NOT NULL )";
    private final Context MySQLContext;
    private boolean createDatabase = false;
    private boolean upgradeDatabase = false;
 
    protected MySQLiteHelper(Context context) {
	super(context, DB_NAME, null, DB_VERSION);
	this.MySQLContext = context;
	DB_PATH = MySQLContext.getDatabasePath(DB_NAME).getAbsolutePath();	    
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    // id TAG 
    final String idTAG = "MySQLiteHelper onCreate";

	// create table
//	db.execSQL(CREATE_LOCATION_TABLE);
	createDatabase = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // id TAG 
    final String idTAG = "MySQLiteHelper onUpgrade";

	// Drop table if existed
//        db.execSQL("DROP TABLE IF EXISTS");
        // create table
//        this.onCreate(db);
	  upgradeDatabase = true;
    }
	
    /**
     * CRUD operations (create "add", read "get", update, delete)  + get all + delete all 
     */
    public void addPosition(MyPosition position) {
    // id TAG 
    final String idTAG = "addPosition";

    	// 1. get reference to writable DB
	db = this.getWritableDatabase();
		 
	// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, position.getLatitude()); 
        values.put(KEY_LONGITUDE, position.getLongitude());
        values.put(KEY_LOCATIONNUMBER, position.getLocationnumber());
        
        // 3. insert
        db.insert(TABLE_LOCATIONS, null, values); // key/value -> keys = column names/ values = column values
        
        // 4. close
        db.close(); 
    }
	
    public MyPosition getPosition(int id) {
    // id TAG 
    final String idTAG = "getPosition";

    	// 1. get reference to readable DB
	db = this.getReadableDatabase();
		 
	// 2. build query
        cursor = db.query(TABLE_LOCATIONS, COLUMNS, " id = ? ", new String[] { String.valueOf(id) }, null, null, null, null);
        
        // 3. if we got results get the first one
        if (cursor != null) cursor.moveToFirst();
 
        // 4. build   object
        MyPosition position = new MyPosition(null, null, null);
        position.setId(Integer.parseInt(cursor.getString(0)));
        position.setLatitude(cursor.getString(1));
        position.setLongitude(cursor.getString(2));
        position.setLocationnumber(cursor.getString(3));
        cursor.close();
        db.close();
        // 5. return  
        Log.d("getPosition", "" + id);
        return position;
    }
	
    // Get All 
    public ArrayList<MyPosition> getAllPositions() {
    // id TAG 
    final String idTAG = "getAllPosition";

    Cursor cursorGetAllPosition;
    ArrayList<MyPosition> positions = new ArrayList<MyPosition>();

    // 1. build the query
    String query = "SELECT * FROM " + TABLE_LOCATIONS;
 
    // 2. get reference to writable DB
    db = this.getWritableDatabase();
    cursorGetAllPosition = db.rawQuery(query, null);
 
    // 3. go over each row, build and add it to list
    MyPosition position = null;
    if (cursorGetAllPosition.moveToFirst()) {
	do {
	    position = new MyPosition(null, null, null);
            position.setId(Integer.parseInt(cursorGetAllPosition.getString(0)));
            position.setLatitude(cursorGetAllPosition.getString(1));
            position.setLongitude(cursorGetAllPosition.getString(2));
            position.setLocationnumber(cursorGetAllPosition.getString(3));
            
            // Add 
            positions.add(position);
            } while (cursorGetAllPosition.moveToNext());
        }    
        cursorGetAllPosition.close();
        db.close();
        return positions;
    }
    
    // Updating single  
    public int updatePosition(MyPosition position) {
        // id TAG 
     	final String idTAG = "updatePosition";

    	// 1. get reference to writable DB
        db = this.getWritableDatabase();
 
	// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("latitude", position.getLatitude()); 
        values.put("longitude", position.getLongitude());
        values.put("locationnumber", position.getLocationnumber());
 
        // 3. updating row
        int i = db.update(TABLE_LOCATIONS, //table
        		  values, // column/value
        		  KEY_ID+" = ?", // selections
                new String[] { String.valueOf(position.getId()) }); //selection args
        
        // 4. close
        db.close();       
        return i;       
    }

    // Deleting single  
    public void deletePosition(MyPosition position) {    	
        // id TAG 
     	final String idTAG = "deletePosition";

    	// 1. get reference to writable DB
        db = this.getWritableDatabase();
        
        // 2. delete
        db.delete(TABLE_LOCATIONS, KEY_ID + " = ?", new String[] { String.valueOf(position.getId()) });
        
        // 3. close
        db.close();
    }
    
    // Get size of element of records
    public int countLocation(MyPosition position) {
        // id TAG 
     	final String idTAG = "countLocation";

    	String countQuery = "SELECT  * FROM " + TABLE_LOCATIONS;
    	db = this.getWritableDatabase();
        cursor = db.rawQuery(countQuery, null);
   
        // return count
        return cursor.getCount();
    }
    
    private void copyDB() throws IOException {
        // id TAG 
     	final String idTAG = "copyDB";

        // Close SQLiteOpenHelper so it will commit the created empty database to internal storage.
        close();
        // Open the database in the assets folder as the input stream.
        InputStream myInput = MySQLContext.getAssets().open(DB_NAME);
        //  Open the empty db in interal storage as the output stream.
        OutputStream myOutput = new FileOutputStream(DB_PATH);
        // Copy over the empty db in internal storage with the database in theassets folder.
        copyFile(myInput, myOutput);
        // Access the copied database so SQLiteHelper will cache it and mark it as created.
        getWritableDatabase().close();
    }
    
    public void prepareDB() {
        // id TAG 
     	final String idTAG = "prepareDB";

        getWritableDatabase();
        if (createDatabase) {
            try {
                copyDB();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        } else if (upgradeDatabase) {
            try {
                copyFile(DB_PATH, OLD_DB_PATH);
                copyDB();
                SQLiteDatabase old_db = SQLiteDatabase.openDatabase(OLD_DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                SQLiteDatabase new_db = SQLiteDatabase.openDatabase(DB_PATH,null, SQLiteDatabase.OPEN_READWRITE);
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }
    
    /**
     * Creates the specified toFile that is a byte for byte a copy
     * of fromFile. If toFile already existed, then
     * it will be replaced with a copy of fromFile. The name and
     * path of toFile will be that of toFile. Both
     * fromFile and toFile will be closed by this
     * operation.
     * 
     * @param fromFile
     *            - InputStream for the file to copy from.
     * @param toFile
     *            - InputStream for the file to copy to.
     */
    public static void copyFile(InputStream fromFile, OutputStream toFile) throws IOException {
        // id TAG 
     	final String idTAG = "copyFile";

    	// transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = fromFile.read(buffer)) > 0) {
                toFile.write(buffer, 0, length);
            }
        }
        // Close the streams
        finally {
            try {
                if (toFile != null) {
                    try {
                        toFile.flush();
                    } finally {
                        toFile.close();
                    }
            }
            } finally {
                if (fromFile != null) {
                    fromFile.close();
                }
            }
        }
    }

    /**
     * Creates the specified toFile that is a byte for byte a copy
     * of fromFile. If toFile already existed, then
     * it will be replaced with a copy of fromFile. The name and
     * path of toFile will be that of toFile. Both
     * fromFile and toFile will be closed by this
     * operation.
     * 
     * @param fromFile
     *            - String specifying the path of the file to copy from.
     * @param toFile
     *            - String specifying the path of the file to copy to.
     */
    public static void copyFile(String fromFile, String toFile) throws IOException {
        // id TAG 
     	final String idTAG = "copyFile";

    	copyFile(new FileInputStream(fromFile), new FileOutputStream(toFile));
    }

    /**
     * Creates the specified toFile that is a byte for byte a copy
     * of fromFile. If toFile already existed, then
     * it will be replaced with a copy of fromFile. The name and
     * path of toFile will be that of toFile. Both
     * fromFile and toFile will be closed by this
     * operation.
     * 
     * @param fromFile
     *            - File for the file to copy from.
     * @param toFile
     *            - File for the file to copy to.
     */
    public static void copyFile(File fromFile, File toFile) throws IOException {
        // id TAG 
     	final String idTAG = "copyFile";

    	copyFile(new FileInputStream(fromFile), new FileOutputStream(toFile));
    }

    /**
     * Creates the specified toFile that is a byte for byte a copy
     * of fromFile. If toFile already existed, then
     * it will be replaced with a copy of fromFile. The name and
     * path of toFile will be that of toFile. Both
     * fromFile and toFile will be closed by this
     * operation.
     * 
     * @param fromFile
     *            - FileInputStream for the file to copy from.
     * @param toFile
     *            - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = fromFile.getChannel();
        FileChannel toChannel = toFile.getChannel();

        try {
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    /**
     * Parses a file containing sql statements into a String array that contains
     * only the sql statements. Comments and white spaces in the file are not
     * parsed into the String array. Note the file must not contained malformed
     * comments and all sql statements must end with a semi-colon ";" in order
     * for the file to be parsed correctly. The sql statements in the String
     * array will not end with a semi-colon ";".
     * 
     * @param sqlFile
     *            - String containing the path for the file that contains sql
     *            statements.
     * 
     * @return String array containing the sql statements.
     */
    private static String[] parseSqlFile(String sqlFile) throws IOException {
        // id TAG 
     	final String idTAG = "getAllPosition";

    	return parseSqlFile(new BufferedReader(new FileReader(sqlFile)));
    }

    /**
     * Parses a file containing sql statements into a String array that contains
     * only the sql statements. Comments and white spaces in the file are not
     * parsed into the String array. Note the file must not contained malformed
     * comments and all sql statements must end with a semi-colon ";" in order
     * for the file to be parsed correctly. The sql statements in the String
     * array will not end with a semi-colon ";".
     * 
     * @param sqlFile
     *            - InputStream for the file that contains sql statements.
     * 
     * @return String array containing the sql statements.
     */
    private static String[] parseSqlFile(InputStream sqlFile) throws IOException {
        return parseSqlFile(new BufferedReader(new InputStreamReader(sqlFile)));
    }

    /**
     * Parses a file containing sql statements into a String array that contains
     * only the sql statements. Comments and white spaces in the file are not
     * parsed into the String array. Note the file must not contained malformed
     * comments and all sql statements must end with a semi-colon ";" in order
     * for the file to be parsed correctly. The sql statements in the String
     * array will not end with a semi-colon ";".
     * 
     * @param sqlFile
     *            - Reader for the file that contains sql statements.
     * 
     * @return String array containing the sql statements.
     */
    private static String[] parseSqlFile(Reader sqlFile) throws IOException {
        return parseSqlFile(new BufferedReader(sqlFile));
    }

    /**
     * Parses a file containing sql statements into a String array that contains
     * only the sql statements. Comments and white spaces in the file are not
     * parsed into the String array. Note the file must not contained malformed
     * comments and all sql statements must end with a semi-colon ";" in order
     * for the file to be parsed correctly. The sql statements in the String
     * array will not end with a semi-colon ";".
     * 
     * @param sqlFile
     *            - BufferedReader for the file that contains sql statements.
     * 
     * @return String array containing the sql statements.
     */
    private static String[] parseSqlFile(BufferedReader sqlFile) throws IOException {
        String line;
        StringBuilder sql = new StringBuilder();
        String multiLineComment = null;

        while ((line = sqlFile.readLine()) != null) {
            line = line.trim();

            // Check for start of multi-line comment
            if (multiLineComment == null) {
                // Check for first multi-line comment type
                if (line.startsWith("/*")) {
                    if (!line.endsWith("}")) {
                        multiLineComment = "/*";
                    }
                // Check for second multi-line comment type
                } else if (line.startsWith("{")) {
                    if (!line.endsWith("}")) {
                        multiLineComment = "{";
                }
                // Append line if line is not empty or a single line comment
                } else if (!line.startsWith("--") && !line.equals("")) {
                    sql.append(line);
                } // Check for matching end comment
            } else if (multiLineComment.equals("/*")) {
                if (line.endsWith("*/")) {
                    multiLineComment = null;
                }
            // Check for matching end comment
            } else if (multiLineComment.equals("{")) {
                if (line.endsWith("}")) {
                    multiLineComment = null;
                }
            }
        }
        sqlFile.close();
        return sql.toString().split(";");
    }
 
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
 
    public String getDBDir() {
	return DB_DIR;
    }
    public String getDBName() {
	return DB_NAME;
    }
    public String getDBPath() {
	return DB_PATH;
    }
}