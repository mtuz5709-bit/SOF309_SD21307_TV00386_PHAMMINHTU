package com.titangear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Properties;

@Configuration
@EnableWebMvc
// Bật tác vụ chạy nền theo lịch — hiện dùng cho DonQuaHanService (tự hủy đơn
// chuyển khoản quá 15 phút chưa thanh toán). Thiếu dòng này thì @Scheduled bị
// bỏ qua LẶNG LẼ: không lỗi, không log, chỉ đơn giản là không bao giờ chạy.
@org.springframework.scheduling.annotation.EnableScheduling
// Bật @Async — dùng cho việc gửi email báo hàng về. Gửi mail đồng bộ nghĩa là
// thủ kho bấm "Nhập kho" rồi ngồi chờ hệ thống gửi xong TỪNG email cho từng
// người đăng ký; 50 người là hơn một phút màn hình trắng. Cũng bị bỏ qua LẶNG LẼ
// giống @Scheduled nếu thiếu dòng này: không lỗi, chỉ là chạy đồng bộ như cũ.
@org.springframework.scheduling.annotation.EnableAsync
@ComponentScan(basePackages = "com.titangear")
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    private Environment env;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // Phân quyền chi tiết tập trung: mọi request /admin/** đi qua đây,
        // method nào gắn @RequirePermission sẽ bị kiểm tra tại PermissionInterceptor
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Trỏ đường dẫn /static/images/ tới hai nơi: thư mục upload khai trong
        // application.properties trước, không thấy file thì lấy ảnh đóng sẵn trong WAR.
        // Ảnh upload để ngoài thư mục target nên không mất khi build lại.
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations(
                        "file:" + env.getProperty("upload.dir"),
                        "/static/images/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    /* Lớp này KHÔNG khai đè configureMessageConverters, để Spring tự đăng ký đủ
       bộ chuyển đổi mặc định: String, byte[], Resource, form và Jackson (Jackson
       có sẵn vì jackson-databind nằm trong classpath), nên trả JSON vẫn chạy.

       Khai đè hàm đó sẽ THAY THẾ toàn bộ danh sách mặc định chứ không thêm vào,
       khiến ứng dụng chỉ còn mỗi bộ JSON:
         - Controller trả String bị Jackson bọc thành JSON: "ABC" (dính dấu nháy)
         - Trả text/plain hoặc byte[] (tải file) thì không bộ nào ghi được -> lỗi 500 */

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(465);
        mailSender.setUsername(env.getProperty("mail.username"));
        mailSender.setPassword(env.getProperty("mail.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return mailSender;
    }
}