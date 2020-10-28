package com.jason.jprmusicapp.model;

import java.io.Serializable;

public class Song implements Serializable {
    private String songName;
    private String songUrl;
    private String songId;

    public Song()
    {
    }

    public Song(String songName, String songUrl,String songId) {
        this.songName = songName;
        this.songUrl = songUrl;
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }
    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }


    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
