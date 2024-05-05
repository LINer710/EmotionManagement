package com.example.emotionmanagement.ui.main.diary;

import static com.example.emotionmanagement.MyApp.URL;
import static com.example.emotionmanagement.ui.main.diary.ChatAdapter.KEY_USER_MESSAGES;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.util.ThemeManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatSemanticAnalysisActivity extends AppCompatActivity {
    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_chat_semantic_analysis);
        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);
        SharedPreferences sharedPreferences = getSharedPreferences("diary_messages", Context.MODE_PRIVATE);


        // 获取当日的用户消息并进行情绪分析
        fetchTodayUserMessagesAndAnalyze(sharedPreferences, userId);
        chart = findViewById(R.id.chart);
        setupBarChart();
    }

    /**
     * 设置条形图的配置。
     * 该方法不接受参数，也不返回任何值。
     * 主要配置包括：禁用描述和图例，设置x轴的位置、不绘制网格线、设置刻度间隔和标签数量，
     * 并且为x轴设置自定义格式器。
     */
    private void setupBarChart() {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(2);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(new String[]{"Positive", "Negative"}));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // start at zero
        leftAxis.setAxisMaximum(100f); // the axis maximum is 100
        leftAxis.setGranularity(10f); // interval count
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // no right axis
    }




    private void fetchTodayUserMessagesAndAnalyze(SharedPreferences sharedPreferences, int userId) {
        String userMessagesJson = sharedPreferences.getString(KEY_USER_MESSAGES, null);
        Log.d("CXL", "userMessagesJson: " + userMessagesJson);
        List<String> todayMessages = new ArrayList<>();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("CXL", "todayDate: " + todayDate);

        if (userMessagesJson != null) {
            try {
                JSONArray userMessagesArray = new JSONArray(userMessagesJson);
                for (int i = 0; i < userMessagesArray.length(); i++) {
                    JSONObject messageObject = userMessagesArray.getJSONObject(i);
                    String dateTime = messageObject.getString("dateTime").split(" ")[0]; // Assuming dateTime format is "yyyy-MM-dd HH:mm:ss"
                    Log.d("CXL", "dateTime: " + dateTime);
                    if (todayDate.equals(dateTime)) {
                        todayMessages.add(messageObject.getString("text"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Join all today's messages into a single string for analysis
        String allTodayMessages = TextUtils.join(" ", todayMessages);
        Log.d("CXL", "allTodayMessages"+allTodayMessages);
        if (!allTodayMessages.isEmpty()) {
            fetchSentimentAnalysis(String.valueOf(userId), allTodayMessages);
        }
    }

    private void fetchSentimentAnalysis(String userId, String text) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("user_id", userId);
            jsonData.put("text", text);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData.toString());
            Request request = new Request.Builder()
                    .url(URL+"/analyze_sentiment") // 根据你的服务器地址进行修改
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    // Handle the error
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();
                        ChatSemanticAnalysisActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonResponse = new JSONObject(myResponse);
                                    double sentimentScore = jsonResponse.getDouble("sentiment_score") * 100;
                                    updateUI(sentimentScore); // Update your UI here based on the sentiment score
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(double score) {
        TextView tvScore = findViewById(R.id.tvScore);
        tvScore.setText(String.valueOf((int) score) + "分");

        CircularProgressIndicator progressIndicator = findViewById(R.id.circularProgressIndicator);
        progressIndicator.setIndeterminate(false);
        progressIndicator.setProgressCompat((int) score, true);

        if (score < 50) {
            progressIndicator.setIndicatorColor(Color.parseColor("#F03752"));
        } else {
            progressIndicator.setIndicatorColor(Color.parseColor("#00828B"));
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) score));
        entries.add(new BarEntry(1f, 100f - (float) score));

        BarDataSet dataSet = new BarDataSet(entries, "Sentiment");
        dataSet.setColors(new int[]{
                Color.parseColor("#00828B"),
                Color.parseColor("#F03752")
        });
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.2f); // set custom bar width

        chart.setData(data);
        chart.invalidate(); // refresh the chart
    }


}