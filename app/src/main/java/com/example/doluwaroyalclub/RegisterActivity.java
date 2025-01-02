package com.example.doluwaroyalclub;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneNumberEditText, registrationNumberEditText;
    private TextInputLayout nameInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout, phoneNumberInputLayout, registrationNumberInputLayout;
    private Button registerButton, selectImageButton;
    private TextView loginTextView;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private DatabaseReference usersRef; // Realtime Database reference
    private Uri imageUri;

    static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_images");
        db = FirebaseFirestore.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users"); // Realtime Database reference

        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        phoneNumberInputLayout = findViewById(R.id.phoneNumberInputLayout);
        registrationNumberInputLayout = findViewById(R.id.registrationNumberInputLayout);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        registrationNumberEditText = findViewById(R.id.registrationNumberEditText);

        registerButton = findViewById(R.id.buttonRegister);
        selectImageButton = findViewById(R.id.buttonSelectImage);
        loginTextView = findViewById(R.id.loginTextView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        // Check phone number has 10 numbers
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString();
                if (phoneNumber.length() != 10) {
                    phoneNumberInputLayout.setError("Phone number must be 10 digits long");
                } else {
                    phoneNumberInputLayout.setError(null); // Clear the error
                }
            }
        });

        // Capitalize first letter in Name
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Handle text change
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    String text = s.toString();
                    String capitalizedText = capitalizeWords(text);
                    if (!text.equals(capitalizedText)) {
                        nameEditText.removeTextChangedListener(this);
                        nameEditText.setText(capitalizedText);
                        nameEditText.setSelection(capitalizedText.length()); // Move cursor to end
                        nameEditText.addTextChangedListener(this);
                    }
                }
            }
        });
    }

    private String capitalizeWords(String text) {
        StringBuilder capitalized = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                capitalized.append(c);
            } else {
                if (capitalizeNext) {
                    capitalized.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    capitalized.append(c);
                }
            }
        }
        return capitalized.toString();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String registrationNumber = registrationNumberEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty() || registrationNumber.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate random account number
        String accountNumber = generateAccountNumber();

        // Show progress dialog
        progressDialog.show();

        // Check if an image is selected
        if (imageUri != null) {
            // Upload image to Firebase Storage
            StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            registerUserWithImage(name, email, password, phoneNumber, registrationNumber, accountNumber, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no image selected, register user without image
            registerUserWithoutImage(name, email, password, phoneNumber, registrationNumber, accountNumber);
        }
    }

    private void registerUserWithoutImage(String name, String email, String password, String phoneNumber, String registrationNumber, String accountNumber) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user); // Send verification email
                            saveUserData(name, email, password, phoneNumber, registrationNumber, accountNumber, null, user.getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUserWithImage(String name, String email, String password, String phoneNumber, String registrationNumber, String accountNumber, String imageUrl) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user); // Send verification email
                            saveUserData(name, email, password, phoneNumber, registrationNumber, accountNumber, imageUrl, user.getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String name, String email, String password, String phoneNumber, String registrationNumber, String accountNumber, @Nullable String imageUrl, String userId) {
        // Create a user object to save in the database
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);
        user.put("registrationNumber", registrationNumber);
        user.put("accountNumber", accountNumber);
        if (imageUrl != null) {
            user.put("profileImageURL", imageUrl);
        }

// Add the yearly and monthly values with correct ordering
        Map<String, Object> yearlyData = new HashMap<>();
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        for (int year = 2018; year <= 2032; year++) {
            Map<String, Object> monthlyData = new HashMap<>();
            for (int month = 0; month < months.length; month++) {
                monthlyData.put(months[month], 0); // Default value of 0
            }
            yearlyData.put(String.valueOf(year), monthlyData);
        }
        user.put("yearlyData", yearlyData);

        // Save the user data to Realtime Database using userId
        usersRef.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getMonthName(int month) {
        switch (month) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "";
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String generateAccountNumber() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000; // Generate a 6-digit number
        return String.valueOf(number);
    }
}
