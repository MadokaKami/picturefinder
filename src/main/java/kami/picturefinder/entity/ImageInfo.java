package kami.picturefinder.entity;

/**
 * @Description 图像信息entity
 * @ClassName ImageInfo
 * @Date 2018/8/14 21:42
 * @Author 李英夫
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018/8/14.
 */
public class ImageInfo {

    /**图像指纹信息*/
    private long fingerPrint;

    /**文件路径*/
    private String filePath;

    /**
     * 图像信息类构造方法
     */
    public ImageInfo() {
    }

    /**
     * 图像信息类构造方法
     * @param fingerPrint 图像指纹信息
     * @param filePath 文件路径
     */
    public ImageInfo(long fingerPrint, String filePath) {
        this.fingerPrint = fingerPrint;
        this.filePath = filePath;
    }

    public long getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(long fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
