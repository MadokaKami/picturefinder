package kami.picturefinder.freemarker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import freemarker.template.Configuration;
import freemarker.template.Template;
import kami.picturefinder.core.ImageCoreProcessor;
import kami.picturefinder.entity.SameImageInfo;

/**
 * @Description 检测报告输出类
 * @ClassName OutputExaminingExport
 * @author 李英夫
 * @since 2018/8/20 17:43
 * @version V1.0.0
 * @Copyright (c) All Rights Reserved, 2018.
 */
@Component
public class OutputExaminingExport {

    private Logger logger = LoggerFactory.getLogger(OutputExaminingExport.class);

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private ImageCoreProcessor imageCoreProcessor;

    /**输出路径*/
    @Value("${freemarker.outPath}")
    private String outPath;

    /**检测报告名称*/
    @Value("${freemarker.examiningExportName}")
    private String examiningExportName;

    /**freemarker模板名称*/
    @Value("${freemarker.templateName}")
    private String templateName;

    /**
     * <p>打印检测报告</p>
     * <prep>从redis中获取全部图像信息，之后调用打印方法，生成html
     * 检测匹配过程与读取过程是独立的，这是为了提示匹配效率，
     * 大批量扫描图片是一个漫长的过程，这里采用先扫描，再匹配的方式</prep>
     * @see ImageCoreProcessor#getAllSameImage()
     */
    public void printExaminingExport(){
        //获取重复图像信息集合
        LinkedList<SameImageInfo> sameImageInfoList = imageCoreProcessor.getAllSameImage();
        logger.info("图片匹配完成,共匹配出相同图片{}组,即将开始打印检测报告....",sameImageInfoList.size());
        int index = 0;
        List<SameImageInfo> sameImageInfoOutputList = new ArrayList<>(100);
        //集合过长，这里需要分开打印检测报告
        while (sameImageInfoList.size() > 0){
            //每一百条输出插入新的集合打印一次
            sameImageInfoOutputList.add(sameImageInfoList.pop());
            if(sameImageInfoOutputList.size() >= 100 || sameImageInfoList.size() == 0){
                printExaminingExport(sameImageInfoOutputList, ++index);
                sameImageInfoOutputList.clear();
            }
        }

    }

    /**
     * <p>打印检测报告</p>
     * <prep>将检测结果通过freemarker进行遍历，生成静态html，展示匹配结果</prep>
     * @param sameImageInfoList 相同的图像信息对象集合
     * @param index 当前页签
     */
    private void printExaminingExport(List<SameImageInfo> sameImageInfoList, int index) {
        // 输出文档路径及名称
        File outFile = new File(outPath + File.separator + examiningExportName + "(" + index + ").html");
        /*if (!outFile.getParentFile().exists()) {  //判断父目录路径是否存在，即test.txt前的I:\a\b\
            outFile.getParentFile().mkdirs(); //不存在则创建父目录
        }*/
        outFile.getParentFile().mkdirs();
        Writer out;

        try {
            Template t = freemarkerConfig.getTemplate(templateName);
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile), StandardCharsets.UTF_8));
            Map<String, List<SameImageInfo>> map = new HashMap<>();
            map.put("sameImageInfoList",sameImageInfoList);
            t.process(map, out);
            out.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null, e.getMessage(),"错误",JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
