package com.example.emotionmanagement.ui.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.ui.main.MainActivity;

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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView textRegister;
    private boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        // 初始化UI组件
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        textRegister = findViewById(R.id.textViewRegister);
        // 初始化密码输入框的可见性状态
        isPasswordVisible = false;
        editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_see_close, 0);

        setStyledText(textRegister, "还没有账号? ", "去注册", Color.GRAY, Color.BLACK, false, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里添加跳转到注册页面的代码
                Toast.makeText(LoginActivity.this, "跳转到注册页面", Toast.LENGTH_SHORT).show();
            }
        });
        // 设置登录按钮点击事件
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入的用户名和密码
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // 检查用户名和密码是否为空
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 执行登录操作
                performLogin(username, password);
            }
        });
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 设置密码可见性图标点击事件
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int drawableEnd = editTextPassword.getRight() - editTextPassword.getCompoundDrawables()[2].getBounds().width();
                    if (event.getRawX() >= drawableEnd) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void performLogin(String username, String password) {
        OkHttpClient client = new OkHttpClient();
      /*  // 检查用户名长度
        if (username.length() < 6) {
            Toast.makeText(LoginActivity.this, "用户名不能少于6个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查密码长度和复杂度
        if (password.length() < 6 || !isPasswordValid(password)) {
            Toast.makeText(LoginActivity.this, "密码必须包含字母和数字，并且不少于6个字符", Toast.LENGTH_SHORT).show();
            return;
        }*/

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
                .url("http://192.168.68.170:5000/login")
                .post(requestBody)
                .build();

        // 打印请求信息
        Log.d("CXL", "Sending login request to server...");

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
                        Toast.makeText(LoginActivity.this, "无法连接到服务器", Toast.LENGTH_SHORT).show();
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
                            Log.d("CXL", "Response: " + responseData);
                            Log.d("CXL", "Message: " + message);
                            if (message.equals("登录成功")) {
                                // 登录成功
                                getUserId(username);
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                // 进入主界面或其他操作
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // 登录失败
                                Toast.makeText(LoginActivity.this, "用户名或密码无效", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CXL", "JSON parsing error: " + e.getMessage());
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

    // 切换密码可见性
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
            isPasswordVisible = false;
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_see_close, 0);
        } else {
            editTextPassword.setTransformationMethod(null);
            isPasswordVisible = true;
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_see_open, 0);
        }
    }

    private void setStyledText(TextView textView, String firstText, String secondText, int firstColor, int secondColor, boolean firstBold, boolean secondBold, View.OnClickListener clickListener) {
        // 设置第一部分文本的样式和颜色
        SpannableString spannableFirst = new SpannableString(firstText);
        spannableFirst.setSpan(new ForegroundColorSpan(firstColor), 0, spannableFirst.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (firstBold) {
            spannableFirst.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableFirst.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 设置第二部分文本的样式和颜色
        SpannableString spannableSecond = new SpannableString(secondText);
        spannableSecond.setSpan(new ForegroundColorSpan(secondColor), 0, spannableSecond.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (secondBold) {
            spannableSecond.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableSecond.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 合并两部分文本
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(spannableFirst);
        builder.append(spannableSecond);

        // 将设置好的文本应用到 TextView
        textView.setText(builder);

        // 设置第二部分文本的点击事件
        if (clickListener != null) {
            textView.setOnClickListener(clickListener);
        }
    }

    private void getUserId(String username) {
        OkHttpClient client = new OkHttpClient();

        // 构建 GET 请求
        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/get_user_id?username=" + username)
                .build();

        // 打印请求信息
        Log.d("CXL", "Sending get user id request to server...");

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
                        Toast.makeText(LoginActivity.this, "无法连接到服务器", Toast.LENGTH_SHORT).show();
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
                            if (jsonObject.has("user_id")) {
                                int userId = jsonObject.getInt("user_id");
                                Log.d("CXL", "User ID: " + userId);
                                // 保存用户ID到SharedPreferences
                                saveUserIdToSharedPreferences(userId);
                            } else {
                                String message = jsonObject.getString("message");
                                Log.e("CXL", "Failed to get user ID: " + message);
                            }
                        } catch (JSONException e) {
                            Log.e("CXL", "JSON parsing error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // 保存用户ID到SharedPreferences
    private void saveUserIdToSharedPreferences(int userId) {
        SharedPreferences sharedPref = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("user_id", userId);
        editor.apply();
    }
}


