package org.ipctabernacle.tabernacle;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Charles Koshy on 3/1/2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/org.ipctabernacle.tabernacle/databases/";
    private static String DB_NAME = "directory2.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private static final String col_1 = "ID";
    private static final String col_2 = "Title";
    private static final String col_3 = "FirstName";
    private static final String col_4 = "Middle";
    private static final String col_5 = "LastName";
    private static final String col_6 = "WifesName";
    private static final String col_7 = "Nicknames";
    private static final String col_8 = "Children";
    private static final String col_9 = "Other";
    private static final String col_10 = "OtherDescription";
    private static final String col_11 = "Address";
    private static final String col_12 = "City";
    private static final String col_13 = "State";
    private static final String col_14 = "ZipCode";
    private static final String col_15 = "Home";
    private static final String col_16 = "Cell";
    private static final String col_17 = "SecondaryCell";
    ArrayList<String> results = new ArrayList<>();
    ArrayList<String> details = new ArrayList<>();

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public void createDataBase() throws IOException {
        //boolean dbExist = checkDataBase();
        //if(dbExist) {
        //do nothing - database already exists
        //} else {
        this.getReadableDatabase();
        try {
            copyDataBase();
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
        //}
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //database doesn't exist yet
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public void openAndQueryDatabase() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT _id, Title, FirstName, Middle, LastName, WifesName, Children, Address, City, State, ZipCode FROM members", null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                cursor.getInt(cursor.getColumnIndex("_id"));
                String str1 = cursor.getString(cursor.getColumnIndex("Title"));
                String str2 = cursor.getString(cursor.getColumnIndex("FirstName"));
                String str3 = cursor.getString(cursor.getColumnIndex("Middle"));
                String str4 = cursor.getString(cursor.getColumnIndex("LastName"));
                String str5 = cursor.getString(cursor.getColumnIndex("WifesName"));
                String str6 = cursor.getString(cursor.getColumnIndex("Children"));
                String str7 = cursor.getString(cursor.getColumnIndex("Address"));
                String str8 = cursor.getString(cursor.getColumnIndex("City"));
                String str9 = cursor.getString(cursor.getColumnIndex("State"));
                String str10 = cursor.getString(cursor.getColumnIndex("ZipCode"));
                results.add(str2 + " " + str3 + " " + str4 + " & " + str5);
                details.add(str1 + " " + str2 + " " + str3 + " " + str4 + " & " + str5 + "\n" + str6 + "\n\n" + str7 +"\n" + str8 + ", " + str9 + " " +str10);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
