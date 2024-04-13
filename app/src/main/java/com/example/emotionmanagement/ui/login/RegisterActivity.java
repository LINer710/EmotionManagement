package com.example.emotionmanagement.ui.login;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.util.ThemeManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.layout_register);
        // 使状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // 初始化UI组件
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // 设置注册按钮点击事件
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入的用户名、密码和确认密码
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // 检查用户名、密码和确认密码是否为空
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "请输入用户名、密码和确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查密码和确认密码是否一致
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "密码和确认密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 执行注册操作
                performRegister(username, password);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
    }

    private void performRegister(String username, String password) {
        OkHttpClient client = new OkHttpClient();
//        if (username.length() < 6) {
//            Toast.makeText(RegisterActivity.this, "用户名不能少于6个字符", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 检查密码长度和复杂度
//        if (password.length() < 6 || !isPasswordValid(password)) {
//            Toast.makeText(RegisterActivity.this, "密码必须包含字母和数字，并且不少于6个字符", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // 构建请求体，包含用户名和密码的哈希值
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password_hash", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // 构建 POST 请求
        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/register")
                .post(requestBody)
                .build();

        // 打印请求信息
//        Log.d("CXL", "Sending registration request to server...");

        // 异步执行请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败
                Log.e("CXL", "Request failed: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "无法连接到服务器", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 处理服务器响应
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String message = jsonObject.getString("message");
//                            Log.d("CXL", "Response: " + responseData);
//                            Log.d("CXL", "Message: " + message);
                            if (message.equals("用户注册成功")) {
                                // 注册成功
                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                finish(); // 结束当前注册页面
                            } else {
                                // 注册失败
                                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CXL", "JSON parsing error: " + response);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, "用户重复，请重新注册~", Toast.LENGTH_SHORT).show();
                                }
                            });
                            editTextUsername.setText("");
                            editTextPassword.setText("");
                            editTextConfirmPassword.setText("");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // 检查密码是否包含字母和数字
    private boolean isPasswordValid(String password) {
        boolean containsDigit = false;
        boolean containsLetter = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                containsDigit = true;
            } else if (Character.isLetter(c)) {
                containsLetter = true;
            }
            if (containsDigit && containsLetter) {
                return true;
            }
        }
        return false;
    }
}
