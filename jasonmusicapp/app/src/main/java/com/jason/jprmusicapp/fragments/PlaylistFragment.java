package com.jason.jprmusicapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.adapters.CustomSongAdapter;
import com.jason.jprmusicapp.model.Song;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    String deviceId;
    RecyclerView recyclerView;
    CustomSongAdapter customImagesAdapter;
    ArrayList<JcAudio> jcAudios;
    JcPlayerView playerView;
    DatabaseReference mDbRefPend;
    LinearLayout noTask;
    SpinKitView spinKitView;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_playlist, container, false);
        jcAudios =new ArrayList<JcAudio>();
        recyclerView=view.findViewById(R.id.recycler_list);
        playerView=view.findViewById(R.id.jcplayer);
        spinKitView=view.findViewById(R.id.spin_kit);
        noTask=view.findViewById(R.id.notask);

        mDbRefPend= FirebaseDatabase.getInstance().getReference().child("Songs");

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Song> options =
                new FirebaseRecyclerOptions.Builder<Song>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Songs").child(deviceId), Song.class)
                        .build();

        customImagesAdapter=new CustomSongAdapter(options);

        customImagesAdapter.setOnitemClickListener(new CustomSongAdapter.OnitemClickListener() {
            @Override
            public void onItemClick(ArrayList<JcAudio> jcAudio1,int position) {
                jcAudios=jcAudio1;
                playerView.setVisibility(View.VISIBLE);
                Log.e("List Audio : ", jcAudios.toString());
                playerView.initPlaylist(jcAudios,null);
                playerView.playAudio(jcAudios.get(position));
                playerView.setVisibility(View.VISIBLE);

            }
        });

        mDbRefPend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(deviceId))
                {
                    noTask.setVisibility(View.GONE);
                }
                else
                {
                    spinKitView.setVisibility(View.GONE);
                    noTask.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        recyclerView.setAdapter(customImagesAdapter);
        playerView.createNotification(R.drawable.ic_library_music);
        customImagesAdapter.startListening();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        customImagesAdapter.startListening();
        mDbRefPend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(deviceId))
                {
                    noTask.setVisibility(View.GONE);
                    spinKitView.setVisibility(View.GONE);
                }
                else
                {
                    spinKitView.setVisibility(View.GONE);
                    noTask.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerView.pause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        deviceId= Settings.Secure.getString(getActivity().getContentResolver(),Settings.Secure.ANDROID_ID);
    }
}