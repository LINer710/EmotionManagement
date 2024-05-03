package com.example.emotionmanagement.ui.main.diary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emotionmanagement.R;


import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.util.ThemeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ChatHistoryActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_chat_history);


        CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Intent intent = new Intent(ChatHistoryActivity.this, ChatDayActivity.class); // 创建跳转到 ChatDayActivity 的意图
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            intent.putExtra("SELECTED_DATE", sdf.format(selectedDate.getTime())); // 将选定日期作为字符串传递
            startActivity(intent);
        });

    }

    private void loadChatHistory(String date) {
        SharedPreferences sharedPreferences = getSharedPreferences("diary_messages", MODE_PRIVATE);
        String userMessagesJson = sharedPreferences.getString("user_messages", "[]");
        String serverMessagesJson = sharedPreferences.getString("server_messages", "[]");

        try {
            JSONArray userMessagesArray = new JSONArray(userMessagesJson);
            JSONArray serverMessagesArray = new JSONArray(serverMessagesJson);
            chatAdapter.clearMessages();

            for (int i = 0; i < userMessagesArray.length(); i++) {
                JSONObject userMessage = userMessagesArray.getJSONObject(i);
                String dateTime = userMessage.getString("dateTime").substring(0, 10);
                if (dateTime.equals(date)) {
                    chatAdapter.addMessage(userMessage.getString("text"), ChatAdapter.getViewTypeUser());
                }
            }

            for (int i = 0; i < serverMessagesArray.length(); i++) {
                JSONObject serverMessage = serverMessagesArray.getJSONObject(i);
                String dateTime = serverMessage.getString("dateTime").substring(0, 10);
                if (dateTime.equals(date)) {
                    chatAdapter.addMessage(serverMessage.getString("text"), ChatAdapter.getViewTypeServer());
                }
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing messages: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
    }
}
