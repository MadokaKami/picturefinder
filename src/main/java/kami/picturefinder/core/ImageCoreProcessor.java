package kami.picturefinder.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import kami.picturefinder.entity.ContrastImageInfo;
import kami.picturefinder.entity.ImageInfo;
import kami.picturefinder.entity.SameImageInfo;
import kami.picturefinder.exception.ImageMatchingException;
import kami.picturefinder.util.PictureLoadUtil;

/**
 * @Description 图像核心处理
 * @ClassName ImageCoreProcessor
 * @Date 2018/8/13 22:57
 * @author 李英夫
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
@Component
public class ImageCoreProcessor {

    private Logger logger = LoggerFactory.getLogger(ImageCoreProcessor.class);

    @Value("${picture.directoryPath}")
    private String directoryPath;

    /**图像指纹存入redis  hash   键名称*/
    @Value("${picture.saveRedisHashKey}")
    private String saveRedisHashKey;

    /**图像指纹取出redis  hash   键名称 以逗号分隔*/
    @Value("${picture.selRedisHashKey}")
    private String selRedisHashKey;

    /**不同图片像素点吻合百分比,超限即认为图像不相同*/
    @Value("${picture.inequalityPercent}")
    private double inequalityPercent;

    /**不相似图片像素点吻合百分比,超限即认为图像不相似*/
    @Value("${picture.dissimilarityPercent}")
    private double dissimilarityPercent;

    @Autowired
    private ImageConverting imageConverting;

    /*@Autowired
    private JedisUtil jedisUtil;*/

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //private static final String JEDIS_FILE_KEY = "myFiles";

    /**
     * 读取图片，并将内容存入redis
     */
    public void pushFileInRedis(){
        pushFileInRedis(directoryPath);
    }

    /**
     * 目标文件夹或文件路径
     * @param directoryPath 目标文件夹或文件路径
     */
    public void pushFileInRedis(String directoryPath){
        LinkedList<File> fileList = PictureLoadUtil.getFiles(directoryPath);
        //数组中文件数量
        final int fileCount = fileList.size();
        logger.info("文件加载完成---->读取到图像文件{}个，即将开始转换",fileCount);
        //文件信息map，键为文件路径，值为文件指纹
        Map<String, String> fileMap = new HashMap<>();
        //完成百分比
        double completePercentCache = 0;
        //执行次数
        int executeTitle = 0;
        while (fileList.size() > 0){
            File file = fileList.pop();
            try{
                fileMap.put(file.getPath(), String.valueOf(imageConverting.getFingerPrint(file)));
            }catch (ImageMatchingException e){
                e.printStackTrace();
            }finally {
                if(executeTitle++ % 30 == 0 && (completePercentCache != (completePercentCache = (double)Math.round((double) executeTitle / fileCount * 10000) / 100))){
                    logger.info("当前已转换完成{}%",completePercentCache);
                }
            }
        }
        /*Jedis jedis = jedisUtil.getJedis();
        jedis.hmset(saveRedisHashKey, fileMap);*/
        redisTemplate.opsForHash().putAll(saveRedisHashKey, fileMap);
    }

    /**
     * 从redis获取全部图像信息
     * 因为后续需要写入匹配时相同像素占比，这里将读出的数据存入子类
     * @return imageInfoLinkedList 图像指纹集合
     */
    private LinkedList<ContrastImageInfo> getAllFingerPrintForRedis(){
        //Jedis jedis = jedisUtil.getJedis();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        if(selRedisHashKey == null || selRedisHashKey.length() == 0){
            throw new IllegalArgumentException("查询图像指纹时redis键异常");
        }
        //图片信息链表
        String[] redisHashKeyArray = selRedisHashKey.split(",");
        LinkedList<ContrastImageInfo> imageInfoLinkedList = new LinkedList<>();
        for(String redisHashKey: redisHashKeyArray){
            //从redis中获取图像指纹
            Map<String, String> fingerPrintMap = hashOperations.entries(redisHashKey);
                    //jedis.hgetAll(redisHashKey);
            //遍历set集合，将获取到的图片信息插入图片信息链表
            for(Map.Entry<String, String> entry : fingerPrintMap.entrySet()){
                imageInfoLinkedList.push(new ContrastImageInfo(Long.valueOf(entry.getValue()),entry.getKey()));
            }
        }
        return imageInfoLinkedList;
    }

    /**
     * 匹配全部相同的图像指纹信息
     * @param imageInfoLinkedList 图像指纹集合
     */
    private LinkedList<SameImageInfo> matchingAllSameFingerPrint(LinkedList<ContrastImageInfo> imageInfoLinkedList){
        LinkedList<SameImageInfo> sameImageInfoList = new LinkedList<>();
        ListIterator<ContrastImageInfo> it;
        while(imageInfoLinkedList.size() > 1){
            List<ContrastImageInfo> imageInfoList = new ArrayList<>();
            //获取用于比对的图像
            ImageInfo imageInfo = imageInfoLinkedList.pop();
            it = imageInfoLinkedList.listIterator();
            //遍历剩余图像指纹信息逐个比对
            while (it.hasNext()){
                ContrastImageInfo iteratorImageInfo = it.next();
                //对比图像指纹汉明距离获取不同像素占比
                double fingerPixelPercent = PictureLoadUtil.inequalityPixelPercent(imageInfo.getFingerPrint(),iteratorImageInfo.getFingerPrint());
                if(fingerPixelPercent < inequalityPercent){
                    iteratorImageInfo.setSamePixelPercent(fingerPixelPercent);
                    imageInfoList.add(iteratorImageInfo);
                    it.remove();
                }
            }
            if(imageInfoList.size() > 0){
                SameImageInfo sameImageInfo = new SameImageInfo();
                //对比出相同的图片
                sameImageInfo.setContrastImageInfoList(imageInfoList);
                //用于对比的图片
                sameImageInfo.setImageInfo(imageInfo);
                sameImageInfoList.push(sameImageInfo);
            }
        }
        return sameImageInfoList;
    }

    /**
     * 获取全部匹配图像
     * @return sameImageInfoList 匹配图像集合
     * @see #getAllFingerPrintForRedis()
     * @see #matchingAllSameFingerPrint(LinkedList)
     */
    public LinkedList<SameImageInfo> getAllSameImage(){
        LinkedList<ContrastImageInfo> imageInfoLinkedList = getAllFingerPrintForRedis();
        return matchingAllSameFingerPrint(imageInfoLinkedList);
    }

}
