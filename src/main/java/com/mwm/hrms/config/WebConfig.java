package com.mwm.hrms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ye project ke root folder mein ek "uploads" folder banayega aur files serve karega
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}