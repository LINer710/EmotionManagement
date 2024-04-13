package com.example.emotionmanagement.ui.main.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.emotionmanagement.R;
import com.example.emotionmanagement.ui.usercenter.ChangePasswordActivity;
import com.example.emotionmanagement.ui.usercenter.ThemeActivity;
import com.example.emotionmanagement.util.DebounceUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonalCenterFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234; // 图片选择的请求码

    private ImageView profileImageView;
    private TextView nicknameTextView;
    private int userId;
    private String currentNickname;

    public PersonalCenterFragment() {
    }

    public static PersonalCenterFragment newInstance() {
        return new PersonalCenterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_personal_center, container, false);

        // 初始化头像 ImageView
        profileImageView = rootView.findViewById(R.id.profile_image);
        // 初始化昵称 TextView
        nicknameTextView = rootView.findViewById(R.id.nickname);

        RelativeLayout changePasswordImageView = rootView.findViewById(R.id.change_password);
        RelativeLayout changeThemeImageView = rootView.findViewById(R.id.change_theme);

        // 从本地加载用户信息
        loadLocalUserInfo();

        // 设置昵称点击事件
        nicknameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebounceUtils.debounce(new Runnable() {
                    public void run() {
                        // 显示昵称修改对话框
                        showChangeNicknameDialog();
                    }
                });
            }
        });

        // 设置头像点击事件
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebounceUtils.debounce(new Runnable() {
                    public void run() {
                        // 显示头像修改对话框
                        openImageChooser();
                    }
                });
            }
        });

        changePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebounceUtils.debounce(new Runnable() {
                    public void run() {
                        // 显示密码修改对话框
                        Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
        changeThemeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebounceUtils.debounce(new Runnable() {
                    public void run() {
                        // 显示密码修改对话框
                        Intent intent = new Intent(requireContext(), ThemeActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        return rootView;
    }

    /**
     * 从本地加载用户信息
     */
    private void loadLocalUserInfo() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);
        currentNickname = sharedPref.getString("nickname", "");

        if (userId != -1) {
            if (!TextUtils.isEmpty(currentNickname)) {
                // 如果本地存在昵称，直接显示
                nicknameTextView.setText(currentNickname);
            } else {
                // 否则显示默认昵称
                nicknameTextView.setText("点击设置默认昵称");
            }

            String avatarUrl = sharedPref.getString("avatar_url", "");
            Log.d("CXL", "个人中心本地头像" + avatarUrl);

            if (!TextUtils.isEmpty(avatarUrl)) {
                // 如果本地存在头像URL，直接加载
                Glide.with(requireContext())
                        .load(avatarUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView);
            } else {
                // 否则显示默认头像
                profileImageView.setImageResource(R.drawable.img_logo);

            }
            loadUserAvatar();
        }
    }


    /**
     * 显示修改昵称对话框
     */
    private void showChangeNicknameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("修改昵称");

        // 设置对话框视图
        View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_nickname, null);
        final EditText inputNickname = viewInflated.findViewById(R.id.input_nickname);
        builder.setView(viewInflated);

        // 设置对话框按钮
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取用户输入的新昵称
                String newNickname = inputNickname.getText().toString().trim();
                if (!TextUtils.isEmpty(newNickname)) {
                    updateNickname(newNickname); // 更新昵称
                } else {
                    Toast.makeText(requireContext(), "昵称不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 取消对话框
            }
        });

        builder.show(); // 显示对话框
    }


    /**
     * 更新昵称
     *
     * @param newNickname
     */
    private void updateNickname(String newNickname) {
        // 发送更新昵称的网络请求
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", String.valueOf(userId));
            jsonBody.put("new_nickname", newNickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/update_nickname")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "无法更新用户昵称", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String message = jsonObject.optString("message", "");
                            if ("昵称更新成功".equals(message)) {
                                currentNickname = newNickname;
                                nicknameTextView.setText(newNickname);

                                // 保存新昵称到本地
                                SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE).edit();
                                editor.putString("nickname", newNickname);
                                editor.apply();
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "无法更新用户昵称", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    /**
     * 加载用户头像
     */
    private void loadUserAvatar() {
        if (userId != -1) {
            String avatarUrl = "http://192.168.68.170:5000/get_avatar/" + userId;
            OkHttpClient client = new OkHttpClient();

            // 构建 GET 请求
            Request request = new Request.Builder()
                    .url(avatarUrl)
                    .build();

            // 异步执行请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // 处理请求失败
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 加载用户头像失败，设置默认头像并显示提示信息
                            profileImageView.setImageResource(R.drawable.img_logo);
                            Toast.makeText(requireContext(), "无法加载用户头像，已设置默认头像", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 处理服务器响应
                    final String responseData = response.body().string();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 解析服务器响应
                                // 这里假设返回的 JSON 数据格式为 {"avatar": "http://example.com/avatar.jpg"}
                                JSONObject jsonObject = new JSONObject(responseData);
                                String avatarUrl = jsonObject.optString("avatar", "");
                         /*       Log.d("CXL", "结果" + jsonObject);
                                Log.d("CXL", "头像" + avatarUrl);
                                Log.d("CXL", "isEmpty" + avatarUrl.isEmpty());*/

                                if (avatarUrl == null || avatarUrl.isEmpty() || "null".equals(avatarUrl)) {
                                /*    Log.d("CXL", "头像为空或为null");
                                    Log.d("CXL", "头像为空");*/
                                    profileImageView.setImageResource(R.drawable.img_logo);
                                    Toast.makeText(requireContext(), "用户头像为空，已设置默认头像", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 使用 Glide 加载头像
                                    Glide.with(requireContext())
                                            .load(avatarUrl)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(profileImageView);
                                    // 本地存储
                                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE).edit();
                                    editor.putString("avatar_url", avatarUrl);
                                    editor.apply();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(requireContext(), "无法加载用户头像", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else {
            // 用户ID不存在，无法加载头像，设置默认头像并显示提示信息
            profileImageView.setImageResource(R.drawable.img_logo);
            Toast.makeText(requireContext(), "用户ID不存在，已设置默认头像", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 打开图库选择图片
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            // 使用 Glide 加载图片，并裁剪为圆形
            Glide.with(this)
                    .load(selectedImage)
                    .apply(new RequestOptions().circleCrop())
                    .into(profileImageView);
            // 将新头像URL上传到服务器
            updateAvatar(selectedImage.toString());
        } else {
            Toast.makeText(getActivity(), "图片选择已取消", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新用户头像
     *
     * @param newAvatarUrl
     */
    private void updateAvatar(String newAvatarUrl) {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);
        if (userId != -1) {
            OkHttpClient client = new OkHttpClient();

            // 构建请求体，包含用户ID和新头像URL
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("user_id", userId);
                jsonBody.put("new_avatar", newAvatarUrl);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

            // 构建 POST 请求
            Request request = new Request.Builder()
                    .url("http://192.168.68.170:5000/update_avatar")
                    .post(requestBody)
                    .build();

            // 异步执行请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // 处理请求失败
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "无法更新用户头像", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 处理服务器响应
                    final String responseData = response.body().string();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(responseData);
                                String message = jsonObject.getString("message");
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                // 如果头像更新成功，重新加载用户头像
                                if (message.equals("头像更新成功")) {
                                    loadUserAvatar();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(requireContext(), "无法更新用户头像", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

}
