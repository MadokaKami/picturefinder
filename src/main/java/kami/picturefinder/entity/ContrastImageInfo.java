package kami.picturefinder.entity;

/**
 * @Description 对比图像信息
 * @ClassName ContrastImageInfo
 * @author 李英夫
 * @since 2018/8/20 20:50
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class ContrastImageInfo extends ImageInfo {

    /**相同像素占比*/
    private double samePixelPercent;

    /**
     * 对比图像信息类构造方法
     * @param fingerPrint 图像指纹信息
     * @param filePath 文件路径
     */
    public ContrastImageInfo(long fingerPrint, String filePath) {
        super(fingerPrint, filePath);
    }

    /**
     * 获取samePixelPercent字段数据
     *
     * @return Returns the samePixelPercent.
     */
    public double getSamePixelPercent() {
        return samePixelPercent;
    }

    /**
     * 设置samePixelPercent字段数据
     *
     * @param samePixelPercent The samePixelPercent to set.
     */
    public void setSamePixelPercent(double samePixelPercent) {
        this.samePixelPercent = samePixelPercent;
    }
}
