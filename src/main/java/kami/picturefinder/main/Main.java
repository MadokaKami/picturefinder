package kami.picturefinder.main;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import kami.picturefinder.config.RootConfig;
import kami.picturefinder.core.ImageCoreProcessor;
import kami.picturefinder.freemarker.OutputExaminingExport;

/**
 * @Description 程序启动主方法
 * @ClassName Main
 * @author 李英夫
 * @since 2018/8/20 18:04
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
public class Main {

    public static void main(String[] args) {
        AbstractApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
        ImageCoreProcessor imageCoreProcessor = (ImageCoreProcessor)applicationContext.getBean("imageCoreProcessor");
        imageCoreProcessor.pushFileInRedis();
        //List<SameImageInfo> sameImageInfoList = imageCoreProcessor.getAllSameImage();
        OutputExaminingExport outputExaminingExport = (OutputExaminingExport)applicationContext.getBean("outputExaminingExport");
        outputExaminingExport.printExaminingExport();
        applicationContext.close();
    }
}