package kami.picturefinder.main;

import kami.picturefinder.config.RootConfig;
import kami.picturefinder.core.ImageCoreProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        /*ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
        ImageCoreProcessor imageCoreProcessor = (ImageCoreProcessor)applicationContext.getBean("imageCoreProcessor");
        imageCoreProcessor.pushFileInRedis();*/
        System.out.println(Arrays.toString("asd".split(",")));
    }
}