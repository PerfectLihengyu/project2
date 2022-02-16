package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CutVideoActivity extends AppCompatActivity {

    private EditText et_inputEndTime;
    private EditText et_inputStartTime;
    private Button bt_submitCutVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_video);

        et_inputStartTime = findViewById(R.id.et_inputStartTime);
        et_inputEndTime = findViewById(R.id.et_inputEndTime);
        bt_submitCutVideo = findViewById(R.id.bt_submitCutVideo);

        bt_submitCutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputStartText = et_inputStartTime.getText().toString();
                String inputEndText = et_inputEndTime.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("startTime", Integer.parseInt(inputStartText));
                intent.putExtra("endTime", Integer.parseInt(inputEndText));
                setResult(1, intent);
                finish();
            }
        });


    }
}