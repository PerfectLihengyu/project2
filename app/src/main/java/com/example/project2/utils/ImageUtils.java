package com.example.project2.utils;


import android.graphics.Bitmap;

/**
 * 图像处理的工具类
 */
public class ImageUtils {
    //把比较两个图片一不一样的方法移植到这里
    //--------------------------下面是一些工具方法-----------------------------
    //这个方法只是通过一个一个的比较像素
    public static boolean compare2Image(Bitmap bitmap, Bitmap nextBitmap) {
        int iteration = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        /*if(width != nextBitmap.getWidth()) return false;
        if(height != nextBitmap.getHeight()) return false;*/
        if(width < height){
            iteration = width;
        } else{
            iteration = height;
        }
        for(int i = 0; i < iteration; ++i) {
            if(bitmap.getPixel(i, i) != nextBitmap.getPixel(i, i)) return false;
        }
        return true;
    }


    //将彩色图转换为灰度图
    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    //用灰度值判断两张图片的相似度
    public static boolean compare2Bitmap(Bitmap bitmap, Bitmap nextBitmap){
        //先获得两张bitmap图片的灰色版本
        Bitmap bitmapGray = convertGreyImg(bitmap);
        Bitmap nextBitmapGray = convertGreyImg(nextBitmap);

        //两张图片的灰度差
        int greySum = 0;

        //处理两张灰色图片的逻辑
        int iteration = 0;
        int bitmapGrayWidth = bitmapGray.getWidth();
        int bitmapGrayHeight = bitmapGray.getHeight();
        //if(bitmapGrayWidth != nextBitmapGray.getWidth() || bitmapGrayHeight != nextBitmapGray.getHeight()) return false;

        if(bitmapGrayWidth < bitmapGrayHeight){
            iteration = bitmapGrayWidth;
        } else{
            iteration = bitmapGrayHeight;
        }
        for(int i = 0; i < iteration; ++i) {
            if (Math.abs(Math.abs(bitmapGray.getPixel(i, i)) - Math.abs(nextBitmapGray.getPixel(i, i))) > 5000000) return false;
        }
        return true;

    }
}
