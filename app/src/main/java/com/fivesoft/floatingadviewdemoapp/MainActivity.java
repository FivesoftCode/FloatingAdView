package com.fivesoft.floatingadviewdemoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.fivesoft.adview.FloatingAdView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingAdView adView = findViewById(R.id.adView);

        findViewById(R.id.disable_ads).setOnClickListener(v -> {
            adView.setAdsEnabled(false);
        });

        findViewById(R.id.enable_ads).setOnClickListener(v -> {
            adView.setAdsEnabled(true);
        });

    }
}