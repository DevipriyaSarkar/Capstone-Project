package com.friendmatch_frontend.friendmatch.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.ViewPagerAdapter;
import com.friendmatch_frontend.friendmatch.fragments.AllEventFragment;
import com.friendmatch_frontend.friendmatch.fragments.UserEventFragment;

public class EventActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private String[] pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pageTitle = getResources().getStringArray(R.array.event_page_title);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_event);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_event);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UserEventFragment(), pageTitle[0]);
        //noinspection ResourceType
        adapter.addFragment(new AllEventFragment(), pageTitle[1]);
        viewPager.setAdapter(adapter);
    }
}

