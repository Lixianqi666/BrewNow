package com.brewnow.config;

import com.brewnow.interceptor.JwtInterceptor;
import com.brewnow.interceptor.AuditLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AuditLogInterceptor auditLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/cart/**", "/order/**", "/address/**", "/user/avatar", "/merchant/**", "/admin/**",
                        "/favorite/**", "/review/submit", "/review/can-review/**", "/review/product/*/reviewable") // 需要认证的路径
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/admin/login",
                        "/merchant/login",
                        "/product/**", // 商品浏览不需要登录
                        "/recommend/**",
                        "/review/product/**",
                        "/review/summary/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/druid/**", // Druid监控
                        "/error" // 错误页面
                );

        registry.addInterceptor(auditLogInterceptor)
                .addPathPatterns(
                        "/user/login",
                        "/admin/login",
                        "/merchant/login",
                        "/merchant/**",
                        "/admin/**",
                        "/order/**",
                        "/review/**",
                        "/recommend/**",
                        "/system/**"
                )
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/error"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
