package com.example.d062629.exchangepal;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Clemens on 25.06.2017.
 */

public class SyncExchangeRates extends AsyncTask {

    public interface TaskListener {
        public void onFinished(boolean success, String timestamp);
    }

    private TaskListener taskListener;
    private DBUtils dbUtils;
    private String[] currencies;

    public SyncExchangeRates(TaskListener taskListener, DBUtils dbUtils, String[] currencies){
        super();
        this.taskListener = taskListener;
        this.dbUtils = dbUtils;
        this.currencies = currencies;
    }

    private String getUrl(String currencyIsoOrigin, String currencyIsoTarget){
        return "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22" +
                currencyIsoOrigin + currencyIsoTarget
                + "%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    }

    private double updateCurrencyConversionRate(String currencyIsoOrigin, String currencyIsoTarget) {

        try {
            URL url = new URL(getUrl(currencyIsoOrigin, currencyIsoTarget));
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            double result = -1.0;
            result = root.getAsJsonObject()
                        .get("query").getAsJsonObject()
                        .get("results").getAsJsonObject()
                        .get("rate").getAsJsonObject()
                        .get("Rate").getAsDouble();

            Log.d("SyncExchangeRates", "Successfully retrieved data from Yahoo");

            return result;
        } catch (Exception e) {
            Log.e("Exception during sync!", e.getMessage() + " || StackTrace: " + e.toString());
            return -1.0;
        }

    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            for (String currency : currencies) {
                double exchangeRate = updateCurrencyConversionRate("USD", currency);
                if(exchangeRate == -1.0){
                    continue;
                }
                dbUtils.writeExchangeRate(currency, exchangeRate);
            }

            return new Boolean(true);

        }catch(Exception e){
            return new Boolean(false);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String timestamp = dbUtils.readTimestamp();
        taskListener.onFinished(true, timestamp);
    }
}
