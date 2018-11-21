package hmi.hmiprojekt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Welcome extends AppCompatActivity {
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button buttonSkip, buttonNext;
    private Preference preference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        preference = Application.getApp().getPreference();
        if (!preference.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
        setContentView(R.layout.activity_welcome);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        buttonSkip = (Button) findViewById(R.id.button_skip);
        buttonNext = (Button) findViewById(R.id.button_next);

        layouts = new int[]{
                R.layout.welcome_screen1,
                R.layout.welcome_screen2,
                R.layout.welcome_screen3};
        addBottomDots(0);
        changeStatusBarColor();
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        buttonSkip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchHomeScreen();
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int current = getItem(+1);
                if (current < layouts.length) {
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_pager_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_pager_inactive);
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        preference.setFirstTimeLaunch(false);
        startActivity(new Intent(Welcome.this, MainActivity.class));
        finish();
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageSelected(int position) {
            addBottomDots(position);
            if (position == layouts.length - 1) {
                buttonNext.setText(getString(R.string.start));
                buttonSkip.setVisibility(View.GONE);
            } else {
                buttonNext.setText(getString(R.string.next));
                buttonSkip.setVisibility(View.VISIBLE);
            }
        }
        public void onPageScrolled(int arg0, float arg1, int arg2) { }
        public void onPageScrollStateChanged(int arg0) { }
    };

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        public MyViewPagerAdapter() {
        }

        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        public int getCount() {
            return layouts.length;
        }
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}