package com.example.emotionmanagement.ui.main.fragment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.emotionmanagement.MyApp;
import com.example.emotionmanagement.R;
import com.example.emotionmanagement.ui.login.LoginActivity;
import com.example.emotionmanagement.ui.usercenter.ChangePasswordActivity;
import com.example.emotionmanagement.ui.usercenter.PrivacyPolicyActivity;
import com.example.emotionmanagement.ui.usercenter.ThemeActivity;
import com.example.emotionmanagement.ui.usercenter.UserAgreementActivity;
import com.example.emotionmanagement.util.DebounceUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
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
    private ImageView logoutButton;

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
        logoutButton = rootView.findViewById(R.id.logout_button);

        RelativeLayout changePasswordLayout = rootView.findViewById(R.id.change_password);
        RelativeLayout changeThemeLayout = rootView.findViewById(R.id.change_theme);
        RelativeLayout changeFontSizeLayout = rootView.findViewById(R.id.change_font_style);
        RelativeLayout userAgreementLayout = rootView.findViewById(R.id.user_agreement);
        RelativeLayout privacyPolicyLayout = rootView.findViewById(R.id.privacy_policy);
        RelativeLayout logoutLayout = rootView.findViewById(R.id.cancel_account);

        // 从本地加载用户信息
        loadLocalUserInfo();
        // 设置点击事件监听器

        // 在logoutLayout的点击事件中添加注销账号功能
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建密码输入对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("确认注销");
                builder.setMessage("请输入密码以确认注销账号：");

                // 设置密码输入框
                final EditText inputPassword = new EditText(requireContext());
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(inputPassword);

                // 添加确认按钮点击事件
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取用户输入的密码
                        String password = inputPassword.getText().toString().trim();

                        // 向服务器发送注销账号请求
                        OkHttpClient client = new OkHttpClient();

                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("user_id", userId);
                            jsonBody.put("password_hash", (password));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));

                        Request request = new Request.Builder()
                                .url("http://192.168.68.170:5000/delete_account")
                                .post(requestBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(requireContext(), "无法连接到服务器", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                            // 在注销账号成功后清除本地持久化数据
                                            SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_info", requireContext().MODE_PRIVATE).edit();
                                            editor.clear();
                                            editor.apply();

                                            // 如果账号注销成功，跳转到登录界面或者执行其他操作
                                            if ("账号注销成功".equals(message)) {
                                                // 跳转到登录界面
                                                Intent intent = new Intent(requireContext(), LoginActivity.class);
                                                startActivity(intent);
                                                requireActivity().finish();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(requireContext(), "服务器返回数据格式错误", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

                // 添加取消按钮点击事件
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消操作，关闭对话框
                        dialog.dismiss();
                    }
                });

                // 显示对话框
                builder.show();
            }
        });


        privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        userAgreementLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserAgreementActivity.class);
                startActivity(intent);
            }
        });
        changeFontSizeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建PopupMenu对象
                PopupMenu popupMenu = new PopupMenu(requireContext(), v);

                // 加载菜单项布局
                popupMenu.getMenuInflater().inflate(R.menu.font_size_menu, popupMenu.getMenu());

                // 设置菜单项点击事件监听器
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        MyApp myApp = (MyApp) getActivity().getApplication();
                        int itemId = item.getItemId();
                        if (itemId == R.id.kaiti_font) {
                            MyApp.setCustomFont("fonts/kaiti.ttf");
                        } else if (itemId == R.id.shouxieti_font) {
                            MyApp.setCustomFont("fonts/shouxieti.ttf");
                        } else if (itemId == R.id.shufati_font) {
                            MyApp.setCustomFont("fonts/shufati.ttf");
                        } else if (itemId == R.id.songti_font) {
                            MyApp.setCustomFont("fonts/songti.ttf");
                        } else if (itemId == R.id.xingti_font) {
                            MyApp.setCustomFont("fonts/xingti.otf");
                        } else if (itemId == R.id.yaunti_font) {
                            MyApp.setCustomFont("fonts/yuanti.ttf");
                        }
                        updateAllFonts();
                        return true;
                    }
                });

                // 显示PopupMenu
                popupMenu.show();
            }
        });

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

        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
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
        changeThemeLayout.setOnClickListener(new View.OnClickListener() {
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
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("确认退出");
                builder.setMessage("确定要退出登录吗？");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 跳转到登录界面
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });


        return rootView;
    }

    private void updateAllFonts() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        updateFontsRecursively(rootView);
    }

    private void updateFontsRecursively(ViewGroup rootView) {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            View child = rootView.getChildAt(i);
            if (child instanceof ViewGroup) {
                updateFontsRecursively((ViewGroup) child);
            } else if (child instanceof TextView) {
                ((TextView) child).setTypeface(MyApp.getCustomFont());
            }
        }
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
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                                Log.d("CXL", avatarUrl);

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
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
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

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // 使用 JPEG 格式压缩，100表示不压缩
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = getActivity().getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);
        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {

                Bitmap bitmap = getBitmapFromUri(selectedImage);
                String imageBase64 = "data:image/jpg;base64," +encodeImageToBase64(bitmap);
                Glide.with(this)
                        .load(imageBase64)
                        .apply(new RequestOptions().circleCrop())
                        .into(profileImageView);
                // 将新头像URL上传到服务器
                updateAvatar(imageBase64);  // 上传 Base64 字符串到服务器
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
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
        if (userId == -1) {
            Toast.makeText(requireContext(), "无法获取用户ID", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("new_avatar", newAvatarUrl);
        } catch (JSONException e) {
            Toast.makeText(requireContext(), "创建请求数据时出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url("http://192.168.68.170:5000/update_avatar")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "服务器错误：" + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseData = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String message = jsonObject.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        if (message.equals("头像更新成功")) {
                            loadUserAvatar();  // 假设这是你实现的一个方法来重新加载用户的头像
                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "响应解析失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        });
    }


}
