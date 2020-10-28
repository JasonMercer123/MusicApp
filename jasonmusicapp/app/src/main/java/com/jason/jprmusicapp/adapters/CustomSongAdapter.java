package com.jason.jprmusicapp.adapters;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.activities.MainActivity;
import com.jason.jprmusicapp.model.Song;
import com.jason.jprmusicapp.activities.SongActivity;
import com.example.jean.jcplayer.model.JcAudio;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class CustomSongAdapter extends FirebaseRecyclerAdapter<Song, CustomSongAdapter.CustomSongViewholder> {

    private View mainContext;
    String deviceId;
    OnitemClickListener onitemClickListener;
    ArrayList<JcAudio> jcAudios =new ArrayList<>();
    DatabaseReference dbRef;
    StorageReference mRef;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CustomSongAdapter(@NonNull FirebaseRecyclerOptions<Song> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final CustomSongViewholder holder, final int position, @NonNull final Song model) {

        jcAudios.add(position,JcAudio.createFromURL(model.getSongName(),model.getSongUrl()));
        holder.songName.setText(model.getSongName());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onitemClickListener!=null)
                {
                    onitemClickListener.onItemClick(jcAudios,position);

                }
                Intent i=new Intent(mainContext.getContext(), SongActivity.class);
                Bundle b =new Bundle();
                b.putSerializable("audios",jcAudios);
                i.putExtra("songurl",model.getSongUrl());
                i.putExtra("songname",model.getSongName());
                i.putExtra("position",position);
                i.putExtras(b);

                mainContext.getContext().startActivity(i);
            }
        });

        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                CharSequence options[]=new CharSequence[]{
                        "Download Song" ,"Delete"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Changes:");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0)
                        {
                            File f = new File(Environment.DIRECTORY_DOWNLOADS,"JasonSongs" );
                            if (!f.exists()) {
                                f.mkdirs();
                            }
                            Toast.makeText(mainContext.getContext(), "Downloading started...", Toast.LENGTH_SHORT).show();
                            DownloadManager downloadManager = (DownloadManager) mainContext.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request =new DownloadManager.Request(Uri.parse(model.getSongUrl()))
                                    .setTitle("File Download")
                                    .setDescription(model.getSongName())
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setAllowedOverRoaming(true)
                                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                                    .setAllowedOverMetered(true)
                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/JasonSongs/"+model.getSongName());
                            downloadManager.enqueue(request);

                            Toast.makeText(mainContext.getContext(), "Your download will appear in  Internal Storage -> Downloads -> JasonSongs, reload to see the changes", Toast.LENGTH_SHORT).show();

                        }
                        else if (i==1)
                        {
                            mRef.child(model.getSongName()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dbRef.child(model.getSongId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(mainContext.getContext(), "Removed successfully", Toast.LENGTH_SHORT).show();
                                            mainContext.getContext().startActivity(new Intent(mainContext.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    public interface OnitemClickListener
    {
        void onItemClick(ArrayList<JcAudio> jcAudio1, int position);
    }


    public void setOnitemClickListener(OnitemClickListener onitemClickListener)
    {
        this.onitemClickListener=onitemClickListener;
    }


    @NonNull
    @Override
    public CustomSongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_image, parent, false);

        return new CustomSongViewholder(view);
    }
    class CustomSongViewholder extends RecyclerView.ViewHolder{

        TextView songName;
        LinearLayout linearLayout;

        public CustomSongViewholder(@NonNull View itemView) {
            super(itemView);
            mainContext=itemView;
            deviceId=Settings.Secure.getString(mainContext.getContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            dbRef= FirebaseDatabase.getInstance().getReference().child("Songs").child(deviceId);
            mRef = FirebaseStorage.getInstance().getReference().child("Songs").child(deviceId);
            songName=itemView.findViewById(R.id.song_name);
            linearLayout=itemView.findViewById(R.id.linear_view);
          }
        }
   }