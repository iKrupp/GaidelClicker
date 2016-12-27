package com.example.gfc.gaidelclicker.bonus;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gfc.gaidelclicker.Building;
import com.example.gfc.gaidelclicker.R;

/**
 * Created by user on 26.12.2016.
 */

public class BuildingHolder extends RecyclerView.ViewHolder {

    private TextView amount;
    private TextView cost;
    private TextView name;
    private ImageView image;

    private Building bonus;

    public BuildingHolder(View itemView, final OnBuildingClickListener onBonusClickListener) {
        super(itemView);
        amount = (TextView) itemView.findViewById(R.id.amount);
        cost = (TextView) itemView.findViewById(R.id.cost);
        image = (ImageView) itemView.findViewById(R.id.image);
        name = (TextView) itemView.findViewById(R.id.name);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bonus != null) {
                    onBonusClickListener.onBonusClick(bonus);
                }
            }
        });
    }

    public void bind(Building bonus) {
        this.bonus = bonus;
        amount.setText(String.valueOf(bonus.getCount()));
        image.setImageResource(bonus.getImageResourceId());
        cost.setText("Цена: " + String.valueOf(Math.round(bonus.getPrice())));//TODO string format
        name.setText(bonus.getName());
    }
}