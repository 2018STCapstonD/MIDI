package com.midi.midi;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecommendMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static class RecoViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView artist;
        public TextView album;

        RecoViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.txt_title);
            artist = view.findViewById(R.id.txt_sub_title);
            album = view.findViewById(R.id.album);
        }
    }

    public ArrayList<RecommendMusic> recoMusicArrayList;
    RecommendMusicAdapter(ArrayList<RecommendMusic> recoMusicArrayList){
        this.recoMusicArrayList = recoMusicArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_music, parent, false);

        return new RecoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecoViewHolder recoViewHolder = (RecoViewHolder) holder;

        recoViewHolder.title.setText(recoMusicArrayList.get(position).title);
        recoViewHolder.artist.setText(recoMusicArrayList.get(position).artist);
        recoViewHolder.album.setText(recoMusicArrayList.get(position).album);
    }

    @Override
    public int getItemCount() {
        return recoMusicArrayList.size();
    }

}