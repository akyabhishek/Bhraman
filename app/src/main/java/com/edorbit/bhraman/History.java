package com.edorbit.bhraman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History extends AppCompatActivity {

    DatabaseReference databaseReference;
    ArrayList<ObjectData> list=new ArrayList<>();

    ArrayList<ObjectData> newlist=new ArrayList<>();
    private ProgressBar pbar;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<HistoryData> tempHis=new ArrayList<>();
    RecyclerView recycler;
    ObjectAdapter objectAdapter;
    Button backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        pbar=findViewById(R.id.pbarHistory);
        backbtn=findViewById(R.id.backHistory);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recycler=findViewById(R.id.mainhistoryRecycler);
        recycler.setHasFixedSize(false);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL));
        objectAdapter=new ObjectAdapter(this,newlist);

        recycler.setAdapter(objectAdapter);
        databaseReference=FirebaseDatabase.getInstance().getReference("objects");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ObjectData objectData = dataSnapshot.getValue(ObjectData.class);
                        list.add(objectData);
                    }
                    Collections.reverse(list);

                    //for history objects
                    if(user!=null) {
                        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("history")
                                .orderByChild("time")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                                HistoryData historyData = dataSnapshot.getValue(HistoryData.class);
                                                tempHis.add(historyData);
                                            }
                                            Collections.reverse(tempHis);
                                            for(int i=0;i<tempHis.size();i++){
                                                for(int j=0;j<list.size();j++){
                                                    if(tempHis.get(i).getName().equals(list.get(j).getName())){
                                                        newlist.add(list.get(j));
                                                        objectAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                        pbar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}