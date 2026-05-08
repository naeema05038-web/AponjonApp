package com.example.aponjon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        prefs = getSharedPreferences("AponjonPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("user_name", "");

        Button btnStart = findViewById(R.id.btnStartAponjon);
        btnStart.setOnClickListener(v -> {
            if (savedName.isEmpty()) {
                showNameInputDialog();
            } else {
                showWelcomeToast(savedName);
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void showNameInputDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter your name");

        new AlertDialog.Builder(this)
                .setTitle("Welcome to APONJON!")
                .setMessage("What should I call you?")
                .setView(input)
                .setPositiveButton("Start", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        prefs.edit().putString("user_name", name).apply();
                        showWelcomeToast(name);
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showWelcomeToast(String name) {
        Toast.makeText(this, "🌟 Welcome, " + name + "! 🌟", Toast.LENGTH_LONG).show();
    }
}
