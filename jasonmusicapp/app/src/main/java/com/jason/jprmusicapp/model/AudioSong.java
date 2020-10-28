package com.jason.jprmusicapp.model;

public class AudioSong {
    String songTitle;
    String songData;

    public AudioSong(String songTitle, String songData) {
        this.songTitle = songTitle;
        this.songData = songData;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongData() {
        return songData;
    }

    public void setSongData(String songData) {
        this.songData = songData;
    }



}
