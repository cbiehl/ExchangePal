package com.example.d062629.exchangepal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Clemens on 03.07.2017.
 */

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context ctx;
    private String[] contentArray;
    private int[] imageArray;

    public SpinnerAdapter(Context context, int resource, String[] objects, int[] imageArray) {
        super(context, R.layout.spinner_row, R.id.spinnerTextView, objects);
        this.ctx = context;
        this.contentArray = objects;
        this.imageArray = imageArray;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_row, parent, false);

        TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
        textView.setText(contentArray[position]);

        TextView textViewSymbol = (TextView) row.findViewById(R.id.spinnerTextViewSymbol);
        textViewSymbol.setText("(" + CurrencyUtils.getCurrencySymbol(contentArray[position]) + ")");

        ImageView imageView = (ImageView) row.findViewById(R.id.spinnerIcon);
        imageView.setImageResource(imageArray[position]);

        return row;
    }
}
