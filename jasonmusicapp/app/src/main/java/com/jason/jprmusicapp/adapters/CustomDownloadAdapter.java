package com.jason.jprmusicapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.model.AudioSong;

import java.util.ArrayList;

public class CustomDownloadAdapter extends RecyclerView.Adapter<CustomDownloadAdapter.CustomDownloadViewHolder> {

    Context mContext;
    ArrayList<AudioSong> maudioSongs;
    OnitemClickListener onitemClickListener;

    public CustomDownloadAdapter(Context context, ArrayList<AudioSong> audiosongs) {
        mContext = context;
        maudioSongs = audiosongs;
    }

    @NonNull
    @Override
    public CustomDownloadAdapter.CustomDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_image, parent, false);
        CustomDownloadViewHolder viewHolder = new CustomDownloadViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomDownloadAdapter.CustomDownloadViewHolder holder, final int position) {

        holder.sName.setText(maudioSongs.get(position).getSongTitle());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onitemClickListener!=null)
                {
                    onitemClickListener.onItemClick(position);

                }
            }
        });

    }

    public interface OnitemClickListener
    {
        void onItemClick(int position);
    }


    public void setOnitemClickListener(OnitemClickListener onitemClickListener)
    {
        this.onitemClickListener=onitemClickListener;
    }



    @Override
    public int getItemCount() {
        return maudioSongs.size();
    }

    public class CustomDownloadViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView sName;

        public CustomDownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            sName=itemView.findViewById(R.id.song_name);
            linearLayout=itemView.findViewById(R.id.linear_view);
        }
    }
}
