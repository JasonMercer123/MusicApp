package com.jason.jprmusicapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jason.jprmusicapp.fragments.DownloadedSongsFragment;
import com.jason.jprmusicapp.fragments.PlaylistFragment;

public class TabViewPagerAdapter extends FragmentPagerAdapter {
    public TabViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                PlaylistFragment playlistFragment  =new PlaylistFragment();
                return playlistFragment;
            case 1:
                DownloadedSongsFragment downloaded_songs =new DownloadedSongsFragment();
                return downloaded_songs;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Playlist";
            case 1:
                return "Downloaded";
            default:
                return null;
        }
    }
}
