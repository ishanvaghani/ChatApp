package com.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class FeedbackActivity extends AppCompatActivity {

    EditText feedback;
    Button submit;
    DatabaseReference reference;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedback = findViewById(R.id.feedback_text);
        submit = findViewById(R.id.submit);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Feedback");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedbackString = feedback.getText().toString();
                reference = FirebaseDatabase.getInstance().getReference("Feedback").child(UUID.randomUUID().toString());
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("feedback", feedbackString);
                reference.setValue(hashMap);
                feedback.setText("");
                Toast.makeText(FeedbackActivity.this, "Thanks for your feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
