package com.youngju.csefinal2;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity{
    FragmentPagerAdapter adapterViewPager;
    public static Context mContext;
    ViewPager vpPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vpPager = (ViewPager)findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        vpPager.setCurrentItem(1);


        mContext = this;
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEM = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HelpActivity.newInstance();
                case 1:
                    return MapActivity.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEM;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int pos = 3;
        vpPager.postDelayed(new Runnable() {

            @Override
            public void run() {
                vpPager.setCurrentItem(pos);
            }
        }, 100);
    }
}
