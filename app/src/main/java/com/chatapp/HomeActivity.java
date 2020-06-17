package com.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chatapp.Adapter.ViewPage;
import com.chatapp.Fragment.ChatFragment;
import com.chatapp.Fragment.ProfileFragment;
import com.chatapp.Fragment.UsersFragment;
import com.chatapp.Update.UpdateHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListner{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);

        setSupportActionBar(toolbar);
        setUpViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        auth = FirebaseAuth.getInstance();

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
        }
        else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        UpdateHelper.with(this)
                .onUpdateCheck(this)
                .check();
    }

    private void setUpViewPager(ViewPager viewPager) {
        ViewPage viewPage = new ViewPage(getSupportFragmentManager());
        viewPage.addFragment(new ChatFragment(), "CHATS");
        viewPage.addFragment(new UsersFragment(), "USERS");
        viewPage.addFragment(new ProfileFragment(), "PROFILE");
        viewPager.setAdapter(viewPage);
    }

    private void status(String status) {

        firebaseUser = auth.getCurrentUser();

        if(firebaseUser != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);

            reference.updateChildren(hashMap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        String svaetime, savedate, main;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM yyyy");
        savedate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        svaetime = currentTime.format(calendar.getTime());

        main = svaetime+", "+savedate;
        status(main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.feedback) {
            startActivity(new Intent(HomeActivity.this, FeedbackActivity.class));
            return true;
        }
        else if(id == R.id.privacy) {
            startActivity(new Intent(HomeActivity.this, PrivacyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnUpdateCheckListner(final String urlapp) {
        AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                .setTitle("New version available")
                .setMessage("update new version for better expiriance")
                .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(HomeActivity.this, ""+urlapp, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
}
