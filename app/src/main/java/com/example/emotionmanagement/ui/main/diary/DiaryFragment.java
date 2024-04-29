package com.example.emotionmanagement.ui.main.diary;

import static com.example.emotionmanagement.MyApp.URL;
import static com.example.emotionmanagement.ui.main.diary.ChatAdapter.KEY_SERVER_MESSAGES;
import static com.example.emotionmanagement.ui.main.diary.ChatAdapter.KEY_USER_MESSAGES;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotionmanagement.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;
import okio.BufferedSource;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiaryFragment extends Fragment {
    private EditText editMessage;
    private Button btnSend;
    private RecyclerView chatRecyclerView;
    private OkHttpClient client = new OkHttpClient();
    private ChatAdapter chatAdapter;  // 假设您已经创建了这个适配器类
    private StringBuilder messageBuilder = new StringBuilder();
    private int userId;
    // SharedPreferences 文件名
    private static final String SHARED_PREF_NAME = "diary_messages";
    // SharedPreferences 存储对话内容的键名
    private static final String KEY_MESSAGES = "messages";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatAdapter = new ChatAdapter(chatRecyclerView);
        chatRecyclerView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        Log.d("CXL", String.valueOf(userId));
        fetchUserDetails(userId);

        loadMessagesFromSharedPreferences();


        return view;
    }



    public void loadMessagesFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // 创建用于存储用户消息和服务器消息的临时列表
        List<String> userMessages = new ArrayList<>();
        List<String> serverMessages = new ArrayList<>();

        // 从SharedPreferences中加载用户消息
        String userMessagesJson = sharedPreferences.getString(KEY_USER_MESSAGES, null);
        if (userMessagesJson != null) {
            try {
                JSONArray userMessagesArray = new JSONArray(userMessagesJson);
                for (int i = 0; i < userMessagesArray.length(); i++) {
                    String userMessage = userMessagesArray.getString(i);
                    userMessages.add(userMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 从SharedPreferences中加载服务器消息
        String serverMessagesJson = sharedPreferences.getString(KEY_SERVER_MESSAGES, null);
        if (serverMessagesJson != null) {
            try {
                JSONArray serverMessagesArray = new JSONArray(serverMessagesJson);
                for (int i = 0; i < serverMessagesArray.length(); i++) {
                    String serverMessage = serverMessagesArray.getString(i);
                    serverMessages.add(serverMessage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 将用户消息和服务器消息按照对话顺序合并到适配器的消息列表中
        int userIndex = 0;
        int serverIndex = 0;
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
    }




    public void saveMessagesToSharedPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 保存用户消息列表
        JSONArray userMessagesArray = new JSONArray(chatAdapter.getUserMessages());
        editor.putString(KEY_USER_MESSAGES, userMessagesArray.toString());

        // 保存服务器消息列表
        JSONArray serverMessagesArray = new JSONArray(chatAdapter.getServerMessages());
        editor.putString(KEY_SERVER_MESSAGES, serverMessagesArray.toString());

        editor.apply();
    }


    private void sendMessage() {
        String message = editMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            chatAdapter.addMessage(message, ChatAdapter.getViewTypeUser());  // 添加用户消息到聊天界面
            postRequest(message);
            editMessage.setText("");
        }
    }

    private void postRequest(String messageContent) {
        // 构建 JSON 对象
        JSONObject jsonBody = new JSONObject();
        try {
//            jsonBody.put("chatId", "abcd");
            jsonBody.put("stream", true);
            jsonBody.put("model", "gpt-3.5-turbo");
//            jsonBody.put("detail", false);
//            JSONObject variables = new JSONObject();
//            variables.put("uid", "asdfadsfasfd2323");
//            variables.put("name", "张三");
//            jsonBody.put("variables", variables);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("content", messageContent);
            message.put("role", "user");
            messages.put(message);
            jsonBody.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
//                .url("https://api.fastgpt.in/api/v1/chat/completions")
//                .url("https://xiaofei.zeabur.app/v1/chat/completions")
                .url("https://testone.caifree.com/v1/chat/completions")

                .post(body)
//                .addHeader("Authorization", "Bearer fastgpt-iUqCFhGNo5EcXGZHByl3YkEmbrFyPGpECid6vWJu1boY4zef7fttqPUDNBEUdZOx")  // Replace with your token
                .addHeader("Authorization", "Bearer sk-bBuL5QvlgkiZJxMR42C1C1Af515143E5852b4774032f4455")  // Replace with your token
                .addHeader("Content-Type", "application/json")
                .build();

        // 异步执行请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() ->
                        chatAdapter.addMessage("Error: " + e.getMessage(), ChatAdapter.getViewTypeServer()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                BufferedSource source = response.body().source();
                try {
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();  // Read next line from the stream
                        if (line != null) {
                            processLine(line);  // Process each line as it comes
                        }
                    }
                } catch (EOFException ignored) {
                    // End of stream
                } finally {
                    response.close();
                }
            }
        });
    }

    private StringBuilder messageBuffer = new StringBuilder();

    private void processLine(String line) {
        if (line.startsWith("data:") && !line.equals("data: [DONE]")) {
            String jsonPart = line.substring(5).trim();  // 去除前缀 "data:"
            try {
                JSONObject jsonObject = new JSONObject(jsonPart);
                JSONArray choices = jsonObject.getJSONArray("choices");
                for (int i = 0; i < choices.length(); i++) {
                    JSONObject choice = choices.getJSONObject(i);
                    if (choice.has("delta")) {
                        JSONObject delta = choice.getJSONObject("delta");
                        if (delta.has("content")) {
                            String content = delta.getString("content");
                            if (!content.isEmpty()) {
                                updateUI(content);  // 立即更新 UI
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("ProcessLine", "JSON parsing error", e);
            }
        } else if (line.equals("data: [DONE]")) {
            // 当接收到 "data: [DONE]" 时，你可能还想进行一些清理工作，但主要内容更新已在接收时完成
        }
    }

    private void updateUI(final String message) {
        getActivity().runOnUiThread(() -> chatAdapter.appendServerMessage(message));
    }
    private void fetchUserDetails(int userId) {
        fetchAvatar(userId);
        fetchNickname(userId);
    }

    private void fetchAvatar(int userId) {
        String url = URL + "/get_avatar/" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Log failure or handle it
                Log.e("FetchAvatar", "Failed to fetch avatar", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    Log.d("CXL", "Avatar URL: " + jsonObject.optString("avatar"));
                    final String avatarUrl = jsonObject.optString("avatar", null);
                    getActivity().runOnUiThread(() -> {
                        // Update UI with avatar URL
                        chatAdapter.setUserAvatar(avatarUrl);
                    });
                } catch (JSONException e) {
                    Log.e("FetchAvatar", "JSON parsing error", e);
                }
            }
        });
    }

    private void fetchNickname(int userId) {
        String url = URL + "/get_nickname/" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Log failure or handle it
                Log.e("FetchNickname", "Failed to fetch nickname", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("CXL", "Nickname: " + responseData);
                        final String nickname = jsonObject.optString("nickname", "用户");
                        getActivity().runOnUiThread(() -> {
                            // Update UI with nickname
                            chatAdapter.setUserNickname(nickname);
                        });
                    } catch (JSONException e) {
                        Log.e("FetchNickname", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        // 在Fragment停止时保存对话内容到SharedPreferences中
        saveMessagesToSharedPreferences();
    }
}