package com.poly.sof309.services;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.CartItem;
import com.titangear.model.Product;
import com.titangear.repository.CartItemRepository;
import com.titangear.repository.ProductRepository;
import com.titangear.repository.ProductVariantRepository;
import com.titangear.service.CartService;
import com.titangear.service.SystemSettingService;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;

/**
 * Unit Test Back-End (BE) tầng Service cho Giỏ hàng (CartService)
 * Chia 2 Suite:
 * - @Tag("smoke"): Thêm mới, cộng dồn, cập nhật số lượng, xóa sản phẩm khỏi giỏ.
 * - @Tag("regression"): Số lượng <= 0, vượt giới hạn tối đa (10), sản phẩm ngừng bán.
 */
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private CartService cartService;

    private List<Product> dsSanPham;
    private List<CartItem> dsGioHang;

    @BeforeEach
    public void setUp() {
        dsSanPham = new ArrayList<>();
        dsGioHang = new ArrayList<>();

        Product spDangBan = new Product();
        spDangBan.setId(1);
        spDangBan.setName("Chuột ATK F1 Ultimate");
        spDangBan.setPrice(new BigDecimal("1850000"));
        spDangBan.setIsActive(true);
        dsSanPham.add(spDangBan);

        Product spNgungBan = new Product();
        spNgungBan.setId(99);
        spNgungBan.setName("Chuột đã ngừng bán");
        spNgungBan.setPrice(new BigDecimal("900000"));
        spNgungBan.setIsActive(false);
        dsSanPham.add(spNgungBan);

        lenient().when(systemSettingService.getValue("MAX_QTY_PER_PRODUCT_PER_ORDER", "10"))
                .thenReturn("10");

        lenient().when(productRepository.findById(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            return dsSanPham.stream().filter(p -> p.getId().equals(id)).findFirst();
        });

        lenient().when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem item = inv.getArgument(0);
            if (item.getId() == null) {
                item.setId(dsGioHang.size() + 1);
                dsGioHang.add(item);
            }
            return item;
        });

        lenient().when(cartItemRepository.findByUserId(anyInt())).thenAnswer(inv -> {
            Integer userId = inv.getArgument(0);
            return dsGioHang.stream()
                    .filter(i -> i.getUser() != null && i.getUser().getId().equals(userId))
                    .toList();
        });

        lenient().when(cartItemRepository.findByUserProductAndVariant(anyInt(), anyInt(), any()))
                .thenAnswer(inv -> {
                    Integer userId = inv.getArgument(0);
                    Integer productId = inv.getArgument(1);
                    Integer variantId = inv.getArgument(2);
                    return dsGioHang.stream()
                            .filter(i -> i.getUser().getId().equals(userId)
                                    && i.getProduct().getId().equals(productId)
                                    && ((variantId == null && i.getVariant() == null)
                                    || (variantId != null && i.getVariant() != null
                                    && i.getVariant().getId().equals(variantId))))
                            .findFirst();
                });

        lenient().when(cartItemRepository.findByIdAndUserId(anyInt(), anyInt())).thenAnswer(inv -> {
            Integer itemId = inv.getArgument(0);
            Integer userId = inv.getArgument(1);
            return dsGioHang.stream()
                    .filter(i -> i.getId().equals(itemId) && i.getUser().getId().equals(userId))
                    .findFirst();
        });

        lenient().doAnswer(inv -> {
            CartItem target = inv.getArgument(0);
            dsGioHang.removeIf(i -> i.getId().equals(target.getId()));
            return null;
        }).when(cartItemRepository).delete(any(CartItem.class));
    }

    // =========================================================================
    // 1. SMOKE TEST SUITE (@Tag("smoke"))
    // =========================================================================

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_CART_01: Thêm 2 Chuột ATK F1 (#1) cho userId = 1 -> giỏ có đúng 1 dòng SL = 2")
    public void testThemVaoGioThanhCong() {
        cartService.addToCart(1, 1, 2, null);

        List<CartItem> gio = cartService.getCart(1);
        assertEquals(1, gio.size());
        assertEquals(1, gio.get(0).getProduct().getId());
        assertEquals(2, gio.get(0).getQuantity());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_CART_02: Thêm trùng sản phẩm #1 (2 + 3) -> cộng dồn thành 1 dòng SL = 5")
    public void testThemTrungSanPhamCongDonSoLuong() {
        cartService.addToCart(1, 1, 2, null);
        cartService.addToCart(1, 1, 3, null);

        List<CartItem> gio = cartService.getCart(1);
        assertEquals(1, gio.size(), "Cùng sản phẩm phải gộp thành 1 dòng trong giỏ");
        assertEquals(5, gio.get(0).getQuantity());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_CART_03: Cập nhật số lượng dòng giỏ hàng từ 2 lên 4")
    public void testCapNhatSoLuongTrongGio() {
        cartService.addToCart(1, 1, 2, null);
        Integer itemId = cartService.getCart(1).get(0).getId();

        cartService.updateQuantity(1, itemId, 4);

        List<CartItem> gio = cartService.getCart(1);
        assertEquals(1, gio.size());
        assertEquals(4, gio.get(0).getQuantity());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_CART_04: Xóa sản phẩm khỏi giỏ hàng -> giỏ hàng trống")
    public void testXoaSanPhamKhoiGio() {
        cartService.addToCart(1, 1, 2, null);
        Integer itemId = cartService.getCart(1).get(0).getId();

        cartService.removeItem(1, itemId);

        assertTrue(cartService.getCart(1).isEmpty(), "Giỏ hàng phải trống sau khi xóa");
    }

    // =========================================================================
    // 2. REGRESSION TEST SUITE (@Tag("regression"))
    // =========================================================================

    @ParameterizedTest
    @Tag("regression")
    @DisplayName("TC_REG_CART_01: Thêm số lượng <= 0 (0, -3) -> ném LoiNghiepVu")
    @CsvSource({"0", "-3"})
    public void testThemSoLuongKhongHopLe(int quantity) {
        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class,
                () -> cartService.addToCart(1, 1, quantity, null));
        assertEquals("Số lượng phải lớn hơn 0.", ex.getMessage());
        assertTrue(dsGioHang.isEmpty());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC_REG_CART_02: Thêm quá giới hạn 10 sản phẩm/đơn (quantity = 11) -> ném LoiNghiepVu")
    public void testThemVuotGioiHanToiDa() {
        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class,
                () -> cartService.addToCart(1, 1, 11, null));
        assertEquals("Bạn chỉ được mua tối đa 10 sản phẩm này trong 1 đơn hàng.", ex.getMessage());
        assertTrue(dsGioHang.isEmpty());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC_REG_CART_03: Thêm sản phẩm đã ngừng bán (id = 99) -> ném IllegalArgumentException")
    public void testThemSanPhamNgungBan() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1, 99, 1, null));
        assertEquals("Sản phẩm không tồn tại hoặc đã ngừng bán.", ex.getMessage());
        assertTrue(dsGioHang.isEmpty());
    }
}
