package com.example.d062629.exchangepal;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by Clemens on 10.07.2017.
 */

public class ProductAdapter extends RecyclerView.Adapter {

    private Vector<Product> products;
    private String currency;
    private Vector<TextView> tvs;

    public ProductAdapter(Vector<Product> products, String currency){
        this.products = products;
        this.currency = currency;
        this.tvs = new Vector<TextView>();
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        for(int i = 0; i < tvs.size(); i++) {
            Product p = products.get(i);
            TextView tv = tvs.get(i);
            tv.setText(p.getName() + " would cost you about " + p.getPrice(currency) + " " + CurrencyUtils.getCurrencySymbol(currency));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        view.setMinimumWidth(parent.getMeasuredWidth());
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Product p = products.get(position);

        TextView tv = ((ProductViewHolder) holder).tv;
        tv.setText(p.getName() + " would cost you about " + p.getPrice(currency) + " " + CurrencyUtils.getCurrencySymbol(currency));
        tvs.add(tv);

        ImageView iv = ((ProductViewHolder) holder).iv;
        iv.setImageDrawable(p.getImage());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
