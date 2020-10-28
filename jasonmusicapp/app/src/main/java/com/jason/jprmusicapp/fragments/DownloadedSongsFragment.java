package com.jason.jprmusicapp.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.adapters.CustomDownloadAdapter;
import com.jason.jprmusicapp.model.AudioSong;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;

import java.util.ArrayList;

public class DownloadedSongsFragment extends Fragment {

    CustomDownloadAdapter customDownloadAdapter;
    ArrayList<JcAudio> jcAudios;
    JcPlayerView playerView;
    RecyclerView recyclerView;
    LinearLayout notask;

    public static final int MY_PERMISSSION_REQUEST = 1;
    ArrayList<AudioSong> audioSongs;

    public DownloadedSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloaded_songs, container, false);
        playerView=view.findViewById(R.id.jcplayer);
        recyclerView=view.findViewById(R.id.recycler_downloaded);
        notask=view.findViewById(R.id.notask);

        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSSION_REQUEST);
            }
            else
            {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSSION_REQUEST);
            }
        }
        else
        {
            doStuff();
        }

        return view;
    }

    private void doStuff() {
        audioSongs=new ArrayList<>();
        jcAudios=new ArrayList<>();
        getMusic();
        customDownloadAdapter = new CustomDownloadAdapter(getActivity(),audioSongs);
        customDownloadAdapter.setOnitemClickListener(new CustomDownloadAdapter.OnitemClickListener() {
            @Override
            public void onItemClick(int position) {
                playerView.setVisibility(View.VISIBLE);
                playerView.initPlaylist(jcAudios,null);
                playerView.playAudio(jcAudios.get(position));
            }
        });


        recyclerView.setAdapter(customDownloadAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
    }

    public void getMusic()
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = getActivity().getContentResolver().query(songUri, null, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"%JasonSongs%"}, null);

        if (songCursor!=null && songCursor.moveToFirst())
        {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentSong = songCursor.getString(songData);
                notask.setVisibility(View.GONE);

                audioSongs.add(new AudioSong(currentTitle, currentSong));
                jcAudios.add(JcAudio.createFromFilePath(currentTitle,currentSong));

            }while (songCursor.moveToNext());

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerView.pause();
    }

}