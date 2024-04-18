package com.example.emotionmanagement.ui.main.emotionmanage;

public class Article {

    private int resourceId;
    private String title;
    private String category;
    private String content;
    private int views;
    private int favorites;
    private String imageUrl; // 如果 imageUrl 是资源标识符，应该是整数类型
    private String url; // webview url


    // 构造函数
    public Article(int resourceId, String title, String category, String content, int views, int favorites, String imageUrl, String url) {
        this.resourceId = resourceId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.views = views;
        this.favorites = favorites;
        this.imageUrl = imageUrl;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    // Getter 和 Setter 方法
    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 判断是否被收藏的方法
    public boolean isFavorite() {
        return favorites > 0; // 如果 favorites 大于 0，则表示被收藏
    }

    public String getShortenedContent() {
        if (content.length() <= 30) {
            return content;
        } else {
            return content.substring(0, 30) + "...";
        }
    }

}
