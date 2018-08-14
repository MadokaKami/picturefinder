package kami.picturefinder.core;

import kami.picturefinder.entity.ImageInfo;
import kami.picturefinder.exception.ImageMatchingException;
import kami.picturefinder.util.JedisUtil;
import kami.picturefinder.util.PictureLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ImageConverting imageConverting;

    @Autowired
    private JedisUtil jedisUtil;

    private static final String JEDIS_FILE_KEY = "myFiles";

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
        Jedis jedis = jedisUtil.getJedis();
        jedis.hmset(JEDIS_FILE_KEY, fileMap);
    }

    /**
     * 匹配全部图像指纹信息
     */
    //这个还没写完
    public void matchingAllFingerPrint(){
        Jedis jedis = jedisUtil.getJedis();
        if(selRedisHashKey == null || selRedisHashKey.length() == 0){
            throw new IllegalArgumentException("查询图像指纹时redis键异常");
        }
        //图片信息链表
        String[] redisHashKeyArray = selRedisHashKey.split(",");
        LinkedList<ImageInfo> imageInfoLinkedList = new LinkedList<>();
        for(String redisHashKey: redisHashKeyArray){
            //从redis中获取图像指纹
            Map<String, String> fingerPrintMap = jedis.hgetAll(redisHashKey);
            //遍历set集合，将获取到的图片信息插入图片信息链表
            for(Map.Entry<String, String> entry : fingerPrintMap.entrySet()){
                imageInfoLinkedList.push(new ImageInfo(Integer.valueOf(entry.getValue()),entry.getKey()));
            }
        }


    }

}
