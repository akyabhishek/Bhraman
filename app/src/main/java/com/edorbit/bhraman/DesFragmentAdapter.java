package com.edorbit.bhraman;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.edorbit.bhraman.fragments.EnglishFragment;
import com.edorbit.bhraman.fragments.HindiFragment;

public class DesFragmentAdapter extends FragmentStateAdapter {
    String en,hi;


    public DesFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,@NonNull String en,@NonNull String hi) {
        super(fragmentManager, lifecycle);
        this.en=en;
        this.hi=hi;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString("en", en);
        bundle.putString("hi",hi);

        switch (position) {
            case 1:
                fragment = new HindiFragment();
                fragment.setArguments(bundle);
                break;
            default:
                fragment = new EnglishFragment();
                fragment.setArguments(bundle);
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
