package kami.picturefinder.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class PictureReader {

    /**目标文件夹*/
    @Value("${picture.directoryPath}")
    private String directoryPath;

    /**压缩后宽度*/
    @Value("${picture.compressWidth}")
    private int compressWidth;

    /**压缩后高度*/
    @Value("${picture.compressHeight}")
    private int compressHeight;

    /**合计像素点位数量*/
    //picture.compressWidth * picture.compressHeight
            // * environment.getProperty('picture.compressHeight', 'Integer.class')
    @Value("#{environment.getProperty('picture.compressWidth', T(Integer)) * environment.getProperty('picture.compressHeight', T(Integer))}")
    private BigDecimal countPixels;

    /**相同图片像素吻合百分比*/
    @Value("${picture.samePercent}")
    private double samePercent;

    /**相似图片像素吻合百分比*/
    @Value("${picture.similarPercent}")
    private double similarPercent;

    //四舍五入并保留十六位小数
    private final MathContext mc = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * 获取文件集合
     * @param path 目标文件夹或文件路径
     * @return List<File> 文件集合
     */
    public List<File> getFiles(String path){
        File directoryFile = new File(path);
        return getFiles(directoryFile);
    }

    /**
     * 获取文件集合
     * @param directoryFile 目标文件夹或文件
     * @return List<File> 文件集合
     */
    private List<File> getFiles(File directoryFile){
        List<File> files = new ArrayList<>();
        //文件夹下所有文件
        if(directoryFile.isDirectory()){
            File[] pictures = directoryFile.listFiles();
            if(pictures != null){
                for(File picture : pictures){
                    if(picture.isDirectory()){
                        files.addAll(getFiles(picture));
                    }else{
                        files.add(picture);
                    }
                }
            }
        }else {
            files.add(directoryFile);
        }
        return files;
    }

    /**
     * 获取对应文件图像缓冲区类
     * @param inputFile 输入文件
     * @return 图像缓冲区实例化对象
     * @see PictureReader#readBufferedImage(File) 读取后需要对图像大小做转换
     */
    private BufferedImage readBufferedImage(File inputFile){
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(inputFile);
        } catch (IOException e) {
            bufferedImage = null;
            e.printStackTrace();
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
        double sx = new BigDecimal(String.valueOf(compressWidth)).divide(new BigDecimal(String.valueOf(src.getWidth())), mc).doubleValue();
        //y轴比例
        double sy = new BigDecimal(String.valueOf(compressHeight)).divide(new BigDecimal(String.valueOf(src.getHeight())), mc).doubleValue();
        BufferedImage target;
        if(type == BufferedImage.TYPE_CUSTOM){
            //图像等于自定义类型
            //获取颜色值及阿尔法值信息
            ColorModel cm = src.getColorModel();
            //判断图片是否透明
            boolean isAlpha = cm.isAlphaPremultiplied();
            //创建图像写入类
            WritableRaster raster = cm.createCompatibleWritableRaster(compressWidth, compressHeight);
            //重建图像缓冲区
            target = new BufferedImage(cm, raster, isAlpha, null);
        }else{
            //重建图像缓冲区
            target = new BufferedImage(compressWidth, compressHeight, type);
        }
        //绘图类
        Graphics2D g = target.createGraphics();
        /****着色微调start****/
        //着色技术配置，设置为默认值
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_DEFAULT);
        //颜色着色技术配置，设置为最低
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        /****着色微调end****/
        //向缓冲区绘制
        g.drawRenderedImage(src, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return  target;
    }

    /**
     * <p>获取文件灰度数组</p>
     * <prep>简化色彩,将缩小后的图片，转为64级灰度。
     * 也就是说，所有像素点总共只有64种颜色。
     * 传入参数<code>BufferedImage</code>宜经过:
     * {@link PictureReader#convertBufferedImageBulk(BufferedImage)}处理</prep>
     * @param bufferedImage 处理后的图片缓冲区
     * @return pixels 处理后的二维灰度数组
     */
    private int[][] obtainGrayArray(BufferedImage bufferedImage){
        int[][] pixels = new int[compressWidth][compressHeight];
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                //由RGB转为64级灰度
                pixels[i][j] = rgbToGray(bufferedImage.getRGB(i, j));
            }
        }
        return pixels;
    }

    /**
     * 获取像素灰度平均值
     * @param pixels 灰度数组
     * @return 灰度平均值
     */
    private int averagePixels(int[][] pixels){
        int size = 0;
        int countPixels = 0;
        for(int[] unidimensionalPixels: pixels){
            for (int twoDimensionalPixels : unidimensionalPixels) {
                countPixels += twoDimensionalPixels;
                size++;
            }
        }
        return new BigDecimal(countPixels).divide(new BigDecimal(size), mc).intValue();
    }

    /**
     * <p>生产图像指纹</p>
     * <prep>获取图像的灰度图像，并获取像素灰度平均值，将各像素点的灰度值与平均值比对，拼接图像指纹。
     * 组合的次序并不重要，但要保证所有图片都采用同样次序</prep>
     * @param bufferedImage 处理过的图像缓冲区类
     * @see PictureReader#convertBufferedImageBulk(BufferedImage)
     * @return fingerPrint 目标图像图像指纹，长度为64的二进制整数
     */
    private int produceFingerPrint(BufferedImage bufferedImage){
        //获取像素灰度平均值
        int[][] pixels = obtainGrayArray(bufferedImage);
        //获取灰度平均值
        int averagePixels = averagePixels(pixels);
        //游标
        int index = 0;
        //图像指纹
        int fingerPrint = 0;
        for(int[] unidimensionalPixels: pixels){
            for (int twoDimensionalPixels : unidimensionalPixels) {
                // 比较像素的灰度。将每个像素的灰度，与平均值进行比较。大于或等于平均值，记为1；小于平均值，记为0。
                if(twoDimensionalPixels >= averagePixels){
                    // 将上一步的比较结果，组合在一起，就构成了一个64位的二进制整数，这就是这张图片的指纹。
                    // 图片指纹中数字拼接的组合次序并不重要，只要保证所有图片都采用同样次序就行了。
                    fingerPrint += (1 << index++);
                }
            }
        }
        return fingerPrint;
    }

    /**
     * <p>图像指纹比对</p>
     * <prep>通过比对源图像与目标图像的图像指纹，计算两者的汉明距离，
     * 统计匹配点位的数量</prep>
     * @param srcFingerPrint 源图像指纹
     * @param tarFingerPrint 目标图像指纹
     * @return 像素点重复数量
     */
    private int compareFingerPrint(int srcFingerPrint, int tarFingerPrint){
        //进行异或运算，汉明距离即为二进制中1的个数
        int compare = srcFingerPrint ^ tarFingerPrint;
        //相同值统计
        int count = 0;
        while (compare != 0){
            if((compare & 1) != 0){
                count++;
            }
            compare = compare >> 1;
        }
        return count;
    }

    /**
     * <p>图像指纹比对</p>
     * <prep>通过比对源图像与目标图像，统计图像指纹计算两者的汉明距离，
     * 统计匹配点位的数量</prep>
     * @param srcPicture 源图像文件
     * @param tarPicture 目标图像文件
     * @see PictureReader#compareFingerPrint(int, int)
     * @return 像素点重复数量
     */
    public int comparePictureDiff(File srcPicture, File tarPicture){
        BufferedImage srcBufferedImage = readBufferedImage(srcPicture);
        BufferedImage tarBufferedImage = readBufferedImage(tarPicture);
        int srcFingerPrint = produceFingerPrint(srcBufferedImage);
        int tarFingerPrint = produceFingerPrint(tarBufferedImage);
        return compareFingerPrint(srcFingerPrint, tarFingerPrint);
    }

    /**
     * 图片同像素点占比
     * @param srcPicture 源图像文件
     * @param tarPicture 目标图像文件
     * @return samePixelPercent 像素点重复百分比
     */
    public double samePixelPercent(File srcPicture, File tarPicture){
        int samePixelConut = comparePictureDiff(srcPicture, tarPicture);
        return new BigDecimal(samePixelConut).divide(countPixels, mc).doubleValue();
    }



    /**
     * rgb像素转64级灰度
     * @param pixels rgb像素
     * @return 灰度像素
     */
    private int rgbToGray(int pixels) {
        // int _alpha = (pixels >> 24) & 0xFF;
        int _red = (pixels >> 16) & 0xFF;
        int _green = (pixels >> 8) & 0xFF;
        int _blue = (pixels) & 0xFF;
        return (int) (0.2989 * _red + 0.5870 * _green + 0.1140 * _blue);
    }

}
