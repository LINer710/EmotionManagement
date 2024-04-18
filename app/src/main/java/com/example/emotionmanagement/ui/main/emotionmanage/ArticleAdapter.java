package com.example.emotionmanagement.ui.main.emotionmanage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotionmanagement.R;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> articles;
    private Context context;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.textTitle.setText(article.getTitle());
        holder.textContent.setText(article.getShortenedContent()); // 使用截取后的内容
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("url", article.getUrl());
                context.startActivity(intent);
            }
        });


        holder.textViews.setText(String.valueOf(article.getViews()));
        holder.imageFavorite.setImageResource(article.isFavorite() ? R.drawable.ic_favorite_outline : R.drawable.ic_no_favorite_outline);

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
