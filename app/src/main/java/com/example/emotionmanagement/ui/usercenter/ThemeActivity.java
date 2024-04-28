package com.example.emotionmanagement.ui.usercenter;

import static com.example.emotionmanagement.MyApp.URL;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_theme);
        // 使状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        LinearLayout linearLayoutColors = findViewById(R.id.linearLayoutColors);
        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);

        // 颜色块的宽度和高度
        int colorBlockSize = 215; // 设置为 215 像素
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.color_block_margin_bottom); // 底部间距

        // 颜色名称数组
        String[] colorNames = {
                "欧碧", "春辰", "苍葭", "翠微",
                "西子", "二绿", "铜青", "石绿",
                "窃蓝", "监德", "苍苍", "群青",
                "退红", "樱花", "丁香", "木槿",
                "半见", "莺儿", "黄白", "松花",
                "盈盈", "粉米", "桃夭", "水红",
                "小红", "鹤顶", "朱殷", "银珠"
        };

        // 创建一个5*4的颜色块网格
        int[][] colors = {
                {Color.parseColor("#c0d695"), Color.parseColor("#a9be7b"), Color.parseColor("#a8bf8f"), Color.parseColor("#4c8045")},
                {Color.parseColor("#87c0ca"), Color.parseColor("#5da39d"), Color.parseColor("#3d8e86"), Color.parseColor("#206864")},
                {Color.parseColor("#88abda"), Color.parseColor("#6f94cd"), Color.parseColor("#5976ba"), Color.parseColor("#2e59a7")},
                {Color.parseColor("#f0cfe3"), Color.parseColor("#e4b8d5"), Color.parseColor("#ce93bf"), Color.parseColor("#ba7ab1")},
                {Color.parseColor("#fffbc7"), Color.parseColor("#ebe1a9"), Color.parseColor("#fff799"), Color.parseColor("#ffee6f")},
                {Color.parseColor("#f9d3e3"), Color.parseColor("#efc4ce"), Color.parseColor("#f6bec8"), Color.parseColor("#ecb0c1")},
                {Color.parseColor("#e67762"), Color.parseColor("#d24735"), Color.parseColor("#b93a26"), Color.parseColor("#d12920")},
        };

        for (int i = 0; i < 7; i++) {
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < 4; j++) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        colorBlockSize,
                        colorBlockSize
                );
                layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.color_block_margin));
                layoutParams.bottomMargin = marginBottom; // 设置底部间距
                final LinearLayout colorBlockLayout = new LinearLayout(this);
                colorBlockLayout.setLayoutParams(layoutParams);
                colorBlockLayout.setBackgroundColor(colors[i][j]);
                colorBlockLayout.setGravity(Gravity.CENTER); // 将子元素置于中央

                TextView colorNameTextView = new TextView(this);
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                colorNameTextView.setLayoutParams(textViewParams);
                colorNameTextView.setText(colorNames[i * 4 + j]); // 根据颜色名称数组设置颜色名字
                colorNameTextView.setTextColor(Color.WHITE); // 设置颜色名字颜色为白色

                colorBlockLayout.addView(colorNameTextView); // 将颜色名字添加到颜色块布局中

                // 添加点击事件监听器
                colorBlockLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 获取点击的颜色
                        int selectedColor = ((ColorDrawable) colorBlockLayout.getBackground()).getColor();
                        Log.d("CXL", "Selected color: " + selectedColor);

                        // 更新全局主题颜色
                        ThemeManager.getInstance(getApplicationContext()).setCurrentThemeColor(selectedColor);

                        // 重启当前Activity以应用新主题
                        recreate();

                        // 上传主题颜色
                        uploadThemeColor(userId, String.format("#%06X", (0xFFFFFF & selectedColor)));
                    }
                });

                rowLayout.addView(colorBlockLayout);
            }

            linearLayoutColors.addView(rowLayout);
        }
    }

    private void uploadThemeColor(int userId, String newThemeColor) {
        // 发送更新昵称的网络请求
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", String.valueOf(userId));
            jsonBody.put("new_theme_color", newThemeColor);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(URL +"/upload_theme_color")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败
                Log.e("CXL", "Request failed: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThemeActivity.this, "网络请求超时或无法连接到服务器", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 处理服务器响应
                final String responseData = response.body().string();
                Log.d("CXL", "主题Response: " + responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String message = jsonObject.getString("message");
                            Log.d("CXL", "主题Response: " + responseData);
                            Log.d("CXL", "主题Message: " + message);
                            if (message.equals("主题颜色更新成功")) {
                                // 注册成功
                                Toast.makeText(ThemeActivity.this, "主题颜色上传成功", Toast.LENGTH_SHORT).show();
                                finish(); // 结束当前注册页面
                            } else {
                                // 注册失败
                                Toast.makeText(ThemeActivity.this, "主题颜色上传失败", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CXL", "JSON parsing error: " + response);
                            Toast.makeText(ThemeActivity.this, "服务器解析错误~", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
    }

}
