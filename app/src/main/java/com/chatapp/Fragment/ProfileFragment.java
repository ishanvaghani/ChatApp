package com.chatapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatapp.EditProfileActivity;
import com.chatapp.ImageViewActivity;
import com.chatapp.MainActivity;
import com.chatapp.R;

import com.chatapp.database.UserData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username, user_status;

    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser fuser;
    UserData user;

    Button btn_logout, btn_edit;

    ValueEventListener valListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid());

        image_profile = view.findViewById(R.id.user_profile_image);
        username = view.findViewById(R.id.username);
        user_status = view.findViewById(R.id.status);
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_edit = view.findViewById(R.id.edit_profile);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String svaetime, savedate, main;
                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM yyyy");
                savedate = currentDate.format(calendar.getTime());

                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                svaetime = currentTime.format(calendar.getTime());

                main = svaetime+", "+savedate;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", main);
                reference.updateChildren(hashMap);

                auth.signOut();

                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        valListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserData.class);
                username.setText(user.getName());
                user_status.setText(user.getUserStatus());
                Glide.with(getActivity()).load(user.getImage()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra("url", user.getImage());
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        reference.removeEventListener(valListener);

    }
}
