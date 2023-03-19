package com.edorbit.bhraman;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Description extends AppCompatActivity {


    TextView title;
    Button viewInAr,backbtn;
    private DatabaseReference databaseReference;
    FirebaseUser user;
    ObjectData objectData;
    ImageView thumbView;
    ProgressBar imgPbar;
    private ViewPager2 Pager2;
    private TabLayout tabLayout;
    private DesFragmentAdapter adapter;
    String en,hi,lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description_activity);

        backbtn=findViewById(R.id.backdesc);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Pager2=findViewById(R.id.pagerview2);
        tabLayout=findViewById(R.id.tablayout);
        user= FirebaseAuth.getInstance().getCurrentUser();

        title=findViewById(R.id.titledesc);
        viewInAr=findViewById(R.id.viewInAr);
        thumbView=findViewById(R.id.thumbView);
        imgPbar=findViewById(R.id.imgPbar);
        databaseReference = FirebaseDatabase.getInstance().getReference("objects");
        databaseReference.child(getIntent().getStringExtra("object").toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            objectData = snapshot.getValue(ObjectData.class);
                            title.setText(objectData.getName());

                            Glide.with(Description.this)
                                    .load(objectData.getThumb()).listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            imgPbar.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(thumbView);

                        }else{
                            Toast.makeText(Description.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        en=getIntent().getStringExtra("en");

        hi=getIntent().getStringExtra("hi");
        FragmentManager fn= getSupportFragmentManager();
        adapter=new DesFragmentAdapter(fn,getLifecycle(),en,hi);
        Pager2.setAdapter(adapter);
        tabLayout.addTab(tabLayout.newTab().setText("English"));
        tabLayout.addTab(tabLayout.newTab().setText("Hindi"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Pager2.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        viewInAr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child(getIntent().getStringExtra("object").toLowerCase()).child("views")
                        .setValue(objectData.getViews()+1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(Description.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                if(user==null){
                    lang=objectData.getEnVoice();
                    startAr();
                }
                else{
                    //language setting code
                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("language")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        if(snapshot.getValue().equals("Hindi")){
                                            lang= objectData.getHiVoice();
                                        }
                                        else{
                                            lang= objectData.getEnVoice();
                                        }
                                    }
                                    else{
                                        lang= objectData.getEnVoice();
                                    }
                                    startAr();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    //history code
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    long dateandtime = Long.parseLong(sdf.format(new Date()));
                    HistoryData historyData=new HistoryData(objectData.getName(), dateandtime);

                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                            .child("history").child(objectData.getName().toLowerCase())
                            .setValue(historyData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Description.this, "You are viewing " + objectData.getName(), Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(Description.this, "Your history is not updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }
    private void startAr() {
        Intent sceneViewerIntent = new Intent(Intent.ACTION_VIEW);
        Uri intentUri =
                Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                        .appendQueryParameter("file", objectData.getObjLink())
                        .appendQueryParameter("title", objectData.getName())
                        .appendQueryParameter("sound", lang)
                        .appendQueryParameter("mode", "ar_preferred")
                        .build();
        sceneViewerIntent.setData(intentUri);
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox");
        sceneViewerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sceneViewerIntent);

    }
}