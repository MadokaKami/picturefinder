package kami.picturefinder.main;

import kami.picturefinder.config.RootConfig;
import kami.picturefinder.util.JedisUtil;
import kami.picturefinder.util.PictureReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
        JedisUtil jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
        /*Jedis jedis = jedisUtil.getJedis();
        jedis.append("xjp","plyy");
        jedis.set("xjp","ply");
        System.out.println(jedis.toString());*/
        PictureReader pictureReader = (PictureReader) applicationContext.getBean("pictureReader");
        File f1 = new File("C:\\Users\\Administrator\\Desktop\\1533482191(1).jpg");
        File f2 = new File("C:\\Users\\Administrator\\Desktop\\63093148_p0.jpg");
        System.out.println(pictureReader.comparePictureDiff(f1, f2));
    }
}
