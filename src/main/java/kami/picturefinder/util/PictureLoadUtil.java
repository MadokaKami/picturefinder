package kami.picturefinder.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kami.picturefinder.core.ImageConverting;

/**
 * @Description 图片读取工具
 * @ClassName PictureLoadUtil
 * @Date 2018/8/13 22:07
 * @author 李英夫
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class PictureLoadUtil {

    private static Logger logger = LoggerFactory.getLogger(ImageConverting.class);

    /**图像后缀名*/
    private static final String[] IMAGE_SUFFIX = new String[]{"bmp", "jpg", "png"};

    /**
     * 获取文件集合
     * @param path 目标文件夹或文件路径
     * @return List<File> 文件集合
     */
    public static LinkedList<File> getFiles(String path){
        File directoryFile = new File(path);
        LinkedList<File> fileList = getFiles(directoryFile);
        logger.info("经查询读取到图像文件{}个", fileList.size());
        return fileList;
    }

    /**
     * 获取文件集合
     * @param directoryFile 目标文件夹或文件
     * @return List<File> 文件集合
     */
    private static LinkedList<File> getFiles(File directoryFile){
        LinkedList<File> files = new LinkedList<>();
        //文件夹下所有文件
        if(directoryFile.isDirectory()){
            File[] pictures = directoryFile.listFiles();
            if(pictures != null && pictures.length > 0){
                for(File picture : pictures){
                    if(picture.isDirectory()){
                        files.addAll(getFiles(picture));
                        //若确认为图片则加入集合
                    }else if(isPicture(picture)){
                        files.push(picture);
                    }
                }
            }
            //当目标对象不为文件夹时，判断是否是图片
        }else if(isPicture(directoryFile)){
            files.add(directoryFile);
        }
        return files;
    }

    /**
     * 判断是否为图片
     * @param file 传入文件
     * @return true 是图片  false 不是图片
     */
    private static boolean isPicture(File file){
        String fileName = file.getName();
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //在后缀名数组寻找是否有匹配值，匹配返回true，找不到匹配的后缀名返回false
        for (String existSuffix : IMAGE_SUFFIX){
            if(existSuffix.equalsIgnoreCase(fileSuffix)){
                return true;
            }
        }
        return false;
    }

    /**
     * <p>获取文件灰度数组</p>
     * <prep>简化色彩,将缩小后的图片，转为64级灰度。
     * 也就是说，所有像素点总共只有64种颜色。
     * 传入参数<code>BufferedImage</code>宜经过:
     * {@link kami.picturefinder.core.ImageConverting#convertBufferedImageBulk(BufferedImage)}处理</prep>
     * @param bufferedImage 处理后的图片缓冲区
     * @return pixels 处理后的二维灰度数组
     */
    public static int[][] obtainGrayArray(BufferedImage bufferedImage){
        int[][] pixels = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                //由RGB转为64级灰度
                pixels[i][j] = rgbToGray(bufferedImage.getRGB(j, i));
            }
        }
        return pixels;
    }

    /**
     * 获取像素灰度平均值
     * @param pixels 灰度数组
     * @return 灰度平均值
     */
    public static int averagePixels(int[][] pixels){
        int size = 0;
        int countPixels = 0;
        for(int[] unidimensionalPixels: pixels){
            for (int twoDimensionalPixels : unidimensionalPixels) {
                countPixels += twoDimensionalPixels;
                size++;
            }
        }
        return countPixels / size;
    }

    /**
     * rgb像素转64级灰度
     * @param pixels rgb像素
     * @return 灰度像素
     */
    private static int rgbToGray(int pixels) {
        // int _alpha = (pixels >> 24) & 0xFF;
        int _red = (pixels >> 16) & 0xFF;
        int _green = (pixels >> 8) & 0xFF;
        int _blue = (pixels) & 0xFF;
        return (int) (0.2989 * _red + 0.5870 * _green + 0.1140 * _blue);
    }

    /**
     * <p>图像指纹比对</p>
     * <prep>通过比对源图像与目标图像的图像指纹，计算两者的汉明距离，
     * 统计匹配点位的数量</prep>
     * @param srcFingerPrint 源图像指纹
     * @param tarFingerPrint 目标图像指纹
     * @return 不匹配像素点数量
     */
    public static int compareFingerPrint(long srcFingerPrint, long tarFingerPrint){
        //进行异或运算，汉明距离即为二进制中1的个数
        long compare = srcFingerPrint ^ tarFingerPrint;
        //相同值统计
        int count = 0;
        while (compare != 0){
            //判断尾端数字
            if((compare & 1) != 0){
                count++;
            }
            compare = compare >>> 1;
        }
        return count;
    }

    /**
     * 图片不同像素点占比
     * @param srcFingerPrint 源图像指纹
     * @param tarFingerPrint 目标图像指纹
     * @return samePixelPercent 像素点重复百分比
     */
    public static double inequalityPixelPercent(long srcFingerPrint, long tarFingerPrint){
        //汉明距离
        int samePixelConut= compareFingerPrint(srcFingerPrint, tarFingerPrint);
        //这里不需要使用bigDecimal高精度运算，百分比返回一个初略的值即可
        return (double)Math.round((double) samePixelConut / (ImageConverting.COMPRESS_AREA) * 10000) / 100;
    }
}
