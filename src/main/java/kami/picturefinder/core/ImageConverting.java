package kami.picturefinder.core;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import kami.picturefinder.exception.ImageMatchingException;
import kami.picturefinder.util.PictureLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>图片转换类</p>
 * <prep>转换读取到的图片，获取图片指纹，比对图片指纹</prep>
 * @ClassName ImageConverting
 * @Date 2018/8/13 22:06
 * @author 李英夫
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
@Component
public class ImageConverting {

    /**压缩后宽度*/
    private static final int COMPRESS_WIDTH = 8;

    /**压缩后高度*/
    private static final  int COMPRESS_HEIGHT = 8;

    /**压缩后像素点数量*/
    public static final int COMPRESS_AREA = COMPRESS_WIDTH * COMPRESS_HEIGHT;

    private Logger logger = LoggerFactory.getLogger(ImageConverting.class);

    /**目标文件夹*/
    @Value("${picture.directoryPath}")
    private String directoryPath;

    /**生成缩略图地址*/
    @Value("${picture.thumbnailPath}")
    private String thumbnailPath;

    /**是否为调试模式*/
    @Value("${picture.debuggingModel}")
    private boolean debuggingModel;

    //四舍五入并保留十六位小数
    private static final MathContext mc = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * 获取对应文件图像缓冲区类
     * @param inputFile 输入文件
     * @return 图像缓冲区实例化对象
     * @see ImageConverting#readBufferedImage(File) 读取后需要对图像大小做转换
     */
    private BufferedImage readBufferedImage(File inputFile) throws ImageMatchingException {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(inputFile);
        } catch (IOException e) {
            bufferedImage = null;
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            bufferedImage = null;
            e.printStackTrace();
            logger.error("该图片中含有大量的元数据(注解数据),结构复杂，java端读取错误，图片地址为：{}",inputFile.getPath());
        }
        if(bufferedImage == null){
            logger.error("发现读取失败的图像，图像地址为{}", inputFile.getPath());
            throw new ImageMatchingException("图片错误");
        }
        return convertBufferedImageBulk(bufferedImage);
    }

    /**
     * <p>文件图像大小转换</p>
     * <prep>缩小尺寸,将图片缩小到8x8的尺寸，总共64个像素。
     * 这一步的作用是去除图片的细节，只保留结构、明暗等基本信息，
     * 摒弃不同尺寸、比例带来的图片差异。</prep>
     * @param src 源图像缓冲区
     * @return target 转换后图像缓冲区
     */
    private BufferedImage convertBufferedImageBulk(BufferedImage src){
        if(src == null){
            return null;
        }
        //获取图像类型
        int type = src.getType();
        //x轴比例
        double sx = new BigDecimal(COMPRESS_WIDTH).divide(new BigDecimal(String.valueOf(src.getWidth())), mc).doubleValue();
        //y轴比例
        double sy = new BigDecimal(COMPRESS_HEIGHT).divide(new BigDecimal(String.valueOf(src.getHeight())), mc).doubleValue();
        BufferedImage target;
        if(type == BufferedImage.TYPE_CUSTOM){
            //图像等于自定义类型
            //获取颜色值及阿尔法值信息
            ColorModel cm = src.getColorModel();
            //判断图片是否透明
            boolean isAlpha = cm.isAlphaPremultiplied();
            //创建图像写入类
            WritableRaster raster = cm.createCompatibleWritableRaster(COMPRESS_WIDTH, COMPRESS_HEIGHT);
            //重建图像缓冲区
            target = new BufferedImage(cm, raster, isAlpha, null);
        }else{
            //重建图像缓冲区
            target = new BufferedImage(COMPRESS_WIDTH, COMPRESS_HEIGHT, type);
        }
        //绘图类
        Graphics2D g = target.createGraphics();
        /**** 着色微调 start ****/
        //着色技术配置，设置为默认值
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_DEFAULT);
        //颜色着色技术配置，设置为最低
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        /**** 着色微调 end ****/
        //向缓冲区绘制
        g.drawRenderedImage(src, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return  target;
    }


    /**
     * <p>生产图像指纹</p>
     * <prep>获取图像的灰度图像，并获取像素灰度平均值，将各像素点的灰度值与平均值比对，拼接图像指纹。
     * 组合的次序并不重要，但要保证所有图片都采用同样次序</prep>
     * @param bufferedImage 处理过的图像缓冲区类
     * @see ImageConverting#convertBufferedImageBulk(BufferedImage)
     * @return fingerPrint 目标图像图像指纹，长度为64的二进制整数
     */
    private long produceFingerPrint(BufferedImage bufferedImage) throws ImageMatchingException {
        //获取像素灰度平均值
        int[][] pixels = PictureLoadUtil.obtainGrayArray(bufferedImage);
        //获取灰度平均值
        int averagePixels = PictureLoadUtil.averagePixels(pixels);
        //游标
        int index = 0;
        //图像指纹
        long fingerPrint = 0;
        for(int[] unidimensionalPixels: pixels){
            for (int twoDimensionalPixels : unidimensionalPixels) {
                // 比较像素的灰度。将每个像素的灰度，与平均值进行比较。大于或等于平均值，记为1；小于平均值，记为0。
                if(twoDimensionalPixels >= averagePixels){
                    // 将上一步的比较结果，组合在一起，就构成了一个64位的二进制整数，这就是这张图片的指纹。
                    // 图片指纹中数字拼接的组合次序并不重要，只要保证所有图片都采用同样次序就行了。
                    fingerPrint += (1L << index);
                }
                //游标前移
                index++;
            }
        }
        String thumbnailName = System.nanoTime() + ".jpg";
        //调试模式打印图片
        if(debuggingModel){
            logger.debug("图像指纹为：{}", Long.toBinaryString(fingerPrint));
            checkCalculatePixels(bufferedImage, pixels, averagePixels, thumbnailName);
        }

        return fingerPrint;
    }

    /**
     * 检验像素计算结果
     * @deprecated 仅限于调试时使用，启动该方法后会大幅减慢程序运行速度
     * @param bufferedImage 执行像素计算函数的图片缓冲区对象
     * @param pixels 像素点位二维数组
     * @param averagePixels 灰度平均值
     * @param thumbnailName 缩略图名
     * @see #produceFingerPrint(BufferedImage) 打印逻辑与该方法相同，生产图像指纹计算逻辑调整，当前方法须同步调整
     */
    @Deprecated
    private void checkCalculatePixels(BufferedImage bufferedImage, int[][] pixels, int averagePixels, String thumbnailName) throws ImageMatchingException {
        logger.debug("打印缩略图,路径为：{}{}{}", thumbnailPath, "/" , thumbnailName);
        if(thumbnailPath == null){
            throw new ImageMatchingException("缩略图文件夹不存在");
        }
        try {
            // 保存处理后的文件
            FileOutputStream out = new FileOutputStream(thumbnailPath + "/" + thumbnailName);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufferedImage);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ImageMatchingException("缩略图文件夹错误");
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImageMatchingException("缩略图生成失败");
        }
        //显示匹配结果缓冲区
        StringBuilder showResultInfoBuilder = new StringBuilder(thumbnailName + "计算结果为：\r\n");
        for(int[] unidimensionalPixels: pixels){
            for (int twoDimensionalPixels : unidimensionalPixels) {
                // 比较像素的灰度。将每个像素的灰度，与平均值进行比较。大于或等于平均值，显示为●；小于平均值，显示为○。
                if(twoDimensionalPixels >= averagePixels){
                    showResultInfoBuilder.append("●");
                }else {
                    showResultInfoBuilder.append("○");
                }
            }
            showResultInfoBuilder.append("\r\n");
        }
        logger.info(showResultInfoBuilder.toString());
    }


    /**
     * 通过指定file对象获取图像指纹
     * @param sourceImage 目标图片
     * @return fingerPrint 图像指纹
     * @see #produceFingerPrint(BufferedImage)
     */
    public long getFingerPrint(File sourceImage) throws ImageMatchingException {
        //获取图像缓冲区
        BufferedImage bufferedImage = readBufferedImage(sourceImage);
        return produceFingerPrint(bufferedImage);
    }

    /**
     * <p>图像指纹比对</p>
     * <prep>通过比对源图像与目标图像，统计图像指纹计算两者的汉明距离，
     * 统计匹配点位的数量</prep>
     * @param srcPicture 源图像文件
     * @param tarPicture 目标图像文件
     * @see PictureLoadUtil#compareFingerPrint(long, long)
     * @return 像素点重复数量
     */
    public int comparePictureDiff(File srcPicture, File tarPicture) {
        //源图像图像指纹
        long srcFingerPrint;
        //目标图像图像指纹
        long tarFingerPrint;
        try {
            srcFingerPrint = getFingerPrint(srcPicture);
            tarFingerPrint = getFingerPrint(tarPicture);
        }catch (ImageMatchingException e){
            e.printStackTrace();
            //图片读取失败，默认像素点重复数量为0
            return 0;
        }
        return PictureLoadUtil.compareFingerPrint(srcFingerPrint, tarFingerPrint);
    }

}
