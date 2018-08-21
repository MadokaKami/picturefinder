package kami.picturefinder.entity;

import java.io.Serializable;

/**
 * @Description 图像信息entity
 * @ClassName ImageInfo
 * @author 李英夫
 * @since 2018/8/14 21:42
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class ImageInfo implements Serializable {

    private static final long serialVersionUID = 5729618741015554442L;

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

    /**
     * 获取fingerPrint字段数据
     *
     * @return Returns the fingerPrint.
     */
    public long getFingerPrint() {
        return fingerPrint;
    }

    /**
     * 设置fingerPrint字段数据
     *
     * @param fingerPrint The fingerPrint to set.
     */
    public void setFingerPrint(long fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    /**
     * 获取filePath字段数据
     *
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置filePath字段数据
     *
     * @param filePath The filePath to set.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
