package com.example.emotionmanagement.ui.main.diary;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotionmanagement.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_SERVER = 2;
    private List<String> messages;
    private String lastServerMessage = "";
    private String userAvatarUrl = "content://media/external_primary/images/media/1000002699"; // 默认头像 URL
    private String userNickname = "用户"; // 默认昵称
    public static final String KEY_USER_MESSAGES = "user_messages";
    public static final String KEY_SERVER_MESSAGES = "server_messages";
    private RecyclerView recyclerView; // RecyclerView 的引用


    public ChatAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.messages = new ArrayList<>();
    }

    public static int getViewTypeUser() {
        return VIEW_TYPE_USER;
    }

    public static int getViewTypeServer() {
        return VIEW_TYPE_SERVER;
    }


    public void addMessage(String message, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            messages.add("User: " + message);
        } else if (viewType == VIEW_TYPE_SERVER) {
//
            messages.add("Server: " + message);

        }
        notifyItemInserted(messages.size() - 1);

        // 滚动到最新消息
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }


    private int lastServerMessageIndex = -1; // 初始化为-1，表示没有服务器消息

    public void appendServerMessage(String message) {
        if (!messages.isEmpty() && lastServerMessageIndex == messages.size() - 1) {
            String updatedMessage = messages.get(lastServerMessageIndex) + message;
            messages.set(lastServerMessageIndex, updatedMessage);
            notifyItemChanged(lastServerMessageIndex);
        } else {
            messages.add("Server: " + message);
            lastServerMessageIndex = messages.size() - 1;
            notifyItemInserted(lastServerMessageIndex);
        }
    }


    public List<String> getUserMessages() {
        List<String> userMessages = new ArrayList<>();
        for (String message : messages) {
            if (message.startsWith("User:")) {
                userMessages.add(message.substring(6));
            }
        }
        return userMessages;
    }

    public List<String> getServerMessages() {
        List<String> serverMessages = new ArrayList<>();
        for (String message : messages) {
            if (message.startsWith("Server:")) {
                serverMessages.add(message.substring(8));
            }
        }
        return serverMessages;
    }

    private void updateMessage(String message) {
        Log.d("CXL", "Updating message: " + message);
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).startsWith("User:")) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_SERVER;
        }
    }

    // 设置用户头像
    public void setUserAvatar(String avatarUrl) {
        this.userAvatarUrl = avatarUrl;
        Log.d("CXL", "setUserAvatar: " + avatarUrl);
        notifyDataSetChanged(); // Optional: update only relevant items
    }

    // 设置用户昵称
    public void setUserNickname(String nickname) {
        this.userNickname = nickname;
        Log.d("CXL", "setUserNickname: " + nickname);
        notifyDataSetChanged(); // Optional: update only relevant items
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_server_message, parent, false);
            return new ServerViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message.substring(6), userNickname, userAvatarUrl);
        } else if (holder instanceof ServerViewHolder) {
            ((ServerViewHolder) holder).bind(message.substring(8));
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

//    static class UserViewHolder extends RecyclerView.ViewHolder {
//        private TextView textView;
//
//        public UserViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textView = itemView.findViewById(R.id.textMessageUser);
//        }
//
//        public void bind(String message) {
//            textView.setText(message);
//        }
//    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView nicknameView;
        private ImageView avatarView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMessageUser);
            nicknameView = itemView.findViewById(R.id.textUserName);
            avatarView = itemView.findViewById(R.id.imageViewUserAvatar);
        }

        public void bind(String message, String nickname, String avatarUrl) {
            textView.setText(message);
            nicknameView.setText(nickname);
            Log.d("CXL", "bind: " + avatarUrl);
            Log.d("CXL", "bind: " + itemView.getContext());

            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground) // 确保你有这样的资源
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideError", "Load failed", e);
                            return false; // 表示异常没有被处理，让Glide显示error占位图
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("GlideSuccess", "Resource ready");
                            return false; // 表示事件没有被处理，让Glide继续执行其默认操作
                        }
                    })
                    .into(avatarView);

        }
    }

    static class ServerViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMessageServer);
        }

        public void bind(String message) {
            textView.setText(message);
        }
    }
}
