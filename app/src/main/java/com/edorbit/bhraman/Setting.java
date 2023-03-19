package com.edorbit.bhraman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Setting extends AppCompatActivity {
    private Button audioLang,backbtn;
    Spinner langSpinner;
    FirebaseUser user;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        backbtn=findViewById(R.id.backSetting);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        user=FirebaseAuth.getInstance().getCurrentUser();
        db=FirebaseDatabase.getInstance().getReference("users");
        audioLang=findViewById(R.id.audioLang);
        audioLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                MaterialAlertDialogBuilder langAlert = new MaterialAlertDialogBuilder(view.getContext());
                langAlert.setTitle("Audio Language");
                langAlert.setMessage("Select a language to set default audio Language");
                View v=li.inflate(R.layout.setting_option,null);
                langAlert.setView(v);
                langSpinner=v.findViewById(R.id.langSpinner);
                List<String> lang = new ArrayList<>();
                lang.add("Hindi");
                lang.add("English");



                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, lang);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                langSpinner.setAdapter(adapter);

                db.child(user.getUid()).child("language").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String lan=snapshot.getValue().toString();
                            if(lan.equals("Hindi")){
                                langSpinner.setSelection(0);
                            }else{
                                langSpinner.setSelection(1);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                langAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.child(user.getUid())
                                .child("language").setValue(langSpinner.getSelectedItem().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Setting.this, "Language changed to "+langSpinner.getSelectedItem().toString()+" successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(Setting.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Setting.this, "You clicked on cancel", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });
    }
}