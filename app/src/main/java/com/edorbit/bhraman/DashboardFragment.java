package com.edorbit.bhraman;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Collections;


public class DashboardFragment extends Fragment {

    RecyclerView recyclerView, histrecycler;
        private DatabaseReference databaseReference;
    ArrayList<ObjectData> list, historyList;
    ArrayList<HistoryData> tempHis = new ArrayList<>();
    ObjectAdapter objectAdapter, objectAdapter2;
    private ProgressBar pbar;
    FirebaseUser user;

    //for searchView
    DatabaseReference ref;
    ArrayList<ObjectData> searchList;
    RecyclerView recycler;
    SearchView searchView;
    TextView noObj;
    SliderView sliderView;

    String url1 = "https://firebasestorage.googleapis.com/v0/b/brahman-4ed71.appspot.com/o/slider%2F1.jpg?alt=media&token=efc338d2-6203-4a61-b5c6-ca63301b59fb";
    String url2 = "https://firebasestorage.googleapis.com/v0/b/brahman-4ed71.appspot.com/o/slider%2F2.jpg?alt=media&token=61dd30a1-f361-4822-a7df-cf8970200af1";
    String url3 = "https://firebasestorage.googleapis.com/v0/b/brahman-4ed71.appspot.com/o/slider%2F3.jpg?alt=media&token=1ce76e0c-6d26-4e98-95dd-e9bbeba20d97";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();
        sliderView = view.findViewById(R.id.slider);
        sliderDataArrayList.add(new SliderData(url1));
        sliderDataArrayList.add(new SliderData(url2));
        sliderDataArrayList.add(new SliderData(url3));

        // passing this array list inside our adapter class.
        HomeSliderAdapter adapter = new HomeSliderAdapter(getContext(), sliderDataArrayList);

        // below method is used to set auto cycle direction in left to
        // right direction you can change according to requirement.
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(adapter);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);

        sliderView.startAutoCycle();

        //for searching objects
        ref = FirebaseDatabase.getInstance().getReference().child("objects");
        recycler = view.findViewById(R.id.rv);
        searchView = view.findViewById(R.id.searchView);
        noObj = view.findViewById(R.id.noObject);

        //for top viewed objects
        pbar = view.findViewById(R.id.explorePbar);
        recyclerView = view.findViewById(R.id.exploreRecycler);
        histrecycler = view.findViewById(R.id.historyRecycler);

        databaseReference = FirebaseDatabase.getInstance().getReference("objects");
        recyclerView.setHasFixedSize(false);
        histrecycler.setHasFixedSize(false);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL));
        histrecycler.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL));
        recyclerView.suppressLayout(true);

        list = new ArrayList<>();
        historyList = new ArrayList<>();
        objectAdapter = new ObjectAdapter(getContext(), list);
        recyclerView.setAdapter(objectAdapter);
        //recycler history
        objectAdapter2 = new ObjectAdapter(getContext(), historyList);
        histrecycler.setAdapter(objectAdapter2);

        databaseReference.orderByChild("views").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ObjectData objectData = dataSnapshot.getValue(ObjectData.class);
                        list.add(objectData);
                        objectAdapter.notifyDataSetChanged();
                    }
                    Collections.reverse(list);
                    pbar.setVisibility(View.GONE);
                }
                SearchAdapter searchAdapter = new SearchAdapter(getContext(), list);
                recycler.setAdapter(searchAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        onStart();
        return view;
    }

    @Override
    //this should be protected but clashing
    public void onStart() {
        super.onStart();
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    if (newText.trim().isEmpty()) {
                        recycler.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        histrecycler.setVisibility(View.VISIBLE);
                        sliderView.setVisibility(View.VISIBLE);

                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        histrecycler.setVisibility(View.GONE);
                        sliderView.setVisibility(View.GONE);
                    }

                    return true;
                }

            });
        }
    }

    private void search(String str) {

        ArrayList<ObjectData> myList = new ArrayList<>();
        for (ObjectData obj : list) {
            if (obj.getName().toLowerCase().contains(str.toLowerCase())) {
                myList.add(obj);
            }
        }

        if (myList.size() == 0) {
            noObj.setVisibility(View.VISIBLE);
        } else {
            noObj.setVisibility(View.GONE);
        }
        Log.v("abmsg", String.valueOf(myList.size()));
        SearchAdapter searchAdapter = new SearchAdapter(getContext(), myList);
        recycler.setAdapter(searchAdapter);


    }
}