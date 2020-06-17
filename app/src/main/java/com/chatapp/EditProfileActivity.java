package com.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatapp.database.UserData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {

    EditText edit_username, edit_status;
    Button btn_save;
    CircleImageView profile_image;
    ValueEventListener valueEventListener;
    DatabaseReference reference;
    StorageReference storageReference;
    StorageTask uploadTask;
    UserData user;

    Toolbar toolbar;

    Uri imageuri;
    byte[] finalImage;
    String finalurl;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edit_username = findViewById(R.id.edit_username);
        edit_status = findViewById(R.id.edit_status);
        btn_save = findViewById(R.id.btn_save);
        profile_image = findViewById(R.id.edit_user_profile_image);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Profiles").child("images/");

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImagePickr();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid());

        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserData.class);
                edit_username.setText(user.getName());
                edit_status.setText(user.getUserStatus());
                Glide.with(EditProfileActivity.this).load(user.getImage()).into(profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalImage != null) {
                    uploadImage();
                    finalImage.equals("");
                }
                else {
                    if(edit_username.getText().toString().trim().equals("")) {
                        edit_username.setError("Enter username");
                    }
                    if(edit_status.getText().toString().trim().equals("")) {
                        edit_status.setError("Enter bio");
                    }
                    else {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", edit_username.getText().toString().trim());
                        hashMap.put("search", edit_username.getText().toString().trim().toLowerCase());
                        hashMap.put("userStatus", edit_status.getText().toString().trim());
                        reference.updateChildren(hashMap);
                        Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void cropImagePickr() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(EditProfileActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                imageuri = result.getUri();
                profile_image.setImageURI(imageuri);

                if(imageuri != null) {

                    File actualfile = new File(imageuri.getPath());
                    try {
                        Bitmap compressedImage = new Compressor(EditProfileActivity.this)
                                .setQuality(30)
                                .compressToBitmap(actualfile);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        compressedImage.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                        finalImage = byteArrayOutputStream.toByteArray();

                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Toast.makeText(EditProfileActivity.this, ""+exception, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Updating");
        progressDialog.show();

        if(finalImage != null) {
            final StorageReference fileReference = storageReference.child(auth.getCurrentUser().getUid());
            uploadTask = fileReference.putBytes(finalImage);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()) {

                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Uri downloaduri = (Uri) task.getResult();
                        finalurl = downloaduri.toString();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", edit_username.getText().toString().trim());
                        hashMap.put("search", edit_username.getText().toString().trim().toLowerCase());
                        hashMap.put("userStatus", edit_status.getText().toString().trim());
                        hashMap.put("image", finalurl);
                        reference.updateChildren(hashMap);
                        Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
        }
    }
}