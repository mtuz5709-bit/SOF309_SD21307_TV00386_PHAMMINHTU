package com.titangear.config;

import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{ AppConfig.class, JpaConfig.class, SecurityConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }

    @Override
    protected Filter[] getServletFilters() {
        // Khai các filter chạy trước mọi request. Hiện chỉ có chuỗi filter của Spring Security.
        // Không thêm OpenEntityManagerInViewFilter vào đây: nó tìm EntityManagerFactory
        // ở root context, mà dự án để toàn bộ cấu hình ở servlet context nên mọi request
        // sẽ trả 500.
        return new Filter[]{
                new DelegatingFilterProxy("springSecurityFilterChain")
        };
    }

    // Cấu hình multipart cho upload file
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(
                new MultipartConfigElement(
                        System.getProperty("java.io.tmpdir"), // thư mục temp
                        5 * 1024 * 1024,  // max file size: 5MB
                        10 * 1024 * 1024, // max request size: 10MB
                        0
                )
        );
    }
}