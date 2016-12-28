package com.example.gfc.gaidelclicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gfc.gaidelclicker.bonus.BuildingsAdapter;
import com.example.gfc.gaidelclicker.bonus.BuildingsRepository;
import com.example.gfc.gaidelclicker.bonus.OnBuildingClickListener;
import com.example.gfc.gaidelclicker.utils.FormatUtils;
import com.example.gfc.gaidelclicker.utils.UIUtils;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int MIN_GOLD_COOKIE_SPAWN_PERIOD = 10 * 1000;
    private static final int MAX_GOLD_COOKIE_SPAWN_PERIOD = 30 * 1000;

    private final Random random = new Random();

    private ImageButton gaidel;
    private ImageView svaston;

    private TextView countOfClicksLabel;
    private TextView speedLabel;

    private SlidingDrawer slidingDrawer;

    private ImageView goldCookie;
    private ObjectAnimator goldCookieAlphaAnimator;

    private RecyclerView recyclerView;
    private BuildingsAdapter adapter;

    private Handler handler;

    private boolean isGoldMode;
    private double goldCoefficient = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new UpdateHandler(this);

        initViews();
        setupViewPager(
                (TabLayout) findViewById(R.id.tab_layout),
                new Pair<>((Fragment) new BuildingsFragment(), "Здания"),
                new Pair<>((Fragment) new AchievementsFragment(), "Ачивки")
        );
        initRecycler();
    }

    private void initViews() {
        gaidel = (ImageButton) findViewById(R.id.buttonGaidel);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        svaston = (ImageView) findViewById(R.id.svaston);

        countOfClicksLabel = (TextView) findViewById(R.id.clicks);
        speedLabel = (TextView) findViewById(R.id.speed);

        slidingDrawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);
        slidingDrawer.setDragView(findViewById(R.id.tab_layout));

        goldCookie = (ImageView) findViewById(R.id.gold_cookie);

        ObjectAnimator anim = ObjectAnimator.ofFloat(svaston, View.ROTATION, 0f, 360f);
        anim.setRepeatCount(-1);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(2000);
        anim.start();

        gaidel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalPrefs.getInstance().increaseBalance(isGoldMode ? BigDecimal.valueOf(goldCoefficient) : BigDecimal.ONE);
                countOfClicksLabel.setText(FormatUtils.formatDecimalAsInteger(GlobalPrefs.getInstance().getBalance()));
            }
        });
        gaidel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        gaidel.setScaleX(1f);
                        gaidel.setScaleY(1f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        gaidel.setScaleX(0.99f);
                        gaidel.setScaleY(0.99f);
                        break;
                    }
                }
                return false;
            }
        });
        goldCookie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isGoldMode = true;
                hideHoldCookie();
                handler.sendEmptyMessageDelayed(UpdateHandler.EXPIRED_GOLD_COOKIE, 77 * 1000);
            }
        });
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new BuildingsAdapter();
        adapter.setOnBuildingClickListener(new OnBuildingClickListener() {
            @Override
            public void onBonusClick(Building bonus) {
                int res = GlobalPrefs.getInstance().getBalance().compareTo(bonus.getPrice());
                if (res != -1) {
                    GlobalPrefs.getInstance().increaseBalance(new BigDecimal("-" + bonus.getPrice()));
                    BuildingsRepository.getInstance().buy(bonus);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setData(BuildingsRepository.getInstance().getBuildings());
    }

    private void setupViewPager(TabLayout tabs, Pair<Fragment, String>... fragments) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);

        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stats:
                Toast toast = Toast.makeText(getApplicationContext(), "Кликов в секунду: " + BuildingsRepository.getInstance().getDeltaPerSecond(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 5);
                toast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestGoldCookieSpawn();
        handler.sendEmptyMessage(UpdateHandler.UPDATE_MESSAGE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(UpdateHandler.UPDATE_MESSAGE);
        handler.removeMessages(UpdateHandler.SPAWN_GOLD_COOKIE);
        handler.removeMessages(UpdateHandler.EXPIRED_GOLD_COOKIE);
    }

    private void hideHoldCookie() {
        goldCookie.setVisibility(View.GONE);
        if (goldCookieAlphaAnimator != null) {
            goldCookieAlphaAnimator.cancel();
            goldCookieAlphaAnimator = null;
        }
    }

    private void spawnGoldCookie() {
        goldCookieAlphaAnimator = ObjectAnimator.ofFloat(goldCookie, View.ALPHA, 1f, 0f);
        goldCookieAlphaAnimator.setDuration(10 * 1000);
        goldCookieAlphaAnimator.addListener(new AnimatorListenerAdapter() {

            private boolean isCanceled;

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                goldCookie.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!isCanceled) {
                    requestGoldCookieSpawn();
                }
            }
        });
        goldCookieAlphaAnimator.start();
        int xRange = Math.round(UIUtils.getWidth(this) - goldCookie.getWidth());
        int yRange = Math.round(UIUtils.getHeight(this) - goldCookie.getHeight());
        if (goldCookie.getWidth() == 0) {
            xRange /= 2;
            yRange /= 2;
            //TODO need fix it correct (now first time w,h equals to zero because visibility is gone)
        }
        int xCord = random.nextInt(xRange);
        int yCord = random.nextInt(yRange);
        goldCookie.setTranslationX(xCord);
        goldCookie.setTranslationY(yCord);
    }

    private void goldCookieExpired() {
        isGoldMode = false;
        hideHoldCookie();
    }

    private void requestGoldCookieSpawn() {
        handler.removeMessages(UpdateHandler.SPAWN_GOLD_COOKIE);
        handler.removeMessages(UpdateHandler.EXPIRED_GOLD_COOKIE);
        goldCookieExpired();
        int spawnDelay = MIN_GOLD_COOKIE_SPAWN_PERIOD + random.nextInt(MAX_GOLD_COOKIE_SPAWN_PERIOD - MIN_GOLD_COOKIE_SPAWN_PERIOD);
        handler.sendEmptyMessageDelayed(UpdateHandler.SPAWN_GOLD_COOKIE, spawnDelay);
    }

    private static class UpdateHandler extends Handler {

        private static final int UPDATE_MESSAGE = 0;
        private static final int SPAWN_GOLD_COOKIE = 1;
        private static final int EXPIRED_GOLD_COOKIE = 2;
        private static final int UPDATE_MESSAGE_DELAY = 200;

        WeakReference<MainActivity> mainActivityWeakReference;

        UpdateHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.what) {
                case UPDATE_MESSAGE:
                    performUpdate(mainActivity);
                    return;
                case SPAWN_GOLD_COOKIE:
                    mainActivity.spawnGoldCookie();
                    return;
                case EXPIRED_GOLD_COOKIE:
                    mainActivity.goldCookieExpired();
                    mainActivity.requestGoldCookieSpawn();
                    break;

            }
        }

        private void performUpdate(MainActivity mainActivity) {
            long previousTs = GlobalPrefs.getInstance().getLastUpdateTs();
            long currentTs = System.currentTimeMillis();
            //TODO need prevent date manipulate

            long timeDifferenceInMs = currentTs - previousTs;
            BigDecimal moneyDifference = BuildingsRepository.getInstance().getDeltaPerSecond().multiply(BigDecimal.valueOf((mainActivity.isGoldMode ? mainActivity.goldCoefficient : 1) * timeDifferenceInMs / 1000d));
            GlobalPrefs.getInstance().increaseBalance(moneyDifference);
            GlobalPrefs.getInstance().putLastUpdateTs(currentTs);

            mainActivity.countOfClicksLabel.setText(FormatUtils.formatDecimalAsInteger(GlobalPrefs.getInstance().getBalance()));
            mainActivity.speedLabel.setText(String.format(mainActivity.getText(R.string.per_second_format).toString(), FormatUtils.formatDecimal(BuildingsRepository.getInstance().getDeltaPerSecond())));

            sendEmptyMessageDelayed(UPDATE_MESSAGE, UPDATE_MESSAGE_DELAY);
        }
    }
}
