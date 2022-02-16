package com.example.project2.utils;

import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 视频处理工具类
 * @author panruijie
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VideoUtils {


    private static final String TAG = "VideoUtils";

    /**
     * Appends mp4 audio/video from {@code anotherFileName} to
     * {@code mainFileName}.
     */
    public static boolean append(String mainFileName, String anotherFileName) {
        boolean rvalue = false;
        try {
            File targetFile = new File(mainFileName);
            File anotherFile = new File(anotherFileName);
            if (targetFile.exists() && targetFile.length() > 0) {
                String tmpFileName = mainFileName + ".tmp";

                append(mainFileName, anotherFileName, tmpFileName);
                anotherFile.delete();
                targetFile.delete();
                new File(tmpFileName).renameTo(targetFile);
                rvalue = true;
            } else if (targetFile.createNewFile()) {
                copyFile(anotherFileName, mainFileName);
                anotherFile.delete();
                rvalue = true;
            }
        } catch (Exception tr) {
            Log.e("VideoUtils", "", tr);
        }
        return rvalue;
    }

    /**
     * 视频拼接
     *
     * @param srcFile    源文件
     * @param appendFile 待插入的文件
     * @param finalFile  最终生成的文件
     */
    public static void append(final String srcFile, final String appendFile, final String finalFile)
            throws IOException {

        final FileOutputStream fos = new FileOutputStream(new File(finalFile));
        final FileChannel fc = fos.getChannel();

        Movie movieSrc = null;
        try {
            movieSrc = MovieCreator.build(srcFile);
        } catch (Throwable tr) {
            tr.printStackTrace();
        }

        Movie movieAppend = null;
        try {
            movieAppend = MovieCreator.build(appendFile);
        } catch (Throwable tr) {
            tr.printStackTrace();
        }

        Movie finalMovie;
        if (movieSrc == null && movieAppend == null) {
            finalMovie = new Movie();
        } else if (movieSrc == null) {
            finalMovie = movieAppend;
        } else if (movieAppend == null) {
            finalMovie = movieSrc;
        } else {
            final List<Track> srcTracks = movieSrc.getTracks();
            final List<Track> appendTracks = movieAppend.getTracks();

            finalMovie = new Movie();
            for (int i = 0; i < srcTracks.size() || i < appendTracks.size(); ++i) {
                finalMovie.addTrack(new AppendTrack(srcTracks.get(i), appendTracks.get(i)));
            }
        }

        final Container container = new DefaultMp4Builder().build(finalMovie);
        container.writeContainer(fc);
        fc.close();
        fos.close();
    }

    /**
     * 复制文件
     * @param from
     * @param destination
     * @throws IOException
     */
    public static void copyFile(final String from, final String destination) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(destination);
        copy(in, out);
        in.close();
        out.close();
    }

    public static void copy(FileInputStream in, FileOutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * 将 MP4 切割
     *
     * @param mp4Path    .mp4
     * @param fromSample 起始位置
     * @param toSample   结束位置
     * @param outPath    .mp4
     */
    public static void cropMp4(String mp4Path, long fromSample, long toSample, String outPath) throws IOException {
        Movie mp4Movie = MovieCreator.build(mp4Path);
        Track videoTracks = null;// 获取视频的单纯视频部分
        for (Track videoMovieTrack : mp4Movie.getTracks()) {
            if ("vide".equals(videoMovieTrack.getHandler())) {
                videoTracks = videoMovieTrack;
            }
        }
        Track audioTracks = null;// 获取视频的单纯音频部分
        for (Track audioMovieTrack : mp4Movie.getTracks()) {
            if ("soun".equals(audioMovieTrack.getHandler())) {
                audioTracks = audioMovieTrack;
            }
        }

        Movie resultMovie = new Movie();
        resultMovie.addTrack(new AppendTrack(new CroppedTrack(videoTracks, fromSample, toSample)));// 视频部分
        resultMovie.addTrack(new AppendTrack(new CroppedTrack(audioTracks, fromSample, toSample)));// 音频部分

        Container out = new DefaultMp4Builder().build(resultMovie);
        FileOutputStream fos = new FileOutputStream(new File(outPath));
        out.writeContainer(fos.getChannel());
        fos.close();
    }

    /**
     * 裁剪视频
     *
     * @param src         源文件
     * @param dest        输出地址
     * @param startSecond 开始时间
     * @param endSecond   结束时间
     */
    public static void cutVideo(String src, String dest, double startSecond, double endSecond) {
        try {
            //构造一个movie对象
            Movie movie = MovieCreator.build(src);
            List<Track> tracks = movie.getTracks();
            movie.setTracks(new ArrayList<Track>());

            boolean timeCorrected = false;
            // Here we try to find a track that has sync samples. Since we can only start decoding
            // at such a sample we SHOULD make sure that the start of the new fragment is exactly
            // such a frame
            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        // This exception here could be a false positive in case we have multiple tracks
                        // with sync samples at exactly the same positions. E.g. a single movie containing
                        // multiple qualities of the same video (Microsoft Smooth Streaming file)

                        throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                    }
                    //矫正开始时间
                    startSecond = correctTimeToSyncSample(track, startSecond, false);
                    //矫正结束时间
                    endSecond = correctTimeToSyncSample(track, endSecond, true);

                    timeCorrected = true;
                }
            }

            for (Track track : tracks) {
                long currentSample = 0;
                double currentTime = 0;
                double lastTime = -1;
                long startSample = -1;
                long endSample = -1;


                for (int i = 0; i < track.getSampleDurations().length; i++) {
                    long delta = track.getSampleDurations()[i];


                    if (currentTime > lastTime && currentTime <= startSecond) {
                        // current sample is still before the new starttime
                        startSample = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endSecond) {
                        // current sample is after the new start time and still before the new endtime
                        endSample = currentSample;
                    }

                    lastTime = currentTime;
                    //计算出某一帧的时长 = 采样时长 / 时间长度
                    currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                    //这里就是帧数（采样）加一
                    currentSample++;
                }
                //在这里，裁剪是根据关键帧进行裁剪的，而不是指定的开始时间和结束时间
                movie.addTrack(new CroppedTrack(track, startSample, endSample));

                Container out = new DefaultMp4Builder().build(movie);
                FileOutputStream fos = new FileOutputStream(String.format(dest));
                FileChannel fc = fos.getChannel();
                out.writeContainer(fc);

                fc.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 矫正裁剪的sample位置
     * @param track 视频轨道
     * @param cutHere 裁剪位置
     * @param next 是否还继续裁剪
     * @return
     */
    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1（采样的下标从1开始而不是0开始，所以要+1 ）
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}