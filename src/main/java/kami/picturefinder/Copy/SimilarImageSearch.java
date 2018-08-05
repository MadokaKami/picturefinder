package kami.picturefinder.Copy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SimilarImageSearch {
    public static final int SAMEVALUE = 5; //相同图片阀值
    public static final int SIMILARVALUE = 10; //相似图片阀值

    public static void main(String[] args) {
        List<String> hashCodes = new ArrayList<String>();

        String searchImgUrl = "C:\\Users\\admin\\Desktop\\image\\example.jpg";
        List<String> urlList = collectionImgUrl();
        String hashCode = null;
        long startMillis = System.currentTimeMillis();

        for (String url : urlList) {
            hashCode = produceFingerPrint(url);
            hashCodes.add(hashCode);
        }

        System.out.println("Resources: ");
        System.out.println(hashCodes);
        System.out.println();

        String sourceHashCode = produceFingerPrint(searchImgUrl);
        System.out.println("Source: ");
        System.out.println(sourceHashCode);
        System.out.println();

        List resultList = new ArrayList();
        List similarResultList = new ArrayList();
        List differences = new ArrayList();

        for (int i = 0; i < hashCodes.size(); i++) {
            int difference = hammingDistance(sourceHashCode, hashCodes.get(i));
            if(difference <= SAMEVALUE){
                resultList.add(urlList.get(i).substring(urlList.get(i).lastIndexOf("\\")+1,urlList.get(i).length()));
            }else if(difference <= SIMILARVALUE){
                similarResultList.add(urlList.get(i).substring(urlList.get(i).lastIndexOf("\\")+1,urlList.get(i).length()));
            }
            differences.add(difference+"->"+urlList.get(i).substring(urlList.get(i).lastIndexOf("\\")+1,urlList.get(i).length()));
        }

        System.out.println("curMillis:"+(System.currentTimeMillis()-startMillis));
        System.out.println("搜索图片:"+searchImgUrl.substring(searchImgUrl.lastIndexOf("\\")+1,searchImgUrl.length()));
                System.out.println("相同图片:"+resultList);
        System.out.println("相似图片:"+similarResultList);
        System.out.println("图片对比:"+differences);
    }


    public static List collectionImgUrl(){
        String imgPath = "C:\\Users\\admin\\Desktop\\image";

        List list = new ArrayList();
        File file = new File(imgPath);

        if(file.isDirectory()){
            String[] fileNames = file.list();
            for(String name : fileNames){
                list.add(imgPath.concat("\\")+name);
            }
        }

        return list;
    }


    public static int hammingDistance(String sourceHashCode, String hashCode) {
        int difference = 0;
        int len = sourceHashCode.length();

        for (int i = 0; i < len; i++) {
            if (sourceHashCode.charAt(i) != hashCode.charAt(i)) {
                difference++;
            }
        }

        return difference;
    }


    public static String produceFingerPrint(String filename) {
        BufferedImage source = ImageHelper.readPNGImage(filename);// 读取文件

        int width = 8;
        int height = 8;

        // 第一步，缩小尺寸。
        // 将图片缩小到8x8的尺寸，总共64个像素。这一步的作用是去除图片的细节，只保留结构、明暗等基本信息，摒弃不同尺寸、比例带来的图片差异。
        BufferedImage thumb = ImageHelper.thumb(source, width, height, false);

        // 第二步，简化色彩。
        // 将缩小后的图片，转为64级灰度。也就是说，所有像素点总共只有64种颜色。
        int[] pixels = new int[width * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i * height + j] = ImageHelper.rgbToGray(thumb.getRGB(i, j));
            }
        }

        // 第三步，计算平均值。
        // 计算所有64个像素的灰度平均值。
        int avgPixel = ImageHelper.average(pixels);

        // 第四步，比较像素的灰度。
        // 将每个像素的灰度，与平均值进行比较。大于或等于平均值，记为1；小于平均值，记为0。
        int[] comps = new int[width * height];
        for (int i = 0; i < comps.length; i++) {
            if (pixels[i] >= avgPixel) {
                comps[i] = 1;
            } else {
                comps[i] = 0;
            }
        }

        // 第五步，计算哈希值。
        // 将上一步的比较结果，组合在一起，就构成了一个64位的整数，这就是这张图片的指纹。组合的次序并不重要，只要保证所有图片都采用同样次序就行了。
        StringBuffer hashCode = new StringBuffer();
        for (int i = 0; i < comps.length; i += 4) {
            int result = comps[i] * (int) Math.pow(2, 3) + comps[i + 1]
                    * (int) Math.pow(2, 2) + comps[i + 2] * (int) Math.pow(2, 1)
                    + comps[i + 2];
            hashCode.append(binaryToHex(result));
        }

        // 得到指纹以后，就可以对比不同的图片，看看64位中有多少位是不一样的。
        return hashCode.toString();
    }


    private static char binaryToHex(int binary) {
        char ch = ' ';
        switch (binary) {
            case 0:
                ch = '0';
                break;
            case 1:
                ch = '1';
                break;
            case 2:
                ch = '2';
                break;
            case 3:
                ch = '3';
                break;
            case 4:
                ch = '4';
                break;
            case 5:
                ch = '5';
                break;
            case 6:
                ch = '6';
                break;
            case 7:
                ch = '7';
                break;
            case 8:
                ch = '8';
                break;
            case 9:
                ch = '9';
                break;
            case 10:
                ch = 'a';
                break;
            case 11:
                ch = 'b';
                break;
            case 12:
                ch = 'c';
                break;
            case 13:
                ch = 'd';
                break;
            case 14:
                ch = 'e';
                break;
            case 15:
                ch = 'f';
                break;
            default:
                ch = ' ';
        }
        return ch;
    }
}
