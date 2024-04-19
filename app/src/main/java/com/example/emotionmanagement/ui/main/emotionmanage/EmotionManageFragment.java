package com.example.emotionmanagement.ui.main.emotionmanage;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotionmanagement.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EmotionManageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalPage = 3; // 假设总共有5页，这个值应由服务器提供
    private String currentCategory; // 当前文章分类
    private int userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_emotion_manage, container, false);

        // 获取菜单栏的 LinearLayout
        LinearLayout menuContainer = rootView.findViewById(R.id.menu_container);
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);

        // 菜单项文本数组
        String[] menuItems = {"精选", "亲子关系", "亲密关系", "人际关系", "咨询小科普",
                "家庭关系", "情绪压力", "个人成长", "我的收藏"};


        // 动态添加菜单项
        for (String menuItem : menuItems) {
            TextView textView = new TextView(getContext());
            textView.setText(menuItem);
            textView.setTextSize(16);
            textView.setPadding(16, 0, 16, 0);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 恢复其他菜单项的字体样式
                    for (int i = 0; i < menuContainer.getChildCount(); i++) {
                        TextView child = (TextView) menuContainer.getChildAt(i);
                        child.setTextSize(16);
                        child.setTypeface(null, Typeface.NORMAL);
                    }

                    // 设置点击的菜单项的字体样式为放大加粗
                    TextView selectedTextView = (TextView) v;
                    selectedTextView.setTextSize(18);
                    selectedTextView.setTypeface(null, Typeface.BOLD);

                    // 更新当前分类
                    currentCategory = selectedTextView.getText().toString();

                    // 更新 RecyclerView 的内容
                    updateRecyclerView(currentCategory);
                }
            });
            menuContainer.addView(textView);
        }

        // 初始化 RecyclerView
        recyclerView = rootView.findViewById(R.id.article_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int totalItemCount = layoutManager.getItemCount();
                    int visibleItemCount = layoutManager.getChildCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= visibleItemCount) {
                            loadMoreItems();
                        }
                    }
                }
            }

            private void loadMoreItems() {
                if (isLoading || isLastPage) {
                    return; // 如果正在加载或已经是最后一页，则直接返回
                }
                Log.d("CXL", "下一页");
                isLoading = true;
                new LoadArticlesTask().execute(currentCategory); // 加载下一页
            }

        });


        // 加载第一个菜单项的文章内容
        updateRecyclerView(menuItems[0]);

        return rootView;

    }


    // 根据菜单项更新 RecyclerView 中的文章内容
    private void updateRecyclerView(String category) {
        // 重置分页和加载状态
        currentPage = 1;
        isLoading = false;
        isLastPage = false;

        // 清空现有数据
        adapter.clearArticles();

        // 根据类别重新加载数据，特别处理"我的收藏"
        if (category.equals("我的收藏")) {
            new LoadFavoritesTask().execute(); // 加载收藏的文章
        } else {
            new LoadArticlesTask().execute(category); // 加载其他分类的文章
        }
    }


    // 异步任务，用于加载指定分类下的文章数据
    private class LoadArticlesTask extends AsyncTask<String, Void, List<Article>> {

        @Override
        protected List<Article> doInBackground(String... params) {
            String category = params[0];
            // 构建请求 URL
            String baseUrl = "http://192.168.68.170:5000";
            String url = baseUrl + "/articles?category=" + category + "&user_id=" + userId + "&page=" + currentPage;


            // 发起网络请求获取文章数据
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray jsonArray = jsonObject.getJSONArray("articles");
                    totalPage = jsonObject.getInt("total_pages");
                    List<Article> articles = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray item = jsonArray.getJSONArray(i);
                        int resourceId = item.getInt(0);
                        String title = item.getString(1);
                        String content = item.getString(3).trim(); // 移除无效的字符
                        int views = item.getInt(4);
                        int favorites = item.getInt(5);
                        String imageUrl = item.getString(6).startsWith("http") ? item.getString(6) : "http:" + item.getString(6);
                        String linkUrl = item.getString(7);
                        Article article = new Article(resourceId, title, category, content, views, favorites, imageUrl, linkUrl);
                        articles.add(article);
                        Log.d("CXL", "文章请求成功：" + article);
                    }
                    return articles;
                } else {
                    Log.d("CXL", "文章请求失败：" + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return new ArrayList<>(); // 返回空列表表示加载失败
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            super.onPostExecute(articles);
            if (articles != null && !articles.isEmpty()) {
                adapter.addArticles(articles); // 添加文章到现有列表
                adapter.notifyDataSetChanged();
                currentPage++;
                if (currentPage > totalPage) {
                    isLastPage = true;
                }
            }
            isLoading = false;
        }
    }

    private class LoadFavoritesTask extends AsyncTask<Void, Void, List<Article>> {
        @Override
        protected List<Article> doInBackground(Void... voids) {
            String baseUrl = "http://192.168.68.170:5000";
            String url = baseUrl + "/get_favorites?user_id=" + userId;
            Log.d("CXL", "收藏请求：" + url);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray jsonArray = jsonObject.getJSONArray("favorites");
                    Log.d("CXL", "收藏请求成功：" + jsonArray);
                    List<Article> articles = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        Article article = parseArticle(item);
                        articles.add(article);
                    }
                    return articles;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.d("CXL", "收藏请求失败：" + e.getMessage());
            }
            return new ArrayList<>(); // 返回空列表表示加载失败
        }


        @Override
        protected void onPostExecute(List<Article> articles) {
            super.onPostExecute(articles);
            if (articles != null && !articles.isEmpty()) {
                adapter.addArticles(articles);
                adapter.notifyDataSetChanged();
            }
            isLoading = false;
        }
    }

    private Article parseArticle(JSONObject item) throws JSONException {
        int resourceId = item.getInt("resource_id");
        String title = item.getString("title");
        String content = item.getString("content");
        String category = item.getString("category");
        int views = item.getInt("views");
        int favorites = item.getInt("favorites");
        String imageUrl = item.getString("image_url");
        String url = item.getString("article_link");

        return new Article(resourceId, title, category, content, views, favorites, imageUrl, url);
    }

}
