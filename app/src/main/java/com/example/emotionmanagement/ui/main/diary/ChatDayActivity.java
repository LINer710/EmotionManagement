package com.example.emotionmanagement.ui.main.diary;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import org.json.JSONObject;
import org.json.JSONArray;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.util.ThemeManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ChatDayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_chat_day);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(recyclerView);

        recyclerView.setAdapter(chatAdapter);

        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        loadChatHistory(selectedDate);
    }

    private void loadChatHistory(String date) {
        SharedPreferences sharedPreferences = getSharedPreferences("diary_messages", MODE_PRIVATE);
        String userMessagesJson = sharedPreferences.getString(ChatAdapter.KEY_USER_MESSAGES, "[]");
        String serverMessagesJson = sharedPreferences.getString(ChatAdapter.KEY_SERVER_MESSAGES, "[]");

        try {
            JSONArray userMessagesArray = new JSONArray(userMessagesJson);
            JSONArray serverMessagesArray = new JSONArray(serverMessagesJson);

            List<String> userMessages = new ArrayList<>();
            List<String> serverMessages = new ArrayList<>();

            // 从 SharedPreferences 中筛选与所选日期匹配的消息
            extractMessagesByDate(userMessages, userMessagesArray, date);
            extractMessagesByDate(serverMessages, serverMessagesArray, date);

            // 将用户消息和服务器消息按照对话顺序合并到适配器的消息列表中
            int userIndex = 0, serverIndex = 0;
            while (userIndex < userMessages.size() || serverIndex < serverMessages.size()) {
                if (userIndex < userMessages.size()) {
                    chatAdapter.addMessage(userMessages.get(userIndex), ChatAdapter.getViewTypeUser());
                    userIndex++;
                }
                if (serverIndex < serverMessages.size()) {
                    chatAdapter.addMessage(serverMessages.get(serverIndex), ChatAdapter.getViewTypeServer());
                    serverIndex++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractMessagesByDate(List<String> messages, JSONArray messagesArray, String date) throws JSONException {
        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObj = messagesArray.getJSONObject(i);
            String dateTime = messageObj.getString("dateTime").substring(0, 10);  // Assuming "yyyy-MM-dd" format
            if (dateTime.equals(date)) {
                messages.add(messageObj.getString("text"));
            }
        }
    }

}
