package com.example.emotionmanagement.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.emotionmanagement.R;
import com.example.emotionmanagement.util.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
        setContentView(R.layout.activity_main);
        // 使状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_diary, R.id.navigation_emotionManage, R.id.navigation_personalCenter
        ).build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Log.d("CXL", "NavController ID: " + navController.toString());
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setBackgroundDrawable(new ColorDrawable(ThemeManager.getInstance(getApplicationContext()).getCurrentThemeColor()));
    }

}
