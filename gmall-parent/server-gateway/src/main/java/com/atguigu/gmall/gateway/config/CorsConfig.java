package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){

        // cors跨域配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");//允许所有请求 进行跨域访问
        configuration.setAllowCredentials(true);//允许跨域请求携带 浏览器带Cookie 用户登录信息
        configuration.addAllowedMethod("*");//允许访问所有方式  POST GET　PUT　。。
        configuration.addAllowedHeader("*");//允许任何请求头的内容

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);
        // cors过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}