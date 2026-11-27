package com.poly.sof309.services;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Product;
import com.titangear.repository.ProductRepository;
import com.titangear.service.AuditLogService;
import com.titangear.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * Unit Test Back-End (BE) tầng Service cho Quản lý Sản phẩm (ProductService)
 * Chia 2 Suite:
 * - @Tag("smoke"): CRUD cốt lõi (thêm, lấy danh sách, sửa giá, xóa mềm).
 * - @Tag("regression"): Dữ liệu biên không hợp lệ (tên rỗng, giá <= 0, tồn kho âm, ID không tồn tại).
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductService productService;

    private List<Product> dsSanPham;

    @BeforeEach
    public void setUp() {
        dsSanPham = new ArrayList<>();

        Product p1 = new Product();
        p1.setId(1);
        p1.setName("Chuột ATK F1 Ultimate");
        p1.setPrice(new BigDecimal("1850000"));
        p1.setStockQuantity(20);
        p1.setIsActive(true);
        dsSanPham.add(p1);

        Product p2 = new Product();
        p2.setId(2);
        p2.setName("Bàn phím cơ ATK RS6 Air");
        p2.setPrice(new BigDecimal("2150000"));
        p2.setStockQuantity(15);
        p2.setIsActive(true);
        dsSanPham.add(p2);

        lenient().when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(dsSanPham.size() + 1);
                dsSanPham.add(p);
            }
            return p;
        });

        lenient().when(productRepository.findById(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            return dsSanPham.stream().filter(p -> p.getId().equals(id)).findFirst();
        });

        lenient().when(productRepository.findByIsActiveTrue()).thenAnswer(inv ->
                dsSanPham.stream().filter(p -> Boolean.TRUE.equals(p.getIsActive())).toList());
    }

    // =========================================================================
    // 1. SMOKE TEST SUITE (@Tag("smoke"))
    // =========================================================================

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_PROD_01: Thêm sản phẩm hợp lệ -> trả về Product & lấy ngược khớp dữ liệu")
    public void testThemSanPhamThanhCong() {
        Product spMoi = new Product();
        spMoi.setName("Tai nghe HyperX Cloud III");
        spMoi.setPrice(new BigDecimal("2490000"));
        spMoi.setStockQuantity(10);

        Product created = productService.addProduct(spMoi);

        assertNotNull(created, "Product trả về sau khi thêm không được null");
        assertNotNull(created.getId(), "Product mới phải được cấp ID");

        Optional<Product> found = productService.findById(created.getId());
        assertTrue(found.isPresent(), "Phải lấy lại được sản phẩm vừa thêm trong danh sách");
        assertEquals("Tai nghe HyperX Cloud III", found.get().getName());
        assertEquals(0, new BigDecimal("2490000").compareTo(found.get().getPrice()));
        assertEquals(10, found.get().getStockQuantity());
        assertTrue(found.get().getIsActive());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_PROD_02: Lấy danh sách sản phẩm đang bán trả về đủ 2 sản phẩm mẫu")
    public void testLayDanhSachSanPhamDangBan() {
        List<Product> list = productService.getAllProducts();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Chuột ATK F1 Ultimate", list.get(0).getName());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_PROD_03: Cập nhật giá sản phẩm #1 từ 1850000 -> 1750000 & ghi AuditLog")
    public void testCapNhatGiaSanPhamVaGhiLog() {
        Product spSua = new Product();
        spSua.setId(1);
        spSua.setName("Chuột ATK F1 Ultimate");
        spSua.setPrice(new BigDecimal("1750000"));

        productService.updateProduct(spSua);

        Optional<Product> updated = productService.findById(1);
        assertTrue(updated.isPresent());
        assertEquals(0, new BigDecimal("1750000").compareTo(updated.get().getPrice()));
        verify(auditLogService).ghi(
                eq(AuditLogService.SUA_GIA),
                eq("products"),
                eq(1),
                eq("1850000"),
                eq("1750000"),
                any()
        );
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_PROD_04: Xóa mềm sản phẩm #2 -> isActive = false & danh sách đang bán còn 1")
    public void testXoaMemSanPhamThanhCong() {
        productService.deleteProduct(2);

        Optional<Product> deleted = productService.findById(2);
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
        assertEquals(1, productService.getAllProducts().size());
    }

    // =========================================================================
    // 2. REGRESSION TEST SUITE (@Tag("regression"))
    // =========================================================================

    @ParameterizedTest
    @Tag("regression")
    @DisplayName("TC_REG_PROD_01: Thêm sản phẩm với dữ liệu biên sai -> ném LoiNghiepVu chính xác")
    @CsvSource({
            "'', '1500000', '10', 'Tên sản phẩm không được để trống!'",
            "'Lót chuột Gaming', '0', '10', 'Giá sản phẩm phải lớn hơn 0!'",
            "'Lót chuột Gaming', '-50000', '10', 'Giá sản phẩm phải lớn hơn 0!'",
            "'Lót chuột Gaming', '500000', '-5', 'Số lượng tồn kho không được âm!'"
    })
    public void testThemSanPhamDuLieuBienKhongHopLe(
            String name, String priceStr, int stockQty, String expectedMsg) {
        Product spLoi = new Product();
        spLoi.setName(name);
        spLoi.setPrice(new BigDecimal(priceStr));
        spLoi.setStockQuantity(stockQty);

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> productService.addProduct(spLoi));
        assertEquals(expectedMsg, ex.getMessage());
        assertEquals(2, dsSanPham.size(), "Danh sách sản phẩm không được thêm mới khi bị lỗi");
    }

    @Test
    @Tag("regression")
    @DisplayName("TC_REG_PROD_02: Cập nhật sản phẩm ID không tồn tại (999) -> ném RuntimeException")
    public void testCapNhatSanPhamKhongTonTai() {
        Product spKhongCo = new Product();
        spKhongCo.setId(999);
        spKhongCo.setName("Sản phẩm không tồn tại");
        spKhongCo.setPrice(new BigDecimal("1000000"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(spKhongCo));
        assertEquals("Không tìm thấy sản phẩm #999", ex.getMessage());
    }
}
