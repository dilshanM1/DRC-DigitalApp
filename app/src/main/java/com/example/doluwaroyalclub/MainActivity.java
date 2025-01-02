package com.example.doluwaroyalclub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView nameTextView, phoneNumberTextView, registrationNumberTextView, aboutTxt, haveToPayCountTextView;
    private ImageView profileImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private static final int TIME_INTERVAL = 200; // Time in milliseconds to wait for second back press
    private long mBackPressed;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTextView = findViewById(R.id.nameTextView);
        phoneNumberTextView = findViewById(R.id.phoneTextView);
        registrationNumberTextView = findViewById(R.id.registrationNumberTextView);
        profileImageView = findViewById(R.id.profileImageView);
        aboutTxt = findViewById(R.id.AboutApp);
        haveToPayCountTextView = findViewById(R.id.haveToPayCountTextView);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userUID = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUID);

            fetchUserData();
        } else {
            Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Set up button click listeners
        setupButtonListeners();

        // Add logout button functionality
        Button logoutButton = findViewById(R.id.logOutButtonId);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        aboutTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutPage();
            }
        });

    }

    private void fetchUserData() {
        if (usersRef == null) {
            Log.e(TAG, "Database reference is null.");
            return;
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String registrationNumber = dataSnapshot.child("registrationNumber").getValue(String.class);
                    String imageUrl = dataSnapshot.child("profileImageURL").getValue(String.class);

                    if (name != null && email != null && phoneNumber != null && registrationNumber != null) {
                        Log.d(TAG, "User data - Name: " + name + ", Email: " + email + ", Phone: " + phoneNumber + ", Reg No: " + registrationNumber);

                        nameTextView.setText(name);
                        phoneNumberTextView.setText(phoneNumber);
                        registrationNumberTextView.setText(registrationNumber);

                        if (imageUrl != null) {
                            // Load the profile image using Glide
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image
                                    .error(R.drawable.baseline_error_24) // Error image
                                    .into(profileImageView);
                        }

                        // Fetch yearly data and calculate total amount due up to the current month
                        int countZero = 0;
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH); // January is 0, December is 11

                        for (int year = 2018; year <= currentYear; year++) {
                            DataSnapshot yearlyDataSnapshot = dataSnapshot.child("yearlyData").child(String.valueOf(year));
                            int monthsToConsider = (year == currentYear) ? currentMonth + 1 : 12;
                            for (int month = 0; month < monthsToConsider; month++) {
                                String monthName = getMonthName(month);
                                Integer value = yearlyDataSnapshot.child(monthName).getValue(Integer.class);
                                if (value != null && value == 0) {
                                    countZero++;
                                }
                            }
                        }
                        // Calculate the total amount due (count of "0" * 200)
                        int totalAmountDue = countZero * 200;
                        haveToPayCountTextView.setText(String.valueOf(totalAmountDue));
                    } else {
                        Toast.makeText(MainActivity.this, "Some user data is missing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to fetch user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month];
    }

    private void setupButtonListeners() {
        int[] buttonIds = {
                R.id.Button2018, R.id.Button2019, R.id.Button2020,
                R.id.Button2021, R.id.Button2022, R.id.Button2023,
                R.id.Button2024, R.id.Button2025, R.id.Button2026,
                R.id.Button2027, R.id.Button2028, R.id.Button2029,
                R.id.Button2030, R.id.Button2031, R.id.Button2032
        };

        Class<?>[] activities = {
                Activity2018.class, Activity2019.class, Activity2020.class,
                Activity2021.class, Activity2022.class, Activity2023.class,
                Activity2024.class, Activity2025.class, Activity2026.class,
                Activity2027.class, Activity2028.class, Activity2029.class,
                Activity2030.class, Activity2031.class, Activity2032.class
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final int buttonId = buttonIds[i];
            final Class<?> activityClass = activities[i];
            Button button = findViewById(buttonId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, activityClass);
                    startActivity(intent);
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: Call finish() to remove this activity from the back stack
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    public void aboutPage(){
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
        finish();
    }
}
