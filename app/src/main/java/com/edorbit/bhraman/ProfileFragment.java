package com.edorbit.bhraman;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    Button btn_logout;
    TextView name,email;
    FirebaseUser user;
    private String userid;
    ProgressBar pbar;
    LinearLayout lr;
    MaterialCardView settings,history;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_profile, container, false);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            getActivity().finish();
            startActivity(new Intent(getActivity(), Login.class));
        }

        name=view.findViewById(R.id.profile_name);
        email=view.findViewById(R.id.profile_email);
        pbar=view.findViewById(R.id.prof_pro);
        lr=view.findViewById(R.id.prof);
        settings=view.findViewById(R.id.settings);
        history=view.findViewById(R.id.historycard);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),Setting.class));
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),History.class));
            }
        });

        user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            userid=user.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserHelperClass usr=snapshot.getValue(UserHelperClass.class);

                    if(usr!=null){
                        name.setText(usr.getName());
                        email.setText(usr.getEmail());
                        lr.setVisibility(View.VISIBLE);
                        pbar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        btn_logout=view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), Login.class));
                getActivity().finish();

            }
        });
        return view;



    }
}