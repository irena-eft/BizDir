package com.example.bizdir;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final String[] CATEGORIES = {"Services", "Fun", "Industry", "Education"};
    private final FragmentActivity fragmentActivity;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return CompanyListFragment.newInstance(CATEGORIES[position]);
    }

    @Override
    public int getItemCount() {
        return CATEGORIES.length;
    }

    public String getPageTitle(int position) {
        return CATEGORIES[position];
    }

    public CompanyListFragment getFragment(int position) {
        String tag = "f" + getItemId(position);
        Fragment fragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment instanceof CompanyListFragment) {
            return (CompanyListFragment) fragment;
        }
        return null;
    }
}
