package com.example.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    Button upload;
    Uri imageUri;
    Button move;
    ImageView image;
    private static final int iPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image= findViewById(R.id.display);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Images/");
        ref = database.getReference("uploads");
        upload = findViewById(R.id.btnUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, iPick);
            }
        });
        move= findViewById(R.id.moveScreen);
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,show.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == iPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            StorageReference imgRef = mStorageRef.child( imageUri.getLastPathSegment());
            imgRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(MainActivity.this,"Uploaded",Toast.LENGTH_LONG).show();
//                            UploadData upload = new UploadData(imageUri.getLastPathSegment(),taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
//                            String uploadid = ref.push().getKey();
//                            ref.child(uploadid).setValue(upload);
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                            UploadData upload = new UploadData(imageUri.getLastPathSegment(),downloadUrl.toString());
                            String uploadId = ref.push().getKey();
                            ref.child(uploadId).setValue(upload);
                            Glide.with(MainActivity.this).load(imageUri).into(image);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }
}