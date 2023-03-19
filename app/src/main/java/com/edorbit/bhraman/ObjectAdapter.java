package com.edorbit.bhraman;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Date;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.MyViewHolder>{
    Context context;
    ArrayList<ObjectData> list;

    public ObjectAdapter(Context context, ArrayList<ObjectData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.object_card,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ObjectData objectData=list.get(position);
        holder.title.setText(objectData.getName());
        holder.viewscount.setText(objectData.getViews().toString()+" Views");
        Glide.with(holder.thumbView.getContext())
                .load(objectData.getThumb()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.pbar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.thumbView);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Description.class);
                intent.putExtra("object",objectData.getName());
                intent.putExtra("en",objectData.getEnText());
                intent.putExtra("hi",objectData.getHiText());
                context.startActivity(intent);
            }
        });
//        holder.viewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(context, "Clicked On Button", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title,viewscount;
        Button viewButton;
        CardView card;
        ImageView thumbView;
        ProgressBar pbar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card=itemView.findViewById(R.id.card);
            title=itemView.findViewById(R.id.title);
            viewscount=itemView.findViewById(R.id.viewscount);
            thumbView=itemView.findViewById(R.id.thumb);
//            viewButton=itemView.findViewById(R.id.viewButton);
            pbar=itemView.findViewById(R.id.pbarforHome);
        }
    }
}
