package kami.picturefinder.config;

import kami.picturefinder.util.JedisUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@ComponentScan("kami.picturefinder")
@Configuration
@PropertySource(value = "classpath:init.properties", encoding = "UTF-8")
public class RootConfig {

    @Value("${freemarker.ftlpath}")
    private String ftlpath;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JedisUtil jedisUtil(){
        return JedisUtil.getInstance();
    }

    @Bean
    public freemarker.template.Configuration freemarkerConfig(){
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_28);
        // 设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
        // 这里我们的模板是放在ftl文件夹下面
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(this.getClass(),ftlpath);

        return configuration;
    }
}
