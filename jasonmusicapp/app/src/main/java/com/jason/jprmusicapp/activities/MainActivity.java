package com.jason.jprmusicapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.model.Song;
import com.jason.jprmusicapp.adapters.TabViewPagerAdapter;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    Toolbar mToolbar;
    private boolean checkPermission = false;
    private Uri uri;
    String songName,songUrl;
    String deviceId;
    ViewPager myViewPager;
    TabLayout tabLayout;
    private TabViewPagerAdapter myTabViewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        validatePermission();
        /*Here now will setup the ViewPagerAdapter over ViewPager*/
        mToolbar=findViewById(R.id.main_page_toolbar);
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(), 0);
        myViewPager.setAdapter(myTabViewPagerAdapter);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(myViewPager);

        tabLayout.getTabAt(0).setText("Playlist");
        tabLayout.getTabAt(1).setText("Downloaded");

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_playlist);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_downloaded);

        tabLayout.getTabAt(0).getIcon().setColorFilter(Color.parseColor("#8FE692"), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(Color.parseColor("#8FE692"), PorterDuff.Mode.SRC_IN);

        deviceId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.parseColor("#8FE692"), PorterDuff.Mode.SRC_IN);
                tabLayout.setTabTextColors(Color.parseColor("#ffffff"), Color.parseColor("#8FE692"));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.parseColor("#8FE692"), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.nav_upload)
        {

            startActivity(new Intent(MainActivity.this,UploadSongActivity.class));
            Animatoo.animateCard(this);
        }

        if (item.getItemId()==R.id.nav_upload_single)
        {
            pickSong();
        }

        if (item.getItemId()==R.id.refresh_layout)
        {
            startActivity(new Intent(MainActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
            Animatoo.animateWindmill(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private void pickSong() {
        Intent pickSongIntent = new Intent();
        pickSongIntent.setType("audio/*");
        pickSongIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(pickSongIntent,1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                uri=data.getData();
                Cursor mcursor = getApplicationContext().getContentResolver().query(uri,null,null,null,null);
                int indexedname = mcursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                mcursor.moveToFirst();
                songName = mcursor.getString(indexedname);
                mcursor.close();


                uploadSongToFirebaseStorage();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadSongToFirebaseStorage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Songs").child(deviceId).child(songName);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlSongs = uriTask.getResult();
                songUrl =urlSongs.toString();

                uploadDetailsToDatabase();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0* taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                int currentProgress = (int)progress;
                progressDialog.setMessage("Uploaded : "+ currentProgress+"%");
            }
        });
    }


    private void uploadDetailsToDatabase() {
        String songId=""+songName;
        songId=songName.replaceAll("\\W+","");
        Song songObj =new Song(songName,songUrl,songId);
        FirebaseDatabase.getInstance().getReference("Songs").child(deviceId).child(songId).setValue(songObj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Song Successfully uploaded", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                    Animatoo.animateShrink(MainActivity.this);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validatePermission()
    {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                checkPermission=true;
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token)
            {
                token.continuePermissionRequest();
            }
        }).check();

        return checkPermission;

    }
}
