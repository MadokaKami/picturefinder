package kami.picturefinder.entity;

import java.util.List;

/**
 * @Description 相同的图像信息
 * 相同的图片信息存入ContrastImageInfo对象的集合中，
 * 这些信息可以被认作完全重复的图片，不在与其他图片做比对
 * @ClassName SameImageInfo
 * @author 李英夫
 * @since 2018/8/16 14:28
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class SameImageInfo {

    /**用于比对的图像本身*/
    private ImageInfo imageInfo;

    /**相同图像信息集合*/
    private List<ContrastImageInfo> contrastImageInfoList;

    /**
     * 获取imageInfo字段数据
     *
     * @return Returns the imageInfo.
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * 设置imageInfo字段数据
     *
     * @param imageInfo The imageInfo to set.
     */
    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    /**
     * 获取contrastImageInfoList字段数据
     *
     * @return Returns the contrastImageInfoList.
     */
    public List<ContrastImageInfo> getContrastImageInfoList() {
        return contrastImageInfoList;
    }

    /**
     * 设置contrastImageInfoList字段数据
     *
     * @param contrastImageInfoList The contrastImageInfoList to set.
     */
    public void setContrastImageInfoList(List<ContrastImageInfo> contrastImageInfoList) {
        this.contrastImageInfoList = contrastImageInfoList;
    }

}
