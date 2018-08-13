package kami.picturefinder.main;

import kami.picturefinder.config.RootConfig;
import kami.picturefinder.util.JedisUtil;
import kami.picturefinder.util.PictureReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import redis.clients.jedis.Jedis;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
        JedisUtil jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
        Jedis jedis = jedisUtil.getJedis();
        System.out.println(jedis.toString());
        System.out.println(jedis.get("thiskey"));
        System.out.println(jedis.get("xjp"));
        PictureReader pictureReader = (PictureReader) applicationContext.getBean("pictureReader");
        File f1 = new File("J:\\壁纸\\~JF]XZ4DPFJE}2[@YLWO4LL.jpg");
        File f2 = new File("J:\\壁纸\\ROAME_309625_FB794B44.jpg");
        System.out.println(pictureReader.comparePictureDiff(f1, f2));
    }
}