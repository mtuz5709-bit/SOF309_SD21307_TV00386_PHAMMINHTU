package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.CartItem;
import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.User;
import com.titangear.repository.CartItemRepository;
import com.titangear.repository.ProductRepository;
import com.titangear.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private SystemSettingService systemSettingService;

    private static final String MAX_QTY_KEY = "MAX_QTY_PER_PRODUCT_PER_ORDER";

    private int getMaxQtyLimit() {
        try {
            return Integer.parseInt(systemSettingService.getValue(MAX_QTY_KEY, "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    // Lấy toàn bộ giỏ hàng của khách
    public List<CartItem> getCart(Integer userId) {
        return cartItemRepository.findByUserId(userId);
    }

    // Thêm sản phẩm vào giỏ (có thể kèm biến thể màu/phiên bản)
    public void addToCart(Integer userId, Integer productId, Integer quantity, Integer variantId) {
        int maxQty = getMaxQtyLimit();

        // Chặn số lượng âm và bằng 0. Các chốt khác đều so theo chiều "lớn hơn" nên
        // số âm lọt hết, mà số âm thì thành tiền âm và tổng đơn bị trừ đi.
        if (quantity == null || quantity <= 0) {
            throw new LoiNghiepVu("Số lượng phải lớn hơn 0.");
        }

        // Tra sản phẩm từ CSDL để chắc nó có thật và đang bán. Không tra thì gõ tay
        // một id không tồn tại sẽ văng lỗi khóa ngoại, còn hàng đã ngừng bán vẫn thêm
        // vào giỏ được.
        Product sanPham = productRepository.findById(productId)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sản phẩm không tồn tại hoặc đã ngừng bán."));

        // Bắt buộc chọn biến thể khi sản phẩm có biến thể, vì tồn kho tính theo biến
        // thể. Dòng giỏ thiếu biến thể vẫn tạo được đơn nhưng lúc xuất kho không tìm
        // ra lô nào, đơn kẹt vĩnh viễn ở trạng thái Chờ hàng.
        boolean coBienThe = sanPham.getVariants() != null && sanPham.getVariants().stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getIsActive()));
        if (coBienThe && variantId == null) {
            throw new LoiNghiepVu(
                    "Sản phẩm này có nhiều phiên bản, vui lòng chọn một phiên bản trước khi thêm vào giỏ.");
        }

        // Biến thể phải THUỘC ĐÚNG sản phẩm đó — không kiểm thì gán được biến thể
        // của sản phẩm khác vào dòng giỏ, xuất kho sẽ trừ nhầm hàng.
        ProductVariant bienThe = null;
        if (variantId != null) {
            bienThe = productVariantRepository.findById(variantId)
                    .filter(v -> v.getProduct() != null
                            && v.getProduct().getId().equals(productId)
                            && Boolean.TRUE.equals(v.getIsActive()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Phiên bản bạn chọn không hợp lệ cho sản phẩm này."));
        }
        // Tìm dòng giỏ theo đúng bộ ba (khách, sản phẩm, biến thể): cùng màu thì cộng
        // dồn số lượng, khác màu thì tạo dòng mới.
        Optional<CartItem> existing =
                cartItemRepository.findByUserProductAndVariant(userId, productId, variantId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > maxQty) {
                throw new LoiNghiepVu(
                        "Bạn chỉ được mua tối đa " + maxQty + " sản phẩm này trong 1 đơn hàng"
                                + " (trong giỏ đã có " + item.getQuantity() + ").");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            if (quantity > maxQty) {
                throw new LoiNghiepVu(
                        "Bạn chỉ được mua tối đa " + maxQty + " sản phẩm này trong 1 đơn hàng.");
            }
            CartItem item = new CartItem();
            User user = new User();
            user.setId(userId);
            item.setUser(user);
            // Dùng bản đã tra ở trên, không dựng Product rỗng theo id nữa
            item.setProduct(sanPham);
            item.setVariant(bienThe);
            item.setQuantity(quantity);
            item.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(item);
        }
    }

    // Đổi số lượng một dòng giỏ. Nhận kèm userId và chỉ cập nhật nếu dòng đó thuộc
    // về đúng người đang đăng nhập, để không ai sửa được giỏ của người khác.
    public void updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        int maxQty = getMaxQtyLimit();
        if (quantity > maxQty) {
            throw new LoiNghiepVu(
                    "Bạn chỉ được mua tối đa " + maxQty + " sản phẩm này trong 1 đơn hàng.");
        }
        cartItemRepository.findByIdAndUserId(cartItemId, userId).ifPresent(item -> {
            // Dòng thuộc combo đi theo cả bộ, không sửa lẻ số lượng được. Giao diện đã
            // ẩn ô số lượng, chặn thêm ở đây cho người gửi thẳng request.
            if (item.getCombo() != null) {
                throw new LoiNghiepVu(
                        "Sản phẩm này nằm trong combo nên không đổi số lượng riêng được. "
                                + "Bạn có thể xóa cả combo khỏi giỏ.");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        });
        // Không thuộc giỏ của user này -> lặng lẽ bỏ qua (không lộ thông tin
        // item đó có tồn tại hay không cho kẻ dò id)
    }

    // Xóa một dòng khỏi giỏ, chỉ xóa được dòng trong giỏ của chính mình.
    // Xóa một món thuộc combo thì xóa cả bộ, vì bộ thiếu món mà vẫn mang combo_id
    // sẽ tính giảm giá sai.
    @Transactional
    public void removeItem(Integer userId, Integer cartItemId) {
        cartItemRepository.findByIdAndUserId(cartItemId, userId).ifPresent(item -> {
            if (item.getCombo() != null) {
                cartItemRepository.deleteAll(
                        cartItemRepository.findByUserIdAndComboId(userId, item.getCombo().getId()));
            } else {
                cartItemRepository.delete(item);
            }
        });
    }

    // Xóa toàn bộ giỏ hàng sau khi đặt hàng xong
    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // Tổng số lượng hàng trong giỏ — con số hiện trên icon giỏ ở header
    public int countItems(Integer userId) {
        Long n = cartItemRepository.tongSoLuong(userId);
        return n != null ? n.intValue() : 0;
    }
}