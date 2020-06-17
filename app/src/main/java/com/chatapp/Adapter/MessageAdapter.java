package com.chatapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatapp.ImageViewActivity;
import com.chatapp.R;
import com.chatapp.database.Chat;
import com.chatapp.database.Chatlist;
import com.chatapp.database.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContex;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContex, List<Chat> mChat, String imageurl) {
        this.mChat = mChat;
        this.mContex = mContex;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContex).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(mContex).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Chat chat = mChat.get(position);
        if(chat.getMessage().startsWith("https://firebasestorage.googleapis.com/v0/b/chat-app")) {
            Glide.with(mContex).load(chat.getMessage()).into(holder.message_image);
            holder.img_time.setText(chat.getTime());
            holder.msg_time.setText(chat.getTime());
            holder.message_image.setVisibility(View.VISIBLE);
            holder.msg_ly.setVisibility(View.GONE);
        }
        else {
            holder.msg_time.setText(chat.getTime());
            holder.show_message.setText(chat.getMessage());
            holder.img_ly.setVisibility(View.GONE);
        }

        Glide.with(mContex).load(imageurl).into(holder.profile_image);

        if(position == mChat.size()-1) {
            if(chat.isIsseen()) {
                if(chat.getMessage().startsWith("https://firebasestorage.googleapis.com/v0/b/chat-app")) {
                    holder.image_seen.setText("seen");
                }
                else {
                    holder.txt_seen.setText("seen");
                }
            }
            else {
                if(chat.getMessage().startsWith("https://firebasestorage.googleapis.com/v0/b/chat-app")) {
                    holder.image_seen.setText("sent");
                }
                else {
                    holder.txt_seen.setText("sent");
                }
            }
        }
        else {
            holder.image_seen.setVisibility(View.GONE);
            holder.txt_seen.setVisibility(View.GONE);
        }

        //open image click
        holder.message_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                intent.putExtra("url", chat.getMessage());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        //option menu when msg long click"
        if(mChat.get(position).getSender().equals(fuser.getUid())) {
            holder.msg_ly.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence option[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Cancle",
                                    "Delete for Everyone"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                    builder.setTitle("Delete Message?");

                    builder.setItems(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()).child(chat.getReceiver()).child(chat.getMsgid());
                                reference.removeValue();
                            }
                            else if(which == 2) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()).child(chat.getReceiver()).child(chat.getMsgid());
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats").child(chat.getReceiver()).child(chat.getSender()).child(chat.getMsgid());
                                reference.removeValue();
                                reference1.removeValue();
                            }
                        }
                    });
                    builder.show();

                    return false;
                }
            });

            holder.message_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence option[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Cancle",
                                    "Delete for Everyone"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                    builder.setTitle("Delete Image?");

                    builder.setItems(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()).child(chat.getReceiver()).child(chat.getMsgid());
                                reference.removeValue();
                            }
                            else if(which == 2) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()).child(chat.getReceiver()).child(chat.getMsgid());
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats").child(chat.getReceiver()).child(chat.getSender()).child(chat.getMsgid());
                                reference.removeValue();
                                reference1.removeValue();
                            }
                        }
                    });
                    builder.show();

                    return false;
                }
            });
        }
        else {

            holder.msg_ly.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence option[] = new CharSequence[]
                            {
                                    "Delete",
                                    "Cancle"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                    builder.setTitle("Delete Message?");

                    builder.setItems(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0) {
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats").child(chat.getReceiver()).child(chat.getSender()).child(chat.getMsgid());
                                reference1.removeValue();
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });

            holder.message_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence option[] = new CharSequence[]
                            {
                                    "Delete",
                                    "Cancle"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                    builder.setTitle("Delete Image?");

                    builder.setItems(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0) {
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats").child(chat.getReceiver()).child(chat.getSender()).child(chat.getMsgid());
                                reference1.removeValue();
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public LinearLayout msg_ly, img_ly;
        public ImageView profile_image;
        public TextView txt_seen, image_seen, msg_time, img_time;
        public ImageView message_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);

            profile_image = itemView.findViewById(R.id.profile_image);
            message_image = itemView.findViewById(R.id.show_image);

            txt_seen = itemView.findViewById(R.id.txt_seen);
            image_seen = itemView.findViewById(R.id.image_seen);

            msg_time = itemView.findViewById(R.id.msg_time);
            img_time = itemView.findViewById(R.id.img_time);

            msg_ly = itemView.findViewById(R.id.msg_layout);
            img_ly = itemView.findViewById(R.id.img_layout);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
}
