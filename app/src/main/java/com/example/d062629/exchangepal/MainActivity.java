package com.example.d062629.exchangepal;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    static final int MIN_DISTANCE = 100;

    private DBUtils dbUtils;
    private String[] currencies;
    private Vector<Product> products;
    private float x1,x2;

    private RelativeLayout rootView;
    private ImageView ivExchangeRateStatusImage;
    private TextView tvExchangerateStatus;
    private TextView tvCurrencyForAmount;
    private TextView tvCurrencyForResult;
    private EditText etAmount;
    private EditText etResult;
    private Spinner spOriginCurrency;
    private Spinner spTargetCurrency;
    private ImageButton btnExchange;
    private ImageButton btnSwapCurrencies;
    private Button btnLocate;
    private Button btnClearText;
    private RecyclerView rvProducts;

    String originCurrency;
    String targetCurrency;
    double originToTarget; // last used conversion rate


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencies = getResources().getStringArray(R.array.currencies);
        int[] flagImages = new int[]{ R.drawable.flag_of_europe,
                R.drawable.flag_of_the_united_states,
                R.drawable.flag_of_canada,
                R.drawable.flag_of_the_united_kingdom,
                R.drawable.flag_of_hongkong,
                R.drawable.flag_of_the_peoples_republic_of_china,
                R.drawable.flag_of_japan,
                R.drawable.flag_of_india,
                R.drawable.flag_of_poland,
                R.drawable.flag_of_denmark,
                R.drawable.flag_of_sweden
        };

        // Get layout elements
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        ivExchangeRateStatusImage = (ImageView) findViewById(R.id.ivExchangerateStatusImage);
        tvExchangerateStatus = (TextView) findViewById(R.id.tvExchangerateStatus);
        tvCurrencyForAmount = (TextView) findViewById(R.id.tvCurrencyForAmount);
        tvCurrencyForResult = (TextView) findViewById(R.id.tvCurrencyForResult);
        etAmount = (EditText) findViewById(R.id.editTextAmount);
        etResult = (EditText) findViewById(R.id.editTextResult);
        spOriginCurrency = (Spinner) findViewById(R.id.spinnerOriginCurrency);
        spTargetCurrency = (Spinner) findViewById(R.id.spinnerTargetCurrency);
        btnExchange = (ImageButton) findViewById(R.id.buttonExchange);
        btnSwapCurrencies = (ImageButton) findViewById(R.id.buttonSwapCurrencies);
        btnLocate = (Button) findViewById(R.id.buttonLocate);
        btnClearText = (Button) findViewById(R.id.buttonClearText);
        rvProducts = (RecyclerView) findViewById(R.id.productslideshow);

        etResult.setInputType(InputType.TYPE_NULL);
        etResult.setTextIsSelectable(true);
        etResult.setKeyListener(null);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawable icon = getDrawable(R.drawable.currencybackground);
        icon.setAlpha(100);
        toolbar.setBackground(icon);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleMarginStart(dpToPx(10));
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setContentInsetsRelative(0, 10);
        toolbar.setContentInsetsRelative(10, 50);
        toolbar.setTitleTextColor(Color.BLACK);

        // Register spinner adapter
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_row, currencies, flagImages);
        spOriginCurrency.setAdapter(adapter);
        spTargetCurrency.setAdapter(adapter);

        // Get last used currencies
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.preference_file), this.MODE_PRIVATE);
        originCurrency = sp.getString(getString(R.string.lastusedorigincurrency), null);
        targetCurrency = sp.getString(getString(R.string.lastusedtargetcurrency), null);

        if(originCurrency != null && targetCurrency != null){
            int originIndex = -1;
            int targetIndex = -1;
            int i = 0;

            while((originIndex == -1 || targetIndex == -1) && i < currencies.length){
                if(currencies[i].equals(originCurrency))
                    originIndex = i;
                else if(currencies[i].equals(targetCurrency))
                    targetIndex = i;

                i++;
            }

            spOriginCurrency.setSelection(originIndex);
            spTargetCurrency.setSelection(targetIndex);
        }

        // Initilaize SQLite DB Reference
        dbUtils = new DBUtils(this);

        products = new Vector<Product>();

        TreeMap<String, Double> pricesWater = new TreeMap<String, Double>(); //{ 0.50, 0.80, 0.60, 0.40, 5.00, 4.00, 65.00, 32.00, 2.00, 3.00, 4.50 }
        pricesWater.put("EUR", 0.50);
        pricesWater.put("USD", 0.80);
        pricesWater.put("CAD", 0.60);
        pricesWater.put("GBP", 0.40);
        pricesWater.put("HKD", 5.00);
        pricesWater.put("CNY", 4.00);
        pricesWater.put("JPY", 65.00);
        pricesWater.put("INR", 32.00);
        pricesWater.put("PLN", 2.00);
        pricesWater.put("DKK", 3.00);
        pricesWater.put("SEK", 4.50);

        products.add(new Product("A bottle of water (1l)", pricesWater, getDrawable(R.drawable.water)));

        TreeMap<String, Double> pricesBigMac = new TreeMap<String, Double>();
        pricesBigMac.put("EUR", 3.88);
        pricesBigMac.put("USD", 5.06);
        pricesBigMac.put("CAD", 5.98);
        pricesBigMac.put("GBP", 3.09);
        pricesBigMac.put("HKD", 19.20);
        pricesBigMac.put("CNY", 19.60);
        pricesBigMac.put("JPY", 380.00);
        pricesBigMac.put("INR", 170.00);
        pricesBigMac.put("PLN", 9.60);
        pricesBigMac.put("DKK", 30.00);
        pricesBigMac.put("SEK", 48.00);

        products.add(new Product("A Big Mac Burger", pricesBigMac, getDrawable(R.drawable.bigmac)));

        TreeMap<String, Double> pricesApple = new TreeMap<String, Double>(); //{ 0.50, 0.80, 0.70, 0.50, 5.00, 3.50, 66.00, 32.00, 2.00, 3.50, 4.50 }
        pricesApple.put("EUR", 0.50);
        pricesApple.put("USD", 0.80);
        pricesApple.put("CAD", 0.70);
        pricesApple.put("GBP", 0.50);
        pricesApple.put("HKD", 5.00);
        pricesApple.put("CNY", 3.50);
        pricesApple.put("JPY", 66.00);
        pricesApple.put("INR", 32.00);
        pricesApple.put("PLN", 2.00);
        pricesApple.put("DKK", 3.50);
        pricesApple.put("SEK", 4.50);

        products.add(new Product("An Apple", pricesApple, getDrawable(R.drawable.apfel)));

        rvProducts.setAdapter(new ProductAdapter(products, targetCurrency));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvProducts.setLayoutManager(layoutManager);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(rvProducts);

        // Add onClick listeners for buttons
        btnSwapCurrencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = MainActivity.this.originCurrency;
                MainActivity.this.originCurrency = MainActivity.this.targetCurrency;
                MainActivity.this.targetCurrency = tmp;
                originToTarget = 1 / originToTarget;

                int targetCurrency = spTargetCurrency.getSelectedItemPosition();
                spTargetCurrency.setSelection(spOriginCurrency.getSelectedItemPosition(), true);
                spOriginCurrency.setSelection(targetCurrency, true);

                convertAndDisplayAmount();
            }
        });

        btnClearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAmount.setText("");
                etResult.setText("");
            }
        });

        btnExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = convertAndDisplayAmount();

                if(success){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }else{
                    etAmount.requestFocus();
                }
            }
        });

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTargetCurrencyByGeoLocation();
            }
        });

        etAmount.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            // Convert currency and display result
                            boolean success = convertAndDisplayAmount();

                            // Hide soft keyboard
                            View view = MainActivity.this.getCurrentFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (success && view != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }else{
                                etAmount.requestFocus();
                                imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
                            }

                            return true;
                        default:
                            break;
                    }
                }

                return false;
            }
        });

        /*
        etAmount.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                // Convert currency and display result
                convertAndDisplayAmount();

                // Hide soft keyboard
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                return true;
            }

        });
        */

        etAmount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            String amount = etAmount.getText().toString();
                            if(amount == null || amount.isEmpty())
                                return MainActivity.super.onTouchEvent(event);

                            if(deltaX < 0){
                                etAmount.setText(String.valueOf(Double.parseDouble(amount) / 10));
                            }else{
                                etAmount.setText(String.valueOf(Double.parseDouble(amount) * 10));
                            }

                            convertAndDisplayAmount();
                        }
                        break;
                }

                return MainActivity.super.onTouchEvent(event);
            }
        });

        spOriginCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertAndDisplayAmount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTargetCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertAndDisplayAmount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvExchangerateStatus.setText(getString(R.string.exchangeratestatus) + " " + dbUtils.readTimestamp());

        syncExchangeRateData();
    }

    // Set menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.update) {
            syncExchangeRateData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void syncExchangeRateData() {
        SyncExchangeRates.TaskListener taskListener = new SyncExchangeRates.TaskListener() {
            @Override
            public void onFinished(boolean success, String timestamp) {
                if (success) {
                    showMessage(getString(R.string.successful_sync));
                    ivExchangeRateStatusImage.setColorFilter(getColor(R.color.colorUptoDate));
                } else {
                    showMessage(getString(R.string.could_not_sync));
                    ivExchangeRateStatusImage.setColorFilter(getColor(R.color.colorNotUptoDate));
                }

                MainActivity.this.tvExchangerateStatus.setText(getString(R.string.exchangeratestatus) + " " + timestamp);
            }
        };

        new SyncExchangeRates(taskListener, dbUtils, currencies).execute();
    }

    private boolean convertAndDisplayAmount() {
        originCurrency = (String) spOriginCurrency.getSelectedItem();
        targetCurrency = (String) spTargetCurrency.getSelectedItem();
        tvCurrencyForAmount.setText(CurrencyUtils.getCurrencySymbol(originCurrency));
        tvCurrencyForResult.setText(CurrencyUtils.getCurrencySymbol(targetCurrency));
        String amount = etAmount.getText().toString();

        ((ProductAdapter) rvProducts.getAdapter()).setCurrency(targetCurrency);

        if(amount == null || amount.isEmpty())
            return false;

        amount = amount.replace(",", ".");

        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showMessage(getString(R.string.wrongnumberformat));
            return false;
        }

        double originToDollar = dbUtils.readExchangeRate(originCurrency);
        double targetToDollar = dbUtils.readExchangeRate(targetCurrency);

        if(originToDollar == -1 || targetToDollar == -1){
            showMessage(getString(R.string.unknown_currency));
            etResult.setText("");
            return false;
        }

        originToTarget = targetToDollar / originToDollar;
        double result = originToTarget * Double.parseDouble(amount);

        etResult.setText(formatDouble(result));

        return true;
    }

    private void setTargetCurrencyByGeoLocation() {
        String currencyIso = getCurrencyByGeolocation();

        if(currencyIso == null || currencyIso.isEmpty())
            return;

        Log.d("Currency by Geolocation", currencyIso);

        int i;
        for(i = 0; i < currencies.length; i++){
            if(currencies[i].equals(currencyIso)) {
                spTargetCurrency.setSelection(i);
                convertAndDisplayAmount();
                break;
            }
        }
    }

    @Nullable
    private String getCurrencyByGeolocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION,
                                                                  Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION,
                                                                  Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        String currencyIso = null;
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        for (String provider : lm.getAllProviders()) {
            try {
                Location loc = lm.getLastKnownLocation(provider);

                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                currencyIso = getCurrencyIso(addresses.get(0).getLocale());
                Log.d("Country ISO Code:", currencyIso);

                return currencyIso;

            } catch(Exception e){
                showMessage(getString(R.string.location_unknown));
                e.printStackTrace();
            }
        }

        return null;
    }

    private static String getCurrencyIso(Locale locale){
        return Currency.getInstance(locale).getCurrencyCode();
    }

    private String formatDouble(double d) {
        DecimalFormat f;
        if (d <= 0.01) {
            d = Math.round(d * 10000);
            d = d/10000;
            f = new DecimalFormat("0.####");
        } else {
            d = Math.round(d * 10000);
            d = d/10000;
            f = new DecimalFormat("##.####");
        }

        return f.format(d);
    }

    // Show a user message (via Snackbar)
    private void showMessage(String messageText){
        Snackbar.make(rootView, messageText, Snackbar.LENGTH_LONG).show();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = MainActivity.this.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(getString(R.string.lastusedtargetcurrency), (String) spTargetCurrency.getSelectedItem());
        editor.putString(getString(R.string.lastusedorigincurrency), (String) spOriginCurrency.getSelectedItem());
        editor.commit();
    }
}
