package com.example.doluwaroyalclub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Activity2031 extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private TextView januaryTxt, februaryTxt, marchTxt, aprilTxt, mayTxt, juneTxt,
            julyTxt, augustTxt, septemberTxt, octoberTxt, novemberTxt, decemberTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2031);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        januaryTxt = findViewById(R.id.JanuaryTxt);
        februaryTxt = findViewById(R.id.FebruaryTxt);
        marchTxt = findViewById(R.id.MarchTxt);
        aprilTxt = findViewById(R.id.AprilTxt);
        mayTxt = findViewById(R.id.MayTxt);
        juneTxt = findViewById(R.id.JuneTxt);
        julyTxt = findViewById(R.id.JulyTxt);
        augustTxt = findViewById(R.id.AugustTxt);
        septemberTxt = findViewById(R.id.SeptemberTxt);
        octoberTxt = findViewById(R.id.OctoberTxt);
        novemberTxt = findViewById(R.id.NovemberTxt);
        decemberTxt = findViewById(R.id.DecemberTxt);

        // Fetch user data
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        usersRef.child(userId).child("yearlyData").child("2031").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()) {
                        String month = monthSnapshot.getKey();
                        Integer value = monthSnapshot.getValue(Integer.class);

                        if (value != null) {
                            updateMonthData(month, value);
                        }
                    }
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateMonthData(String month, Integer value) {
        String text = value.toString();
        switch (month) {
            case "January":
                januaryTxt.setText(text);
                break;
            case "February":
                februaryTxt.setText(text);
                break;
            case "March":
                marchTxt.setText(text);
                break;
            case "April":
                aprilTxt.setText(text);
                break;
            case "May":
                mayTxt.setText(text);
                break;
            case "June":
                juneTxt.setText(text);
                break;
            case "July":
                julyTxt.setText(text);
                break;
            case "August":
                augustTxt.setText(text);
                break;
            case "September":
                septemberTxt.setText(text);
                break;
            case "October":
                octoberTxt.setText(text);
                break;
            case "November":
                novemberTxt.setText(text);
                break;
            case "December":
                decemberTxt.setText(text);
                break;
        }
    }
}
