package com.example.d062629.exchangepal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Clemens on 25.06.2017.
 */

public class Data extends SQLiteOpenHelper {

    // The database names
    private static final String DATABASE_NAME = "exchangepal.db";
    protected static final String TABLE_NAME = "CONVERSION_RATES_TO_US_DOLLAR";
    protected static final String _ID = "ID";
    protected static final String COLUMN_CURRENCY = "CURRENCY";
    protected static final String COLUMN_AMOUNT = "AMOUNT";
    protected static final String COLUMN_DOLLAR_AMOUNT = "AMOUNT_IN_US_DOLLAR";
    protected static final String COLUMN_TIMESTAMP = "LASTCHANGE";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    public Data(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // CREATE TABLE
        final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_CURRENCY + " VARCHAR(3) NOT NULL, " +
                COLUMN_AMOUNT + " DOUBLE NOT NULL, " +
                COLUMN_DOLLAR_AMOUNT + " DOUBLE NOT NULL, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))" +
                /*COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +*/
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);

        sqLiteDatabase.execSQL("CREATE TRIGGER UpdateLastTimestamp AFTER UPDATE ON " + TABLE_NAME + " FOR EACH ROW BEGIN " +
                                "UPDATE " + TABLE_NAME + " SET " + COLUMN_TIMESTAMP +
                                " = (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')) WHERE " +
                                _ID + " = old." + _ID + "; END");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Database version should never change
        // If the database us upgraded, it is dropped and a new one is created
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}