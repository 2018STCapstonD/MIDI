package com.midi.midi;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecommendMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView artist;
        private TextView album;

        MyViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            album = view.findViewById(R.id.album);
        }
    }

    private ArrayList<RecommendMusic> recoMusicArrayList;
    RecommendMusicAdapter(ArrayList<RecommendMusic> recoMusicArrayList){
        this.recoMusicArrayList = recoMusicArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_music, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.title.setText(recoMusicArrayList.get(position).title);
        myViewHolder.artist.setText(recoMusicArrayList.get(position).artist);
        myViewHolder.album.setText(recoMusicArrayList.get(position).album);
    }

    @Override
    public int getItemCount() {
        return recoMusicArrayList.size();
    }

}