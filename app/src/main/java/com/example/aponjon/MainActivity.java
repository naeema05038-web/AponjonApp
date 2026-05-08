package com.example.aponjon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private LinearLayout normalMode;
    private LinearLayout startBtn, medicineBtn, emergencyBtn, safetyBtn, lovedOneBtn, settingsBtn;
    private TextView tvGreeting;
    private TextToSpeech tts;
    private String userName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUserName();
        checkMicrophonePermission();

        normalMode = findViewById(R.id.normalMode);
        startBtn = findViewById(R.id.startBtn);
        medicineBtn = findViewById(R.id.medicineBtn);
        emergencyBtn = findViewById(R.id.emergencyBtn);
        safetyBtn = findViewById(R.id.safetyBtn);
        lovedOneBtn = findViewById(R.id.lovedOneBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        tvGreeting = findViewById(R.id.tvGreeting);

        updateGreeting();
        tts = new TextToSpeech(this, this);

        // Start Conversation - Opens ChatActivity (NEW PAGE)
        startBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Medicine Reminder
        medicineBtn.setOnClickListener(v -> startActivity(new Intent(this, MedicineActivity.class)));

        // Other Buttons - Coming Soon
        emergencyBtn.setOnClickListener(v -> Toast.makeText(this, "🚨 Emergency Alert - Coming Soon!", Toast.LENGTH_SHORT).show());
        safetyBtn.setOnClickListener(v -> Toast.makeText(this, "🛡️ Safety Monitoring - Coming Soon!", Toast.LENGTH_SHORT).show());
        lovedOneBtn.setOnClickListener(v -> Toast.makeText(this, "👨‍👩‍👧 Loved One - Coming Soon!", Toast.LENGTH_LONG).show());
        settingsBtn.setOnClickListener(v -> Toast.makeText(this, "⚙️ Settings - Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void loadUserName() {
        SharedPreferences prefs = getSharedPreferences("AponjonPrefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
    }

    private void updateGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Good Morning";
        else if (hour < 17) greeting = "Good Afternoon";
        else greeting = "Good Evening";
        tvGreeting.setText(greeting + ", " + userName + "!");
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "APONJON is listening...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);
            // You can handle voice input here if needed
        }
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
