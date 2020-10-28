package com.jason.jprmusicapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.jason.jprmusicapp.R;
import com.jason.jprmusicapp.adapters.UploadListAdapter;
import com.jason.jprmusicapp.model.Song;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class UploadSongActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_SONG = 1;
    private Button mSelectBtn;
    private RecyclerView mUploadList;

    private List<String> fileNameList;
    private List<String> fileDoneList;
    public static int  backPressed=1;

    private UploadListAdapter uploadListAdapter;

    private StorageReference mStorage;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_upload_song);

        deviceId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        mStorage = FirebaseStorage.getInstance().getReference();

        mSelectBtn = findViewById(R.id.select_btn);
        mUploadList = (RecyclerView) findViewById(R.id.upload_list);

        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();

        uploadListAdapter = new UploadListAdapter(fileNameList, fileDoneList);

        //RecyclerView

        mUploadList.setLayoutManager(new LinearLayoutManager(this));
        mUploadList.setHasFixedSize(true);
        mUploadList.setAdapter(uploadListAdapter);


        mSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), RESULT_LOAD_SONG);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RESULT_LOAD_SONG && resultCode == RESULT_OK){

            if(data.getClipData() != null){

                Toast.makeText(this, "Please don't press back until all songs get uploaded", Toast.LENGTH_SHORT).show();

                int totalItemsSelected = data.getClipData().getItemCount();

                for(int i = 0; i < totalItemsSelected; i++){

                    Uri fileUri = data.getClipData().getItemAt(i).getUri();

                    final String fileName = getFileName(fileUri);

                    fileNameList.add(fileName);
                    fileDoneList.add("uploading");
                    uploadListAdapter.notifyDataSetChanged();

                    StorageReference fileToUpload = mStorage.child("Songs").child(deviceId).child(fileName);

                    final int finalI = i;
                    fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                            String songUrl;
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isComplete());
                            Uri urlSongs = uriTask.getResult();
                            songUrl =urlSongs.toString();

                            uploadDetailsToDatabase(fileName,songUrl);

                            fileDoneList.remove(finalI);
                            fileDoneList.add(finalI, "done");
                            uploadListAdapter.notifyDataSetChanged();
                            
                        }
                    });

                }
                //Toast.makeText(MainActivity.this, "Selected Multiple Files", Toast.LENGTH_SHORT).show();

            } else if (data.getData() != null){

                Toast.makeText(this, "please Select Multiple Files", Toast.LENGTH_SHORT).show();
            }
        }

    }



    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadDetailsToDatabase(String songName,String songUrl) {

        String songId =""+songName;
        songId=songName.replaceAll("\\W+","");
        Song songObj =new Song(songName,songUrl,songId);
        FirebaseDatabase.getInstance().getReference("Songs").child(deviceId).child(songId).setValue(songObj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(UploadSongActivity.this, "Song Successfully uploaded", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadSongActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(UploadSongActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
        Animatoo.animateShrink(UploadSongActivity.this);
    }
}