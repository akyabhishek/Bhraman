package com.edorbit.bhraman.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.edorbit.bhraman.R;

public class HindiFragment extends Fragment {

    private TextView t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hindi, container, false);

        String txt = getArguments().getString("hi");
        t=view.findViewById(R.id.hintext);
        t.setText(txt);

        return view;
    }
}