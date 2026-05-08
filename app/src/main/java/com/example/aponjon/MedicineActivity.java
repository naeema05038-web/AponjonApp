package com.example.aponjon;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MedicineActivity extends AppCompatActivity {

    private EditText etMedicine;
    private Button btnTime;
    private Button btnSave;
    private RecyclerView rvReminders;
    private ReminderAdapter reminderAdapter;
    private List<ReminderItem> remindersList = new ArrayList<>();

    private int selectedHour = 0;
    private int selectedMinute = 0;
    private boolean timeSelected = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        etMedicine = findViewById(R.id.etMedicine);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);
        rvReminders = findViewById(R.id.rvReminders);

        createNotificationChannel();
        loadReminders();
        setupRecyclerView();

        btnTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveReminder());

        timeUpdateRunnable = () -> {
            if (reminderAdapter != null) reminderAdapter.notifyDataSetChanged();
            handler.postDelayed(timeUpdateRunnable, 60000);
        };
        handler.post(timeUpdateRunnable);
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            selectedHour = hourOfDay;
            selectedMinute = minuteOfHour;
            timeSelected = true;
            String ampm = (selectedHour >= 12) ? "PM" : "AM";
            int displayHour = (selectedHour > 12) ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
            btnTime.setText(String.format("%02d:%02d %s", displayHour, selectedMinute, ampm));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void saveReminder() {
        String medicine = etMedicine.getText().toString().trim();

        if (medicine.isEmpty()) {
            Toast.makeText(this, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!timeSelected) {
            Toast.makeText(this, "Please select a time first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog();
                return;
            }
        }

        scheduleReminder(medicine);

        String ampm = (selectedHour >= 12) ? "PM" : "AM";
        int displayHour = (selectedHour > 12) ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
        String timeText = String.format("%02d:%02d %s", displayHour, selectedMinute, ampm);

        ReminderItem reminder = new ReminderItem(
                System.currentTimeMillis(),
                medicine,
                selectedHour,
                selectedMinute,
                timeText
        );
        remindersList.add(reminder);
        saveRemindersList();
        reminderAdapter.notifyItemInserted(remindersList.size() - 1);

        etMedicine.setText("");
        btnTime.setText("Select Time");
        timeSelected = false;

        Toast.makeText(this, "✅ Reminder saved for " + medicine + " at " + timeText, Toast.LENGTH_SHORT).show();
    }

    private void scheduleReminder(String medicine) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medicine_name", medicine);
        intent.putExtra("reminder_id", System.currentTimeMillis());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private String getTimeUntilReminder(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.HOUR_OF_DAY, hour);
        reminderTime.set(Calendar.MINUTE, minute);
        reminderTime.set(Calendar.SECOND, 0);

        if (reminderTime.getTimeInMillis() <= now.getTimeInMillis()) {
            reminderTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        long diff = reminderTime.getTimeInMillis() - now.getTimeInMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (hours > 0) return hours + "h " + minutes + "m left";
        if (minutes > 0) return minutes + "m left";
        return "Soon";
    }

    private void setupRecyclerView() {
        reminderAdapter = new ReminderAdapter(remindersList, position -> showDeleteConfirmDialog(position), this);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        rvReminders.setAdapter(reminderAdapter);
    }

    private void showDeleteConfirmDialog(int position) {
        ReminderItem reminder = remindersList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Delete reminder for '" + reminder.getMedicineName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    remindersList.remove(position);
                    saveRemindersList();
                    reminderAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("APONJON needs permission to set exact alarm reminders.")
                .setPositiveButton("Open Settings", (dialog, which) -> startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveRemindersList() {
        SharedPreferences prefs = getSharedPreferences("MedicinePrefs", MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (ReminderItem r : remindersList) {
            if (sb.length() > 0) sb.append("|||");
            sb.append(r.getId()).append(",").append(r.getMedicineName()).append(",")
                    .append(r.getHour()).append(",").append(r.getMinute()).append(",").append(r.getTimeText());
        }
        prefs.edit().putString("reminders_list", sb.toString()).apply();
    }

    private void loadReminders() {
        String saved = getSharedPreferences("MedicinePrefs", MODE_PRIVATE).getString("reminders_list", "");
        if (!saved.isEmpty()) {
            for (String item : saved.split("\\|\\|\\|")) {
                String[] parts = item.split(",");
                if (parts.length == 5) {
                    remindersList.add(new ReminderItem(Long.parseLong(parts[0]), parts[1],
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), parts[4]));
                }
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("medicine_channel", "Medicine Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders to take your medicine");
            channel.enableVibration(true);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeUpdateRunnable);
    }


    public static class ReminderItem {
        private final long id;
        private final String medicineName;
        private final int hour, minute;
        private final String timeText;

        public ReminderItem(long id, String medicineName, int hour, int minute, String timeText) {
            this.id = id;
            this.medicineName = medicineName;
            this.hour = hour;
            this.minute = minute;
            this.timeText = timeText;
        }

        public long getId() { return id; }
        public String getMedicineName() { return medicineName; }
        public int getHour() { return hour; }
        public int getMinute() { return minute; }
        public String getTimeText() { return timeText; }
    }


    public static class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
        private final List<ReminderItem> reminders;
        private final OnDeleteClickListener listener;
        private final Context context;

        public interface OnDeleteClickListener { void onDeleteClick(int position); }

        public ReminderAdapter(List<ReminderItem> reminders, OnDeleteClickListener listener, Context context) {
            this.reminders = reminders;
            this.listener = listener;
            this.context = context;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMedicineName, tvTime, tvTimeLeft;
            Button btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReminderItem r = reminders.get(position);
            holder.tvMedicineName.setText(r.getMedicineName());
            holder.tvTime.setText(r.getTimeText());
            holder.tvTimeLeft.setText(getTimeUntilReminder(context, r.getHour(), r.getMinute()));
            holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDeleteClick(position); });
        }

        @Override public int getItemCount() { return reminders.size(); }

        private String getTimeUntilReminder(Context context, int hour, int minute) {
            Calendar now = Calendar.getInstance();
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.set(Calendar.HOUR_OF_DAY, hour);
            reminderTime.set(Calendar.MINUTE, minute);
            reminderTime.set(Calendar.SECOND, 0);

            if (reminderTime.getTimeInMillis() <= now.getTimeInMillis()) {
                reminderTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            long diff = reminderTime.getTimeInMillis() - now.getTimeInMillis();
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

            if (hours > 0) return hours + "h " + minutes + "m left";
            if (minutes > 0) return minutes + "m left";
            return "Soon";
        }
    }
}
