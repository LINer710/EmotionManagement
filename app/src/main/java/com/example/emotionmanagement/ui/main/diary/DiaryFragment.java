package com.example.emotionmanagement.ui.main.diary;

import android.Manifest;
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

public class DiaryFragment extends Fragment {
    private EditText editMessage;
    private Button btnSend;
    private RecyclerView chatRecyclerView;
    private OkHttpClient client = new OkHttpClient();
    private ChatAdapter chatAdapter;  // 假设您已经创建了这个适配器类
    private StringBuilder messageBuilder = new StringBuilder();
    private int userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        Log.d("CXL", String.valueOf(userId));
        fetchUserDetails(userId);

        return view;
    }

    private void sendMessage() {
        String message = editMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            chatAdapter.addMessage(message);  // 添加用户消息到聊天界面
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
                .url("https://xiaofei.zeabur.app/v1/chat/completions")
                .post(body)
//                .addHeader("Authorization", "Bearer fastgpt-iUqCFhGNo5EcXGZHByl3YkEmbrFyPGpECid6vWJu1boY4zef7fttqPUDNBEUdZOx")  // Replace with your token
                .addHeader("Authorization", "Bearer sk-zQOYt7UCrvSjzx6G094bFeC7111b4cF1A75c88E855714d5e")  // Replace with your token
                .addHeader("Content-Type", "application/json")
                .build();

        // 异步执行请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() ->
                        chatAdapter.addMessage("Error: " + e.getMessage()));
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
        String url = "http://192.168.68.170:5000/get_avatar/" + userId;
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
        String url = "http://192.168.68.170:5000/get_nickname/" + userId;
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

}