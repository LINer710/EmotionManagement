package com.example.emotionmanagement.ui.usercenter;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.emotionmanagement.R;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextOldPassword, editTextNewPassword;
    private Button buttonUpdatePassword;
    private ImageView imageViewLogo; // 添加 ImageView
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        Log.d("CXL", "userId" + String.valueOf(userId));

        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonUpdatePassword = findViewById(R.id.buttonUpdatePassword);
        imageViewLogo = findViewById(R.id.imageViewLogo);

        // 加载本地头像
        loadUserAvatar();

        buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = editTextOldPassword.getText().toString().trim();
                String newPassword = editTextNewPassword.getText().toString().trim();

                // 发起网络请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient client = new OkHttpClient();

                            MediaType mediaType = MediaType.parse("application/json");
                            String requestBody = "{\"user_id\":" + userId + ",\"old_password_hash\":\"" + oldPassword + "\",\"new_password_hash\":\"" + newPassword + "\"}";
                            RequestBody body = RequestBody.create(mediaType, requestBody);

                            Request request = new Request.Builder()
                                    .url("http://192.168.68.170:5000/update_password")
                                    .post(body)
                                    .build();

                            Response response = client.newCall(request).execute();
                            String responseData = response.body().string();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("CXL", "responseData" + responseData);
                                    try {
                                        JSONObject jsonObject = new JSONObject(responseData);
                                        String message = jsonObject.getString("message");
                                        if (message.equals("密码更新成功")) {
                                            // 请求成功，更新UI
                                            Toast.makeText(ChangePasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            // 请求失败，显示错误信息
                                            Toast.makeText(ChangePasswordActivity.this, "密码修改失败：" + message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        // JSON解析出错，显示错误信息
                                        Toast.makeText(ChangePasswordActivity.this, "无法解析服务器响应", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 网络请求出现异常，显示错误信息
                                    Toast.makeText(ChangePasswordActivity.this, "网络请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    private void loadUserAvatar() {
        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
        String avatarUrl = sharedPref.getString("avatar_url", "");
        Log.d("CXL", "avatarUrl" + avatarUrl);

        if (!TextUtils.isEmpty(avatarUrl)) {
            // 如果本地存在头像URL，直接加载
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageViewLogo);
        } else {
            // 否则显示默认头像
            imageViewLogo.setImageResource(R.drawable.img_logo);

        }
    }
}
