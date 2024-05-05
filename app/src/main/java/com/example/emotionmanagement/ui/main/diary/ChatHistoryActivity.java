package com.example.emotionmanagement.ui.main.diary;

import static com.example.emotionmanagement.MyApp.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emotionmanagement.R;


import android.util.Log;
import android.widget.Toast;

import com.example.emotionmanagement.util.DateAxisValueFormatter;
import com.example.emotionmanagement.util.ThemeManager;
import com.example.emotionmanagement.util.TodayDecorator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;


import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatHistoryActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;
    private MaterialCalendarView calendarView;
    private int userId;
    private LineChart lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_chat_history);
        SharedPreferences sharedPref = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        lineChart = findViewById(R.id.lineChart);


        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        // 添加今天的日期装饰
        int todayColor = Color.parseColor("#F03752"); // 直接从十六进制颜色代码解析得到颜色值
        calendarView.addDecorator(new TodayDecorator(todayColor));
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                Intent intent = new Intent(ChatHistoryActivity.this, ChatDayActivity.class);

                // 获取 LocalDate
                org.threeten.bp.LocalDate selectedDate = date.getDate();

                // 使用 DateTimeFormatter 格式化 LocalDate
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = selectedDate.format(formatter);

                intent.putExtra("SELECTED_DATE", formattedDate);
                Log.d("SELECTED_DATE", formattedDate);

                startActivity(intent);
            }
        });
        fetchSentimentResults();
        fetchSentimentScoreResults();


    }

    private void fetchSentimentScoreResults() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://172.20.10.3:5000/get_latest_daily_sentiment?user_id=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatHistoryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> updateLineChart(responseData));
                }
            }
        });
    }
    private void setupLineChart() {
        // 配置 X 轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 确保 X 轴在底部
        xAxis.setDrawGridLines(false); // 不显示网格线

        // 配置 Y 轴（左侧）
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // 不显示网格线

        // 配置 Y 轴（右侧）
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // 不显示右侧 Y 轴

        // 配置图表的其它部分
        lineChart.getLegend().setEnabled(false); // 不显示图例
        lineChart.getDescription().setEnabled(false); // 不显示描述标签
    }
    private void updateLineChart(String jsonData) {
        // 清除旧的图表数据
        lineChart.clear();

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        lineChart.getXAxis().setValueFormatter(new DateAxisValueFormatter(startDate));

        ArrayList<Entry> values = new ArrayList<>();
        try {
            JSONArray results = new JSONArray(jsonData);

            HashSet<String> dateSet = new HashSet<>(); // 用于检查重复日期
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String dateStr = result.getString("date");

                // 检查日期是否重复
                if (dateSet.add(dateStr)) {
                    float score = (float) result.getDouble("sentiment_score");
                    LocalDate localDate = LocalDate.parse(dateStr);
                    long dayOfYear = startDate.until(localDate, ChronoUnit.DAYS);
                    values.add(new Entry((float) dayOfYear, score));
                }
            }

            LineDataSet dataSet = new LineDataSet(values, "Sentiment Score");
            dataSet.setColor(Color.parseColor("#00828B"));
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate(); // 刷新图表
            setupLineChart(); // 重新设置图表
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing JSON for chart: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void fetchSentimentResults() {
        OkHttpClient client = new OkHttpClient();
        String url = URL + "/get_daily_sentiment?user_id=" + userId;  // Adjust URL as needed

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatHistoryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> updateCalendarDecorations(responseData));
                }
            }
        });
    }

    private void updateCalendarDecorations(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray results = jsonObject.getJSONArray("daily_sentiments");

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String dateStr = result.getString("date");
                String sentiment = result.getString("sentiment");
                CalendarDay day = CalendarDay.from(LocalDate.parse(dateStr));
                int drawableRes = getDrawableResForSentiment(sentiment);
                calendarView.addDecorator(new MyDayDecorator(this, day, drawableRes));
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private int getDrawableResForSentiment(String sentiment) {
        Random random = new Random();
        if (sentiment.equals("positive")) {
            int[] drawables = {R.drawable.ic_emo_postive1, R.drawable.ic_emo_postive2, R.drawable.ic_emo_postive3, R.drawable.ic_emo_postive4};
            return drawables[random.nextInt(drawables.length)];
        } else if (sentiment.equals("negative")) {
            int[] drawables = {R.drawable.ic_emo_negetive1, R.drawable.ic_emo_negetive2, R.drawable.ic_emo_negetive3, R.drawable.ic_emo_negetive4};
            return drawables[random.nextInt(drawables.length)];
        }
        return R.drawable.ic_emo_postive1;
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
