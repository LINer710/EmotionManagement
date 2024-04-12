package com.example.emotionmanagement.ui.main;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.emotionmanagement.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_diary, R.id.navigation_emotionManage, R.id.navigation_personalCenter
        ).build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Log.d("CXL", "NavController ID: " + navController.toString());
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }

}
