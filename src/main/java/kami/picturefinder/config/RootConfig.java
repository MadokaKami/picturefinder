package kami.picturefinder.config;

import kami.picturefinder.util.JedisUtil;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@ComponentScan("kami.picturefinder")
@Configuration
@PropertySource(value = "classpath:init.properties", encoding = "UTF-8")
public class RootConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JedisUtil jedisUtil(){
        return JedisUtil.getInstance();
    }
}
