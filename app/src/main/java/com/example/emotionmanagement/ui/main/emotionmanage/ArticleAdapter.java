package com.example.emotionmanagement.ui.main.emotionmanage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotionmanagement.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> articles;
    private Context context;
    private int userId;


    public ArticleAdapter(List<Article> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    // 设置文章列表
    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged(); // 通知适配器数据集已更改
    }

    // 添加文章到现有列表
    public void addArticles(List<Article> newArticles) {
        if (newArticles != null) {
            articles.addAll(newArticles);
            notifyDataSetChanged();
        }
    }

    public void clearArticles() {
        this.articles.clear();
        notifyDataSetChanged();  // 通知数据变化，刷新界面
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Article article = articles.get(position);
        SharedPreferences sharedPref = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        holder.textTitle.setText(article.getTitle());
        holder.textContent.setText(article.getShortenedContent()); // 使用截取后的内容
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("url", article.getUrl());
                context.startActivity(intent);
                incrementViews(article.getResourceId(), position, holder);
            }
        });

        holder.textViews.setText(String.valueOf(article.getViews()));
        holder.imageFavorite.setImageResource(article.isFavorite() ? R.drawable.ic_is_favorite_outline : R.drawable.ic_not_favorite_outline);
        holder.imageFavorite.setOnClickListener(v -> {
            toggleFavorite(article, position, holder);
        });
        // 使用 Glide 加载图片
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            String imageUrl = article.getImageUrl();
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = "http://" + imageUrl; // 确保 URL 是完整的
            }
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.imageArticle);
        }
    }

    private void toggleFavorite(Article article, int position, ViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("resource_id", article.getResourceId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/toggle_favorite")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String message = jsonResponse.optString("message");
                        if (message.equals("点赞成功") || message.equals("取消点赞成功")) {
                            article.toggleFavorite();  // 更新点赞状态
                            if (context instanceof Activity) {
                                ((Activity) context).runOnUiThread(() -> {
                                    holder.imageFavorite.setImageResource(article.isFavorite() ? R.drawable.ic_is_favorite_outline : R.drawable.ic_not_favorite_outline);
                                    notifyItemChanged(position);
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void incrementViews(int resourceId, int position, ViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("resource_id", resourceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/increment_views")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    int updatedCount = 0;
                    updatedCount = articles.get(position).getViews() + 1;

                    final int finalUpdatedCount = updatedCount;
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            Article article = articles.get(position);
                            article.setViews(finalUpdatedCount); // 更新模型数据
                            holder.textViews.setText(String.valueOf(finalUpdatedCount)); // 更新视图
                            notifyItemChanged(position); // 仅刷新修改的项
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textTitle;
        public TextView textContent;
        public ImageView imageArticle;
        public ImageView imageViews;
        public TextView textViews;
        public ImageView imageFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textContent = itemView.findViewById(R.id.text_content);
            imageArticle = itemView.findViewById(R.id.image_article);
            imageViews = itemView.findViewById(R.id.image_views);
            textViews = itemView.findViewById(R.id.text_views);
            imageFavorite = itemView.findViewById(R.id.image_favorite);
        }
    }
}
