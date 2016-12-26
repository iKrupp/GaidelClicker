package com.example.gfc.gaidelclicker;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gfc.gaidelclicker.bonus.BonusesAdapter;
import com.example.gfc.gaidelclicker.bonus.OnBonusClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageButton Gaidel;
    private Button Stats;
    private ImageView svaston;
    public static TextView countOfClick;

    private RecyclerView recyclerView;
    private BonusesAdapter adapter;

    private Spinner spinnerScheme;

    private int maximum = 0;
    static int maximumAll = 0;
    static double count = 0;
    static double delta = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Gaidel = (ImageButton) findViewById(R.id.buttonGaidel);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        Stats = (Button) findViewById(R.id.buttonStat);

        spinnerScheme = (Spinner) findViewById(R.id.spinner);


        svaston = (ImageView) findViewById(R.id.imageView);

        countOfClick = (TextView) findViewById(R.id.clicks);
        countOfClick.setText(Integer.toString((int) count));
        countOfClick.setTextSize(36);


        AutoClickerThread autoClick = new AutoClickerThread();


        ObjectAnimator anim = ObjectAnimator.ofFloat(svaston, View.ROTATION, 0f, 360f);
        anim.setRepeatCount(-1);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(2000);
        anim.start();


        View.OnClickListener clickOnGaidel = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                maximum++;
                countOfClick.setText(Integer.toString((int) count));


            }
        };
        View.OnClickListener clickOnStat = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "Кликов в секунду: " + Double.toString(delta) + "\nВсего накликано: " + maximum + "\nВсего собрано: " + (maximumAll + maximum), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 5);
                toast.show();
            }
        };

        Gaidel.setOnClickListener(clickOnGaidel);
        Gaidel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        Gaidel.setScaleX(1f);
                        Gaidel.setScaleY(1f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        Gaidel.setScaleX(0.99f);
                        Gaidel.setScaleY(0.99f);
                        break;
                    }
                }
                return false;
            }
        });
        Stats.setOnClickListener(clickOnStat);

        initRecycler();

        spinnerScheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {

                String[] choose = getResources().getStringArray(R.array.gaidellist);
                switch (selectedItemPosition) {
                    case 0: Gaidel.setBackground(getResources().getDrawable(R.drawable.gaidel_face_gold)); break;
                    case 1: Gaidel.setBackground(getResources().getDrawable(R.drawable.gaidel_face_pink)); break;
                    case 2: Gaidel.setBackground(getResources().getDrawable(R.drawable.gaidel_face_dark)); break;
                    default: Gaidel.setBackground(getResources().getDrawable(R.drawable.gaidel_face)); break;
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new BonusesAdapter();
        adapter.setOnBonusClickListener(new OnBonusClickListener() {
            @Override
            public void onBonusClick(Bonus bonus) {
                if (count > bonus.getPrice()) {
                    count -= bonus.getPrice();
                    bonus.buy();
                    delta += bonus.getDelta();
                    countOfClick.setText(String.valueOf((int) MainActivity.count));
                    adapter.notifyDataSetChanged();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        List<Bonus> bonusesData = new ArrayList<>();
        bonusesData.add(new Bonus(R.drawable.click_gaidel, 20, 0.1));
        bonusesData.add(new Bonus(R.drawable.factory_of_gaidel, 150, 1));
        bonusesData.add(new Bonus(R.drawable.farm_of_gaidel, 560, 8));
        //todo change next 3 lines
        bonusesData.add(new Bonus(R.drawable.click_gaidel, 20, 0.1));
        bonusesData.add(new Bonus(R.drawable.factory_of_gaidel, 150, 1));
        bonusesData.add(new Bonus(R.drawable.farm_of_gaidel, 560, 8));
        adapter.setData(bonusesData);
    }


}
