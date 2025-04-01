package com.example.watermyplants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBase extends SQLiteOpenHelper {

    //Create all needed variables
    private final Context context;
    private static final String DATABASE_NAME = "WaterMyPlantsDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIMER = "timer";

    public static final String COLUMN_START_TIMER = "start_time";

    //Constructor to connect context
    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    //Creating the table with columns
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_IMAGE + " BLOB, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_TIMER + " INTEGER, " +
                COLUMN_START_TIMER + " INTEGER);";

        db.execSQL(query);
    }

    //OnUpgrade called when table already exists
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Adding plant to the database
    void addPlant(byte[] imageID, String name, String description, long timer, long start_timer) {
        //Continue writing in the database
        SQLiteDatabase db = this.getWritableDatabase();
        //Created to insert new data in columns
        ContentValues cv = new ContentValues();

        //Adding data to columns
        cv.put(COLUMN_IMAGE, imageID);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_TIMER, timer);
        cv.put(COLUMN_START_TIMER, start_timer);

        //In case something goes wrong and can't add info to the table
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Failed to add plant to Database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Added plant to Database", Toast.LENGTH_SHORT).show();
        }
    }

    //To be able to remove a plant column
    void removePlant(String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if(result == -1) {
            Toast.makeText(context, "Not able to delete", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        }


    }

    //To be able to update the plant
    void updatePlant(String row_id, byte[] imageID, String name, String description, long timer, long start_time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IMAGE, imageID);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_TIMER, timer);
        cv.put(COLUMN_START_TIMER, start_time);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if (result == -1) {
            Toast.makeText(context, "Failed to update the plant", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    //Return a cursor which contains all the information from database
    Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME, null, null);
        if(result == -1) {
            Toast.makeText(context, "Failed to delete plants", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted Plants", Toast.LENGTH_SHORT).show();
        }
    }
}
