package com.chatapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.chatapp.MessageActivity;
import com.chatapp.R;
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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContex;
    private List<UserData> mUsers;
    private boolean ischat;

    private String theLastMessage;
    private FirebaseUser firebaseUser;
    private boolean lastmsg_seenStatus;

    public UserAdapter(Context mContex, List<UserData> mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContex = mContex;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContex).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final UserData user = mUsers.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.username.setText(user.getName());
        Glide.with(mContex).load(user.getImage()).into(holder.profile_image);



        if(ischat) {
            lastMessage(user.getId(), holder.last_msg);

//            checkIsBlocked(userid, holder, position);

//            holder.blocked.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(user.isBlocked()) {
//                        unBlockUser(userid, position);
//                    }
//                    else {
//                        blockUser(userid, position);
//                    }
//                }
//            });

        }
        else {
            holder.last_msg.setText(user.getUserStatus());
        }

        if(ischat) {
            if(user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }
            else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContex, MessageActivity.class);
                intent.putExtra("userid", mUsers.get(position).getId());
                intent.putExtra("name", mUsers.get(position).getName());
                intent.putExtra("isBlocked", mUsers.get(position).isBlocked());
                mContex.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence option[] = new CharSequence[]
                        {
                                "Delete Chat",
                                "Cancle",
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setTitle("Chat");

                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(mUsers.get(position).getId());
                            reference.removeValue();
                        }
                    }
                });
                builder.show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        private ImageView profile_image, lastmsg_dot;
        private ImageView img_on, img_off;
        private TextView last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            lastmsg_dot = itemView.findViewById(R.id.lastmsg_dot);
        }
    }

    private void lastMessage(final String userid, final TextView last_msg) {

        theLastMessage = "default";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid()).child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if(firebaseUser.getUid().equals(chat.getReceiver()) && userid.equals(chat.getSender()) ||
                            userid.equals(chat.getReceiver()) && firebaseUser.getUid().equals(chat.getSender())) {
                        theLastMessage = chat.getMessage();
                        lastmsg_seenStatus = chat.isIsseen();
                    }
                }

                if(theLastMessage.equals("default")) {
                    last_msg.setText("");
                }
                else {
                    if(theLastMessage.startsWith("https://firebasestorage.googleapis.com/v0/b/chat-app")) {
                        last_msg.setText("Image");
                    }
                    else {
                        last_msg.setText(theLastMessage);
                    }
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
