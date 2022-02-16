package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.project2.utils.TwoImageSimilarUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //手动申请权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 100);
    }

    public void record(View view) {
        startActivity(new Intent(this, MediaRecordActivity.class));
    }

    public void monitor(View view) {
        startActivity(new Intent(this, MonitorActivity.class));
    }

    public void takeAPhoto(View view) {
        startActivity(new Intent(this, TakeAPhotoActivity.class));
    }
}