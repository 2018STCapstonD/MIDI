package com.midi.midi;

import java.util.ArrayList;

public class RecommendMusic {
    public String title;
    public String artist;
    public String album;

    public RecommendMusic(String title, String artist, String album){
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public RecommendMusic(String[] recoList){
        this.title = recoList[0];
        this.artist = recoList[1];
        this.album = recoList[2];
    }
}


