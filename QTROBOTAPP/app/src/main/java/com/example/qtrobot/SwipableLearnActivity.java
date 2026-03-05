package com.example.qtrobot;

import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;

public class SwipableLearnActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipable_learn);

        ViewPager2 viewPager = findViewById(R.id.learn_view_pager);
        if (viewPager != null) {
            LearnPagerAdapter adapter = new LearnPagerAdapter(this);
            viewPager.setAdapter(adapter);

            int startPosition = getIntent().getIntExtra("extra_start_position", 0);
            viewPager.setCurrentItem(startPosition, false);
        }
    }
}
