package com.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatapp.Adapter.MessageAdapter;
import com.chatapp.Adapter.UserAdapter;
import com.chatapp.Notification.APIService;
import com.chatapp.Notification.Client;
import com.chatapp.Notification.Data;
import com.chatapp.Notification.MyResponse;
import com.chatapp.Notification.Sender;
import com.chatapp.Notification.Token;
import com.chatapp.database.Chat;
import com.chatapp.database.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username, status_user;

    FirebaseUser fuser;
    DatabaseReference reference;
    StorageReference storageReference;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;
    String userid, msg, user_name;

    ValueEventListener seenListner;

    ImageButton btn_send, btn_attach;
    ImageView btn_emoji;
    EmojiconEditText text_send;
    View rootView;
    EmojIconActions emojIcon;

    APIService apiService;
    boolean notify = false;
    boolean isBlocked= false;

    Uri filepath, imgUri;
    byte[] finalImage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        rootView = findViewById(R.id.root_view);

        progressDialog = new ProgressDialog(MessageActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.user_name);
        status_user = findViewById(R.id.status);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.emojicon_edit_text);
        btn_attach = findViewById(R.id.attach);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("name");

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDetails();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDetails();
            }
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Profiles");

        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else {
                        chooseImage();
                    }
                }
                else {
                    chooseImage();
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                msg = text_send.getText().toString().trim();
                if(!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                }
                else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData user = dataSnapshot.getValue(UserData.class);

                String typingStatus = user.getTypingTo();
                if(typingStatus.equals(fuser.getUid())) {
                    status_user.setText("typing...");
                }
                else {
                    status_user.setText(user.getStatus());
                }

                username.setText(user.getName());
                Glide.with(getApplicationContext()).load(user.getImage()).into(profile_image);

                readMessages(fuser.getUid(), userid, user.getImage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //emoji keyboard
        btn_emoji = findViewById(R.id.emogi);

        if(rootView != null) {
            emojIcon = new EmojIconActions(this, rootView, text_send, btn_emoji, "#3F51B5", "#e8e8e8", "#f4f4f4");
            emojIcon.ShowEmojIcon();
            emojIcon.setIconsIds(R.drawable.ic_keyboard_black_24dp, R.drawable.ic_sentiment_satisfied_black_24dp);
            emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
                @Override
                public void onKeyboardOpen() {

                }

                @Override
                public void onKeyboardClose() {

                }
            });
        }

        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0) {
                    typingStatus("noOne");
                }
                else {
                    typingStatus(userid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        seenMessage(userid);
    }

    public void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {

            final List<Uri> uris = new ArrayList<>();
            ClipData clipData = data.getClipData();

            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);

            if (clipData != null) {
                progressDialog.setTitle("Sending multiple Images");
                progressDialog.show();

                for (int i = 0; i < clipData.getItemCount(); i++) {
                    filepath = clipData.getItemAt(i).getUri();
                    try {
                        uris.add(filepath);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            } else {
                filepath = data.getData();

                progressDialog.setTitle("Sending Image");
                progressDialog.show();

                try {
                    uris.add(filepath);
                } catch (Exception e) {
                    Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (final Uri b : uris) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

//                                File actualfile = new File(b.getPath());
//                                try {
//                                    Bitmap compressedImage = new Compressor(getApplicationContext())
//                                            .setQuality(50)
//                                            .compressToBitmap(actualfile);
//
//                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                                    compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//                                    finalImage = byteArrayOutputStream.toByteArray();
//                                    sendFile(fuser.getUid(), userid);
//                                } catch (Exception e) {
//                                    Log.d("good error", "" + e);
//                                }
                                sendFile(fuser.getUid(), userid, b);
                                uris.clear();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid).child(fuser.getUid());
        seenListner = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(fuser.getUid().equals(chat.getReceiver()) && userid.equals(chat.getSender())){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendFile(final String sender, final String receiver, Uri uri) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            final String id = UUID.randomUUID().toString();
            final String msgab = FirebaseDatabase.getInstance().getReference().push().getKey();

            final StorageReference sreference = storageReference.child("Message_images/" + id);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference finalReference = reference;

            sreference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    sreference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            imgUri = uri;
                            String imgurl = uri.toString();

                            String svaetime, savedate, main;
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM");
                            savedate = currentDate.format(calendar.getTime());
                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            svaetime = currentTime.format(calendar.getTime());
                            main = svaetime+", "+savedate;

                            Chat chat = new Chat(sender, receiver, imgurl, false, msgab, main);
                            finalReference.child("Chats").child(fuser.getUid()).child(userid).child(msgab).setValue(chat);
                            finalReference.child("Chats").child(userid).child(fuser.getUid()).child(msgab).setValue(chat);
                            progressDialog.dismiss();
                        }
                    });
                }
            });

            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist");

            HashMap<String, String> hashMap1 = new HashMap<>();
            hashMap1.put("id", userid);
            chatRef.child(fuser.getUid()).child(userid).setValue(hashMap1);

            HashMap<String, String> hashMap2 = new HashMap<>();
            hashMap2.put("id", sender);
            chatRef.child(receiver).child(sender).setValue(hashMap2);

            //notification
            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserData user = dataSnapshot.getValue(UserData.class);
                    if(notify) {
                        sendNotification(receiver, user.getName(), "Image");
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //--------------
        }
        else {
            progressDialog.dismiss();
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(final String sender, final String receiver, final String message) {

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            String msgab = FirebaseDatabase.getInstance().getReference().push().getKey();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            String svaetime, savedate, main;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM");
            savedate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            svaetime = currentTime.format(calendar.getTime());
            main = svaetime+", "+savedate;

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            hashMap.put("message", message);
            hashMap.put("isseen", false);
            hashMap.put("msgid", msgab);
            hashMap.put("time", main);

            reference.child("Chats").child(fuser.getUid()).child(userid).child(msgab).setValue(hashMap);
            reference.child("Chats").child(userid).child(fuser.getUid()).child(msgab).setValue(hashMap);

            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist");

            HashMap<String, String> hashMap1 = new HashMap<>();
            hashMap1.put("id", userid);
            chatRef.child(fuser.getUid()).child(userid).setValue(hashMap1);

            HashMap<String, String> hashMap2 = new HashMap<>();
            hashMap2.put("id", sender);
            chatRef.child(receiver).child(sender).setValue(hashMap2);

            //notification
            final String msg = message;

            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserData user = dataSnapshot.getValue(UserData.class);
                    if(notify) {
                        sendNotification(receiver, user.getName(), msg);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //--------------

        }
        else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.logo, username+": "+message, "New Message", userid);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200) {
                                        if(response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myid, final String userid, final String imageurl) {

        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(myid).child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chat chat = snapshot.getValue(Chat.class);

                    if(myid.equals(chat.getReceiver()) && userid.equals(chat.getSender()) ||
                    userid.equals(chat.getReceiver()) && myid.equals(chat.getSender())){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);

    }

    private void typingStatus(String typing) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListner);

        String svaetime, savedate, main;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM yyyy");
        savedate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        svaetime = currentTime.format(calendar.getTime());

        main = svaetime+", "+savedate;
        status(main);
        typingStatus("noOne");
        currentUser("none");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.clearChat) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()).child(userid);
            reference.removeValue();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userDetails() {
        View view = LayoutInflater.from(MessageActivity.this).inflate(R.layout.activity_user_details, null);

        final ImageView user_image = view.findViewById(R.id.image_user);
        final TextView username = view.findViewById(R.id.textView);
        final TextView status = view.findViewById(R.id.textView2);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData user = dataSnapshot.getValue(UserData.class);
                username.setText(user.getName());
                status.setText(user.getUserStatus());
                Glide.with(getApplicationContext()).load(user.getImage()).into(user_image);

                readMessages(fuser.getUid(), userid, user.getImage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        builder.create().show();
    }
}