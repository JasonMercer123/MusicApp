package com.jason.jprmusicapp.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jason.jprmusicapp.R;
import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;

import java.util.ArrayList;
import java.util.Random;

public class SongActivity extends AppCompatActivity {

    ArrayList<JcAudio> jcAudios =new ArrayList<>();
    JcPlayerView playerView;
    String songname,songurl;
    ImageView img;
    TextView songTitle;
    int position;
    Animation aniRotate;
    int[] back = {R.drawable.background_one,R.drawable.background_two,R.drawable.background_four,R.drawable.background_three,R.drawable.background_five};
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_song);
        playerView=findViewById(R.id.jcplayer1);
        songTitle=findViewById(R.id.song_title);

        img=findViewById(R.id.song_disc);
        aniRotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
        img.startAnimation(aniRotate);

        Bundle b=getIntent().getExtras();
        jcAudios= (ArrayList<JcAudio>) b.getSerializable("audios");

        songname=getIntent().getStringExtra("songname");
        songurl=getIntent().getStringExtra("songurl");
        position=getIntent().getIntExtra("position",0);

        playerView.initPlaylist(jcAudios,null);
        playerView.playAudio(jcAudios.get(position));
        playerView.createNotification(R.drawable.ic_library_music);
        songTitle.setText(playerView.getCurrentAudio().getTitle());

        relativeLayout=findViewById(R.id.back_song);
        relativeLayout.setBackgroundResource(back[new Random().nextInt(5)]);


        playerView.setJcPlayerManagerListener(new JcPlayerManagerListener() {
            @Override
            public void onPreparedAudio(JcStatus jcStatus) {
                songTitle.setText(playerView.getCurrentAudio().getTitle());
                img.startAnimation(aniRotate);
            }

            @Override
            public void onCompletedAudio() {

            }

            @Override
            public void onPaused(JcStatus jcStatus) {
                img.clearAnimation();
            }

            @Override
            public void onContinueAudio(JcStatus jcStatus) {
                img.setAnimation(aniRotate);
            }

            @Override
            public void onPlaying(JcStatus jcStatus) {
                songTitle.setText(playerView.getCurrentAudio().getTitle());
                img.startAnimation(aniRotate);
            }

            @Override
            public void onTimeChanged(JcStatus jcStatus) {

            }

            @Override
            public void onStopped(JcStatus jcStatus) {
                img.clearAnimation();

            }

            @Override
            public void onJcpError(Throwable throwable) {

            }
        });


     }
}
