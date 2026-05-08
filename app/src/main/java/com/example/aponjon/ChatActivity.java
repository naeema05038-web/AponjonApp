package com.example.aponjon;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int SPEECH_REQUEST_CODE = 100;
    private TextToSpeech tts;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend, btnVoice, btnBack;
    private ChatAdapter adapter;
    private List<ChatMessage> messagesList = new ArrayList<>();
    private String userName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userName = getSharedPreferences("AponjonPrefs", MODE_PRIVATE).getString("user_name", "User");

        btnBack = findViewById(R.id.btnBack);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);

        adapter = new ChatAdapter(messagesList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        tts = new TextToSpeech(this, this);

        addBotMessage("Hello " + userName + "! I'm APONJON. How can I help you today?");

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendUserMessage(msg);
                etMessage.setText("");
            }
        });

        btnVoice.setOnClickListener(v -> startVoiceInput());
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
            String spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            etMessage.setText(spokenText);
            sendUserMessage(spokenText);
        }
    }

    private void sendUserMessage(String message) {
        addUserMessage(message);
        String reply = getSmartReply(message);
        addBotMessage(reply);
        speak(reply);
    }

    private String getSmartReply(String msg) {
        String lower = msg.toLowerCase();
        if (lower.contains("fever")) return "I'm sorry you have fever! Please rest and drink warm water. 🥵";
        if (lower.contains("emergency")) return "🚨 Please call 999 immediately! 🆘";
        if (lower.contains("good morning")) return "Good morning " + userName + "! ☀️ Have a lovely day!";
        if (lower.contains("how are you")) return "I'm fine, thank you! How are you feeling today? 😊";
        if (lower.contains("sad")) return "I'm here for you " + userName + "! 🤗❤️ Would you like to talk?";
        if (lower.contains("thank")) return "You're most welcome, " + userName + "! 💖";
        if (lower.contains("bye")) return "Take care " + userName + "! 👋 I'm always here.";
        return "I hear you, " + userName + "! 💙 Tell me more. I'm listening.";
    }

    private void addUserMessage(String message) {
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        messagesList.add(new ChatMessage(message, true, time));
        adapter.notifyItemInserted(messagesList.size() - 1);
        rvMessages.smoothScrollToPosition(messagesList.size() - 1);
    }

    private void addBotMessage(String message) {
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        messagesList.add(new ChatMessage(message, false, time));
        adapter.notifyItemInserted(messagesList.size() - 1);
        rvMessages.smoothScrollToPosition(messagesList.size() - 1);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.setPitch(0.9f);
            tts.setSpeechRate(0.8f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "✅ APONJON voice is ready!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "❌ TTS initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public static class ChatMessage {
        private String message;
        private boolean isUser;
        private String time;

        public ChatMessage(String message, boolean isUser, String time) {
            this.message = message;
            this.isUser = isUser;
            this.time = time;
        }

        public String getMessage() { return message; }
        public boolean isUser() { return isUser; }
        public String getTime() { return time; }
    }
}