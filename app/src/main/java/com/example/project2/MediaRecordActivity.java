package com.example.project2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MediaRecordActivity extends AppCompatActivity {

    private TextureView textureView;
    private Button bt_record;
    private MediaRecorder mediaRecorder;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);
        textureView = findViewById(R.id.textureView);
        bt_record = findViewById(R.id.bt_record);

        bt_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence text = bt_record.getText();
                if (TextUtils.equals(text, "start")){
                    bt_record.setText("stop");

                    camera = Camera.open();
                    camera.setDisplayOrientation(90);
                    camera.unlock();


                    mediaRecorder = new MediaRecorder();
                    //将修正角度后的摄像头给mediarecorder
                    mediaRecorder.setCamera(camera);
                    //设置音频：麦克风
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    //设置视频：摄像头
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    //设置视频输出格式:mp4
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    //设置音频编码格式
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    //设置视频编码格式
                    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                    //修正视频角度
                    mediaRecorder.setOrientationHint(90);
                    //设置视频文件输出文件
                    mediaRecorder.setOutputFile(new File(getExternalFilesDir(""), "a.mp4").getAbsolutePath());
                    //设置视频大小
                    mediaRecorder.setVideoSize(640, 480);
                    //设置预览画布
                    mediaRecorder.setPreviewDisplay(new Surface(textureView.getSurfaceTexture()));
                    //prepare准备录制
                    try {
                        mediaRecorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //开始录制
                    mediaRecorder.start();
                }else {
                    bt_record.setText("start");
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    camera.stopPreview();
                    camera.release();
                }
            }
        });
    }



}