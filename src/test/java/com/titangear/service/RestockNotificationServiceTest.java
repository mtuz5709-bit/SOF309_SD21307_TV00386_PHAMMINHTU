package com.titangear.service;

import com.titangear.model.Product;
import com.titangear.model.RestockNotification;
import com.titangear.repository.RestockNotificationRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test "báo hàng về": không đăng ký trùng, và khi hàng về thì gửi mail + đánh dấu đã gửi.
 */
@ExtendWith(MockitoExtension.class)
class RestockNotificationServiceTest {

    @Mock private RestockNotificationRepository repo;
    @Mock private JavaMailSender mailSender;
    @Mock private ProductService productService;
    @Mock private ProductVariantService variantService;

    @InjectMocks private RestockNotificationService service;

    @Test
    void subscribe_boQuaKhiDaDangKyChuaGui() {
        when(repo.existsByProductIdAndVariantIdIsNullAndEmailAndNotifiedFalse(1, "a@b.com")).thenReturn(true);
        service.subscribe(1, null, "a@b.com");
        verify(repo, never()).save(any());          // trùng -> không lưu thêm
    }

    @Test
    void subscribe_luuKhiChuaCoAiDangKy() {
        when(repo.existsByProductIdAndVariantIdIsNullAndEmailAndNotifiedFalse(1, "new@b.com")).thenReturn(false);
        service.subscribe(1, null, "new@b.com");
        verify(repo, times(1)).save(any(RestockNotification.class));
    }

    @Test
    void subscribe_theoBienThe_taoDangKyRiengChoMauDo() {
        when(repo.existsByProductIdAndVariantIdAndEmailAndNotifiedFalse(1, 5, "do@b.com")).thenReturn(false);
        service.subscribe(1, 5, "do@b.com");
        verify(repo, times(1)).save(any(RestockNotification.class));
    }

    @Test
    void notifyRestock_guiMailVaDanhDauDaGui() {
        RestockNotification sub = new RestockNotification();
        sub.setProductId(1);
        sub.setEmail("x@y.com");
        sub.setNotified(false);
        when(repo.findByProductIdAndVariantIdIsNullAndNotifiedFalse(1))
                .thenReturn(new ArrayList<>(List.of(sub)));
        Product p = new Product();
        p.setName("Chuột X");
        when(productService.findById(1)).thenReturn(Optional.of(p));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        service.notifyRestock(1, null);

        verify(mailSender).send(any(MimeMessage.class));   // có gửi mail
        assertTrue(sub.getNotified());                      // đánh dấu đã gửi
        verify(repo).save(sub);
    }

    @Test
    void notifyRestock_nhapMauDo_khongLamPhienNguoiChoMauKhac() {
        // Người chờ ĐÚNG màu Đỏ (variant 5)
        RestockNotification choDo = new RestockNotification();
        choDo.setProductId(1);
        choDo.setVariantId(5);
        choDo.setEmail("do@b.com");
        choDo.setNotified(false);

        when(repo.findByProductIdAndVariantIdIsNullAndNotifiedFalse(1)).thenReturn(new ArrayList<>());
        when(repo.findByProductIdAndVariantIdAndNotifiedFalse(1, 5))
                .thenReturn(new ArrayList<>(List.of(choDo)));
        Product p = new Product();
        p.setName("Chuột X");
        when(productService.findById(1)).thenReturn(Optional.of(p));
        when(variantService.findById(5)).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        service.notifyRestock(1, 5);

        // chỉ gửi ĐÚNG 1 mail — cho người chờ màu Đỏ
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertTrue(choDo.getNotified());
    }
}
