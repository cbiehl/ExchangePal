package com.example.d062629.exchangepal;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Clemens on 10.07.2017.
 */

public class ProductViewHolder extends RecyclerView.ViewHolder {

    protected TextView tv;
    protected ImageView iv;

    public ProductViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.tvProductText);
        iv = (ImageView) itemView.findViewById(R.id.ivProductImage);
    }

}
