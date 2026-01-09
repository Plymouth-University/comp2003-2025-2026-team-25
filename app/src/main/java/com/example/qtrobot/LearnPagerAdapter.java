package com.example.qtrobot;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LearnPagerAdapter extends FragmentStateAdapter {

    public LearnPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ArrivalFragment();
            case 1: return new BeforeApptFragment();
            case 2: return new DuringApptFragment();
            case 3: return new AfterApptFragment();
            default: return new ArrivalFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
