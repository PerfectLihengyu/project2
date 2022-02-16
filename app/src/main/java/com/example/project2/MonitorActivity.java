package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.project2.utils.ImageUtils;
import com.example.project2.utils.VideoUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MonitorActivity extends AppCompatActivity {
    private Button bt_monitor;
    private MediaRecorder mediaRecorder;
    private Camera camera;
    private SurfaceView monitorSurfaceView;

    private int wantedStartTime = 0;
    private int wantedEndTime = 0;
    private final int wantedCrossTime = 3;
    private int duringTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        bt_monitor = findViewById(R.id.bt_monitor);
        monitorSurfaceView = findViewById(R.id.monitorSurfaceView);

        bt_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence text = bt_monitor.getText();
                if (TextUtils.equals(text, "start")){
                    //初始化相机和mediarecorder
                    initMediarecorder();
                    //实现监控功能
                    monitorFunction();
                }else {
                    Toast.makeText(MonitorActivity.this, "您自行终止了监控功能", Toast.LENGTH_SHORT).show();
                    bt_monitor.setText("start");
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    camera.stopPreview();
                    camera.release();

                    //对录制的视频进行处理
                    processTheVideo();
                    //单单视频裁剪功能
                    //startActivityForResult(new Intent(MonitorActivity.this, CutVideoActivity.class), 1);

                    //循环使用这个功能，再次跳到初始的activity
                    startActivity(new Intent(MonitorActivity.this, MainActivity.class));
                }
            }
        });
    }



    //这是跟着截取视频的那个实验功能
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }
        if (requestCode == 1){
            Bundle bundle = data.getExtras();
            int statTime = bundle.getInt("startTime");
            int endTime = bundle.getInt("endTime");
            //视频源，在哪里获取我们要处理的视频
            String srcPath = getExternalFilesDir(null).getAbsolutePath() + "/a.mp4";
            //输出源，处理完了视频之后要保存到哪里
            String targetPath = getExternalFilesDir(null).getAbsolutePath() + "/b.mp4";

            VideoUtils.cutVideo(srcPath, targetPath, statTime, endTime);
        }
    }*/

    //---------------------------------------在这里分块，上面是主要函数，下面是在主要函数中用到的函数--------------------------

    private void initMediarecorder() {
            bt_monitor.setText("monitoring...");
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
            //mediaRecorder.setVideoSize(2340, 1080);

            //设置预览画布
            //mediaRecorder.setPreviewDisplay(new Surface(monitorTextureView.getSurfaceTexture()));
            mediaRecorder.setPreviewDisplay(monitorSurfaceView.getHolder().getSurface());

            //准备录制
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    private void monitorFunction() {
        Toast.makeText(this, "正在监控中...", Toast.LENGTH_SHORT).show();
        mediaRecorder.start();
    }

    //----------------------上面的两个函数把初始化摄像头，准备录制，开始录制等都做完了，下面的功能是对录完的视频的处理--------------------

    private void processTheVideo() {
        //在这个处理函数中，保存图片，确定开始时间，确定结束时间，截取视频这四件事我们用四个线程来完成，以提升速度

        //视频源，在哪里获取我们要处理的视频
        String srcPath = getExternalFilesDir(null).getAbsolutePath() + "/a.mp4";
        //输出源，处理完了视频之后要保存到哪里
        String targetPath = getExternalFilesDir(null).getAbsolutePath() + "/b.mp4";

        //处理视频离不开的Android类，mediametadataretriever类
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        //给这个类设置视频源
        mediaMetadataRetriever.setDataSource(srcPath);
        //获取视频时长，长度单位为毫秒
        String videoTime = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //获取视频时长，长度单位为秒
        int videoSeconds = Integer.parseInt(videoTime) / 1000;

        //线程一：把视频每秒都保存为图片
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= videoSeconds; i++) {
                    //这个for循环是将每一秒的视频存成bitmap图片，图片存放地址和原视频地址一样
                    //将每秒视频转化为bitmap图像，为什么是*1000*1000是因为这个getframeattime函数第一个参数单位只能是us（微秒）
                    Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime((long) i * 1000 * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);

                    //给每一秒的视频转化成的图像设置存放路径
                    String bitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + i + ".jpg";
                    //设置一个文件输出流，用于输出图像数据到文件
                    FileOutputStream fos = null;
                    try {
                        //这个构造函数中传入了图像的路径
                        fos = new FileOutputStream(bitmapPath);
                        //bitmap.compress函数用于将位图的压缩版本写入指定的输出流
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //线程二：判断裁剪视频的开始时间
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    thread1.join();
                    //重新写一个for循环，用来确定剪裁视频的开始时间
                    for (int i = 1; i < videoSeconds; i++) {

                        String bitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + i + ".jpg";
                        String nextBitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + (i + 1) + ".jpg";

                        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
                        Bitmap nextBitmap = BitmapFactory.decodeFile(nextBitmapPath);

                        /*//将每秒视频转化为bitmap图像，为什么是*1000*1000是因为这个getframeattime函数第一个参数单位只能是us（微秒）
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime((long) i * 1000 * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);

                        //在这直接判断两个bitmap一样不一样了，从而获取视频开始截取的时间
                        //nextbitmap是下一秒的bitmap图像，我们通过compare2Image函数来判断这两个bitmap图像一不一样，如果一样就不要，如果不一样就开始截取
                        Bitmap nextBitmap = mediaMetadataRetriever.getFrameAtTime((long) (i + 1) * 1000 * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);*/

                        if (!ImageUtils.compare2Bitmap(bitmap, nextBitmap)) {
                            //设置wantedStartTime不等于0的原因是现阶段我只想让它执行一回，否则就乱套了
                            changeWantedStartTimeInt(i);
                            Thread.sleep(1);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        //线程三：判断截取视频的结束时间
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    thread2.join();
                    //这个地方只要开始截取的时间还不够，还需要一个结束的时间
                    //需求是，当画面连续不变 x 时间时，就不要后面部分的了，也就是说，条件满足的那一刻，就是结束时间
                    //我打算在已经知道的开始时间开始，再去判断一次有没有这样的结束时间，否则就传入总的视频时长结束时间
                    //比如说，这里我们假设如果连续三秒画面不变的话，我们就不要后面的了，就到此为止

                    for (int i = wantedStartTime; i <= videoSeconds - wantedCrossTime; i++) {
                        /*Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime((long) i * 1000 * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);
                        Bitmap nextBitmap = mediaMetadataRetriever.getFrameAtTime((long) (i + 1) * 1000 * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);*/
                        String bitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + i + ".jpg";
                        String nextBitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + (i + 1) + ".jpg";

                        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
                        Bitmap nextBitmap = BitmapFactory.decodeFile(nextBitmapPath);

                        if (ImageUtils.compare2Bitmap(bitmap, nextBitmap)) {
                            changeDuringTimeInt();
                            if (duringTime == wantedCrossTime) {
                                changeWantedEndTimeInt(i + 1);
                                break;
                            }
                        } else {
                            changeDuringTimeToZero();
                        }
                        System.out.println(wantedEndTime);
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        //线程四：截取视频
        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取要截取的视频的开端（什么时候图片不一样了再截取），把视频结束时间传进去
                    //获取要截取的视频的结尾（这个好说，只是视频的末尾就可以）
                    //其实就是videoSeconds
                    //进行视频的二次加工
                    thread3.join();
                    if (wantedEndTime == 0){
                        System.out.println("此时开始时间是多少？" + wantedStartTime);
                        System.out.println("此时结束时间是多少？" + wantedEndTime);
                        VideoUtils.cutVideo(srcPath, targetPath, wantedStartTime, videoSeconds);
                    }else {
                        System.out.println("此时开始时间是多少？" + wantedStartTime);
                        System.out.println("此时结束时间是多少？" + wantedEndTime);
                        VideoUtils.cutVideo(srcPath, targetPath, wantedStartTime, wantedEndTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        /*for (int i = 1 ; i < videoSeconds ; i++) {
            String bitmapPath = getExternalFilesDir(null).getAbsolutePath() + "/" + i + ".jpg";
            Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);

            Toast.makeText(MonitorActivity.this, "" + bitmap.getPixel(100, 100), Toast.LENGTH_SHORT).show();
        }*/


        //获取要截取的视频的开端（什么时候图片不一样了再截取），把视频结束时间传进去
        //获取要截取的视频的结尾（这个好说，只是视频的末尾就可以）
        //其实就是videoSeconds
        //进行视频的二次加工

        /*if (wantedEndTime == 0){
            //makeVideoEnd(srcPath, targetPath, wantedStartTime, videoSeconds);
            System.out.println("此时开始时间是多少？" + wantedStartTime);
            VideoUtils.cutVideo(srcPath, targetPath, wantedStartTime, videoSeconds);
        }else {
            //makeVideoEnd(srcPath, targetPath, wantedStartTime, wantedEndTime);
            System.out.println("此时开始时间是多少？" + wantedStartTime);
            VideoUtils.cutVideo(srcPath, targetPath, wantedStartTime, wantedEndTime);
        }*/
    }

    public synchronized void changeWantedStartTimeInt(int i) {
        this.wantedStartTime = i;
    }

    public synchronized void changeWantedEndTimeInt(int i) {
        this.wantedEndTime = i;
    }

    public synchronized void changeDuringTimeInt() {
        this.duringTime ++;
    }

    public synchronized void changeDuringTimeToZero() {
        this.duringTime = 0;
    }
}