package com.example.qtrobot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class SwipableLearnActivity extends AppCompatActivity {

    public static final String EXTRA_START_POSITION = "extra_start_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipable_learn);

        ViewPager2 viewPager = findViewById(R.id.learn_view_pager);
        LearnPagerAdapter adapter = new LearnPagerAdapter(this);
        viewPager.setAdapter(adapter);

        int startPosition = getIntent().getIntExtra(EXTRA_START_POSITION, 0);
        viewPager.setCurrentItem(startPosition, false);
    }
}
