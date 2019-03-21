package com.qunar.qapm.demo.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qunar.qapm.demo.R;
import com.qunar.qapm.demo.utils.StatusBarUtils;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mContentViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHomeActivityView();
    }

    private void initHomeActivityView() {
        setContentView(R.layout.activity_home);
        StatusBarUtils.setWindowStatusBarColor(this, android.R.color.holo_green_dark);
        mContentViewPager = findViewById(R.id.contentPanel);
        mContentViewPager.setAdapter(new ContentViewPagerAdapter(this.getSupportFragmentManager()));
        mContentViewPager.addOnPageChangeListener(new ContentViewPagerListener());
    }
}

class ContentViewPagerListener implements ViewPager.OnPageChangeListener {

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}

class ContentViewPagerAdapter extends FragmentPagerAdapter {

    public ContentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
