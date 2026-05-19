package com.stayhub.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục "uploads" nằm ở gốc dự án của bạn
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        // Cấu hình: Cứ có request dạng http://localhost:8080/uploads/...
        // thì Spring Boot sẽ tự động móc file tương ứng trong thư mục vật lý ra hiển thị
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}