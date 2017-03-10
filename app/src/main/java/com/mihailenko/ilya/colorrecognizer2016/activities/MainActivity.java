package com.mihailenko.ilya.colorrecognizer2016.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.activities.viewmodels.MainActivityViewModel;
import com.mihailenko.ilya.colorrecognizer2016.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainActivityViewModel {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(this);
    }

    @Override
    public void onSurfClick(View view) {
        startActivity(new Intent(this, SurfActivity.class));
    }

    @Override
    public void onCameraClick(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }
}
