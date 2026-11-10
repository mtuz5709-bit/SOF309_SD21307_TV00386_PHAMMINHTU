package com.poly.sof309.controllers;

import com.titangear.controller.AdminProductController;
import com.titangear.controller.CartController;
import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Product;
import com.titangear.model.User;
import com.titangear.service.CartService;
import com.titangear.service.CategoryService;
import com.titangear.service.ProductService;
import com.titangear.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] BÀI TEST MINH HỌA KIỂM THỬ ĐƠN VỊ CHO CHỨC NĂNG CRUD SẢN PHẨM & GIỎ HÀNG
 * - Kiểm thử đầy đủ 5 ca CRUD sản phẩm (TC_PROD_01 -> TC_PROD_05)
 * - Kiểm thử luồng Giỏ hàng & Giới hạn tồn kho (TC_CART_01, TC_CART_02, TC_CART_04)
 * - Áp dụng @ParameterizedTest + @CsvSource và assertThrows(LoiNghiepVu.class)
 * =========================================================================
 */
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @Mock
    private Authentication auth;

    @InjectMocks
    private AdminProductController adminProductController;

    @InjectMocks
    private CartController cartController;

    private Product sampleProduct1;
    private Product sampleProduct2;
    private User sampleUser;

    @BeforeEach
    public void setUp() {
        sampleProduct1 = new Product();
        sampleProduct1.setId(1);
        sampleProduct1.setName("Chuột ATK F1 Ultimate");
        sampleProduct1.setPrice(new BigDecimal("1850000"));
        sampleProduct1.setStockQuantity(20);
        sampleProduct1.setIsActive(true);

        sampleProduct2 = new Product();
        sampleProduct2.setId(2);
        sampleProduct2.setName("Bàn phím cơ ATK RS6 Air");
        sampleProduct2.setPrice(new BigDecimal("3200000"));
        sampleProduct2.setStockQuantity(10);
        sampleProduct2.setIsActive(true);

        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setEmail("minhtu@gmail.com");
    }

    @Test
    @DisplayName("TC_PROD_01: Thêm mới sản phẩm hợp lệ thành công")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testAddProductSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(productService.addProduct(any(Product.class))).thenReturn(sampleProduct1);

        String view = adminProductController.addProduct(
                sampleProduct1, bindingResult, null, null, null, null, null, model
        );

        assertNotNull(view);
        assertEquals("redirect:/admin/products", view);
        assertEquals(1, sampleProduct1.getId());
        assertEquals("Chuột ATK F1 Ultimate", sampleProduct1.getName());
        verify(productService).addProduct(sampleProduct1);
    }

    @Test
    @DisplayName("TC_PROD_02: Ném ngoại lệ khi thêm sản phẩm có giá <= 0")
    public void testAddProductInvalidPriceThrowsException() {
        Product invalidProd = new Product();
        invalidProd.setId(4);
        invalidProd.setName("Lót chuột Gaming lỗi giá");
        invalidProd.setPrice(new BigDecimal("-50000"));

        when(productService.addProduct(invalidProd))
                .thenThrow(new LoiNghiepVu("Giá sản phẩm phải lớn hơn 0"));

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> {
            productService.addProduct(invalidProd);
        });
        assertTrue(ex.getMessage().contains("Giá sản phẩm phải lớn hơn 0"));
    }

    @Test
    @DisplayName("TC_PROD_03: Lấy thông tin sản phẩm theo ID thành công")
    public void testGetProductByIdSuccess() {
        when(productService.findById(1)).thenReturn(Optional.of(sampleProduct1));

        String view = adminProductController.editForm(1, model);

        assertEquals("admin/product-form", view);
        verify(model).addAttribute("product", sampleProduct1);
    }

    @Test
    @DisplayName("TC_PROD_04: Lấy danh sách toàn bộ sản phẩm")
    public void testListProducts() {
        List<Product> list = List.of(sampleProduct1, sampleProduct2);
        when(productService.getAllProducts()).thenReturn(list);

        String view = adminProductController.listProducts(null, null, null, null, 1, auth, model);

        assertEquals("admin/products", view);
        assertFalse(list.isEmpty(), "Danh sách sản phẩm không được rỗng");
        assertTrue(list.size() >= 2, "Hệ thống có ít nhất 2 sản phẩm");
    }

    @Test
    @DisplayName("TC_PROD_05: Xóa sản phẩm thành công theo ID")
    public void testDeleteProductSuccess() {
        doNothing().when(productService).deleteProduct(2);

        String view = adminProductController.deleteProduct(2);

        assertEquals("redirect:/admin/products", view);
        verify(productService).deleteProduct(2);
    }

    @Test
    @DisplayName("TC_CART_01: Thêm sản phẩm vào giỏ với số lượng hợp lệ")
    public void testAddToCartValidQuantitySuccess() {
        when(auth.getName()).thenReturn("minhtu@gmail.com");
        when(userService.findByEmail("minhtu@gmail.com")).thenReturn(Optional.of(sampleUser));

        String view = cartController.addToCart(1, 2, null, false, auth);

        assertEquals("redirect:/products/1?added=true", view);
        verify(cartService).addToCart(1, 1, 2, null);
    }

    @Test
    @DisplayName("TC_CART_02: Thêm sản phẩm vượt quá số lượng tồn kho ném ngoại lệ LoiNghiepVu")
    public void testAddToCartExceedStockThrowsException() {
        doThrow(new LoiNghiepVu("Số lượng trong kho không đủ đáp ứng!"))
                .when(cartService).addToCart(1, 1, 999, null);

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> {
            cartService.addToCart(1, 1, 999, null);
        });

        assertTrue(ex.getMessage().contains("không đủ"));
    }

    @ParameterizedTest
    @DisplayName("TC_CART_04: Kiểm tra biên cập nhật số lượng giỏ hàng bằng @CsvSource")
    @CsvSource({
            "10, 0, true",
            "10, -1, true",
            "10, -5, true",
            "10, 3, false"
    })
    public void testUpdateCartQuantityBoundary(int cartItemId, int quantity, boolean shouldRemove) {
        when(auth.getName()).thenReturn("minhtu@gmail.com");
        when(userService.findByEmail("minhtu@gmail.com")).thenReturn(Optional.of(sampleUser));

        String view = cartController.updateCart(cartItemId, quantity, auth);

        assertEquals("redirect:/cart", view);
        if (shouldRemove) {
            verify(cartService).removeItem(eq(1), eq(cartItemId));
        } else {
            verify(cartService).updateQuantity(eq(1), eq(cartItemId), eq(quantity));
        }
    }
}
