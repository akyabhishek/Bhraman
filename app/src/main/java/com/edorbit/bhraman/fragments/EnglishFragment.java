package com.edorbit.bhraman.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.edorbit.bhraman.R;

public class EnglishFragment extends Fragment {


    private TextView t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_english, container, false);

        String txt = getArguments().getString("en");
        t=view.findViewById(R.id.engText);
        t.setText(txt);

        return view;

    }
}