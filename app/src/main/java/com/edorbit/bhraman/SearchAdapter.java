package com.edorbit.bhraman;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder>  {

    Context context;
    ArrayList<ObjectData> list;

    public SearchAdapter(Context context, ArrayList<ObjectData> list){
        this.context=context;
        this.list=list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_holder,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.id.setText(list.get(holder.getAdapterPosition()).getName());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Description.class);
                intent.putExtra("object",list.get(holder.getAdapterPosition()).getName());
                intent.putExtra("en",list.get(holder.getAdapterPosition()).getEnText());
                intent.putExtra("hi",list.get(holder.getAdapterPosition()).getHiText());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



        class MyViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        MaterialCardView card;
            public MyViewHolder (@NonNull View itemView){
                super(itemView);
                id=itemView.findViewById(R.id.objId);
                card=itemView.findViewById(R.id.cardObj);
            }
        }
}
