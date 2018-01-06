package com.example.d062629.exchangepal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Clemens on 25.06.2017.
 */

public class DBUtils {

    private Context context;

    private SQLiteDatabase sdb;

    public DBUtils(Context context){
        this.context = context;
        initSQLiteDB();
    }

    private void initSQLiteDB(){
        // Create a DB helper (this will create the DB if run for the first time)
        Data dbHelper = new Data(context);
        sdb = dbHelper.getWritableDatabase();
    }

    protected String readTimestamp(){
        String timestamp = null;

        try {
            if (sdb == null) {
                initSQLiteDB();
            }

            Cursor cursor = sdb.rawQuery("SELECT " + Data.COLUMN_TIMESTAMP + " FROM " + Data.TABLE_NAME + " WHERE "
                                        + Data.COLUMN_CURRENCY + " = 'USD'", null);
            cursor.moveToFirst();

            if (!cursor.isNull(cursor.getColumnIndex(Data.COLUMN_TIMESTAMP))) {
                timestamp = cursor.getString(cursor.getColumnIndex(Data.COLUMN_TIMESTAMP));
            }

        }catch(Exception e){
            Log.e("EXCEPTION!!! ", e.toString());
        }

        return timestamp;
    }

    protected double readExchangeRate(String currencyIsoCode){
        try {
            if (sdb == null) {
                initSQLiteDB();
            }

            //query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
            Cursor cursor = sdb.rawQuery("SELECT * FROM " + Data.TABLE_NAME + " WHERE " + Data.COLUMN_CURRENCY + " = '" +
                                            currencyIsoCode + "' ORDER BY " + Data.COLUMN_TIMESTAMP + " DESC", null);
            cursor.moveToFirst();

            double result = -1.0;
            if (!cursor.isNull(cursor.getColumnIndex(Data.COLUMN_DOLLAR_AMOUNT))) {
                result = cursor.getDouble(cursor.getColumnIndex(Data.COLUMN_DOLLAR_AMOUNT));
            }

            return result;
        }catch(Exception e){
            Log.e("EXCEPTION!!! ", e.toString());
        }

        return -1.0;
    }

    protected void writeExchangeRate(String currencyIsoCode, double dollarConversionRate){
        if(sdb == null) {
            initSQLiteDB();
        }

        // Delete old exchange rate
        //sdb.delete(Data.TABLE_NAME, Data.COLUMN_CURRENCY + "='" + currencyIsoCode + "'", null);

        Log.d("DBUtils", "Inserting data! :-)");
        ContentValues cv = new ContentValues();
        cv.put(Data.COLUMN_AMOUNT, 1.0);
        cv.put(Data.COLUMN_CURRENCY, currencyIsoCode);
        cv.put(Data.COLUMN_DOLLAR_AMOUNT, dollarConversionRate);
        sdb.update(Data.TABLE_NAME, cv, Data.COLUMN_CURRENCY + " = '" + currencyIsoCode + "'", null);
        sdb.insert(Data.TABLE_NAME, "changes() = 0", cv);
    }

}
