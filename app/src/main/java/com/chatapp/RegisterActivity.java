package com.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chatapp.database.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class RegisterActivity extends AppCompatActivity {

    CircleImageView imageView;
    EditText textemail, textpassword, textusername;
    Button signup;

    FirebaseAuth firebaseAuth;
    DatabaseReference database;
    Uri imgUri;
    Uri filepath;
    StorageReference storageReference;
    StorageReference reference;

    byte[] finalImage;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = findViewById(R.id.circleimage);
        textemail = findViewById(R.id.email);
        textpassword = findViewById(R.id.password);
        textusername = findViewById(R.id.username);
        signup = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN );

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Profiles");
        database = FirebaseDatabase.getInstance().getReference("Users");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for storage permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                     != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else {
                        cropImagePickr();
                    }
                }
                else {
                    cropImagePickr();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                    String email = textemail.getText().toString().trim();
                    String password = textpassword.getText().toString().trim();
                    String name = textusername.getText().toString().trim();

                    if(email.length() == 0) {
                        textemail.setError("Please Enter Email");
                    }
                    if(password.length() == 0) {
                        textpassword.setError("Please Enter Password");
                    }
                    if(name.length() == 0) {
                        textusername.setError("Please Enter Name");
                    }
                    else if(password.length()<6) {
                        textpassword.setError("Password is too weak");
                    }
                    else {
                        uploadimage(name, email, password);
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cropImagePickr() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(RegisterActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                filepath = result.getUri();
                imageView.setImageURI(filepath);

                if(filepath != null) {

                    File actualfile = new File(filepath.getPath());
                    try {
                        Bitmap compressedImage = new Compressor(this)
                                .setQuality(30)
                                .compressToBitmap(actualfile);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        compressedImage.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                        finalImage = byteArrayOutputStream.toByteArray();
                    } catch (Exception e) {
                        Log.d("", "" + e);
                    }
                }
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Toast.makeText(this, ""+exception, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadimage(final String name1, final String email1, final String password1) {

        progressBar.setAlpha(1f);
        signup.setAlpha(0f);
        textemail.setEnabled(false);
        textpassword.setEnabled(false);
        textusername.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email1, password1)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                                final String id = firebaseAuth.getCurrentUser().getUid();

                                reference = storageReference.child("images/" + id);
                                UploadTask uploadTask = reference.putBytes(finalImage);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imgUri = uri;

                                                        setProfile(name1, email1, password1, id, ""+imgUri);

                                                    }
                                                });
                                            }
                                        });
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                finish();

                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                            progressBar.setAlpha(0f);
                            signup.setAlpha(1f);
                            textemail.setEnabled(true);
                            textpassword.setEnabled(true);
                            textusername.setEnabled(true);

                        }
                    }
                });
    }

    private void setProfile(String name2, String email2, String pass2, String id, String goturi) {
        String status = "online";
        User user = new User(name2, pass2, email2, id,goturi, status, "hey, I am using ChatApp", false, name2.toLowerCase(), "noOne");
        database.child(firebaseAuth.getCurrentUser().getUid().toString()).setValue(user);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = firebaseAuth.getCurrentUser();
        if(currentuser != null){
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
        }
    }
}