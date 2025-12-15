package org.sang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * Created by sang on 2024/01/01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/upload/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        String absolutePath = System.getProperty("user.dir") + uploadPath;
        
        // 映射头像资源
        registry.addResourceHandler("/static/avatar/**")
                .addResourceLocations("file:" + absolutePath + "avatar/");
        
        // 映射其他上传文件资源
        registry.addResourceHandler("/static/upload/**")
                .addResourceLocations("file:" + absolutePath);
        
        // 默认静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}