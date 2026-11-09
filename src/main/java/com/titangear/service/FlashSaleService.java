package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.FlashSale;
import com.titangear.model.FlashSaleItem;
import com.titangear.model.Product;
import com.titangear.repository.FlashSaleItemRepository;
import com.titangear.repository.FlashSaleRepository;
import com.titangear.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlashSaleService {

    @Autowired private FlashSaleRepository flashSaleRepository;
    @Autowired private FlashSaleItemRepository flashSaleItemRepository;
    @Autowired private ProductRepository productRepository;

    // ------------------------------------------------------------
    // TẠO MỚI
    // ------------------------------------------------------------
    @Transactional
    public FlashSale createFlashSale(FlashSale flashSale,
                                      List<Integer> productIds,
                                      List<String> discountTypes,
                                      List<BigDecimal> discountValues) {
        validateAndNormalize(flashSale);
        FlashSale saved = flashSaleRepository.save(flashSale);
        saveItems(saved, productIds, discountTypes, discountValues);
        return saved;
    }

    // ------------------------------------------------------------
    // SỬA — xóa hết item cũ rồi ghi lại theo danh sách mới gửi lên
    // ------------------------------------------------------------
    @Transactional
    public FlashSale updateFlashSale(Integer id, FlashSale flashSale,
                                      List<Integer> productIds,
                                      List<String> discountTypes,
                                      List<BigDecimal> discountValues) {
        flashSale.setId(id);
        validateAndNormalize(flashSale);
        FlashSale saved = flashSaleRepository.save(flashSale);
        flashSaleItemRepository.deleteByFlashSaleId(id);
        flashSaleItemRepository.flush();
        saveItems(saved, productIds, discountTypes, discountValues);
        return saved;
    }

    private void saveItems(FlashSale flashSale, List<Integer> productIds,
                            List<String> discountTypes, List<BigDecimal> discountValues) {
        if (productIds == null || productIds.isEmpty()) {
            throw new LoiNghiepVu("Cần chọn ít nhất 1 sản phẩm áp dụng flash sale");
        }
        // Chặn TRÙNG sản phẩm ngay trong cùng một đợt. Chốt chống chồng lịch bên
        // dưới loại trừ chính đợt đang lưu (excludeSaleId) nên không bắt được ca
        // này. Hậu quả nếu để lọt: 1 sản phẩm có 2 mức giảm cùng lúc, mà truy vấn
        // của trang danh sách và trang chi tiết sắp xếp khác nhau -> hai trang
        // hiện hai giá khác nhau cho cùng một món.
        java.util.Set<Integer> daThem = new java.util.HashSet<>();

        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            if (productId == null) continue;

            String typeStr = (discountTypes != null && i < discountTypes.size()) ? discountTypes.get(i) : null;
            BigDecimal value = (discountValues != null && i < discountValues.size()) ? discountValues.get(i) : null;
            if (typeStr == null || value == null) continue;

            FlashSaleItem.DiscountType type = FlashSaleItem.DiscountType.valueOf(typeStr);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm #" + productId));

            if (!daThem.add(productId)) {
                throw new LoiNghiepVu("Sản phẩm \"" + product.getName()
                        + "\" bị thêm 2 lần trong cùng một đợt. Mỗi sản phẩm chỉ được 1 mức giảm.");
            }

            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new LoiNghiepVu("Giá trị giảm giá phải lớn hơn 0");
            }
            // KHÔNG cho giảm về 0đ. Gõ nhầm 100 thay vì 10, hoặc thừa một số 0 ở
            // mức giảm tiền mặt, là hàng bán giá 0đ mà không lớp nào phía sau
            // chặn lại: đơn vẫn tạo, vẫn xuất kho, doanh thu ghi 0.
            if (type == FlashSaleItem.DiscountType.PERCENT) {
                if (value.compareTo(new BigDecimal("100")) >= 0) {
                    throw new LoiNghiepVu("Phần trăm giảm của \"" + product.getName()
                            + "\" phải nhỏ hơn 100% — giảm 100% là bán giá 0đ.");
                }
            } else {
                BigDecimal gia = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                if (value.compareTo(gia) >= 0) {
                    throw new LoiNghiepVu("Mức giảm của \"" + product.getName()
                            + "\" (" + value.toPlainString() + "đ) bằng hoặc lớn hơn giá bán ("
                            + gia.toPlainString() + "đ) — sẽ thành bán giá 0đ.");
                }
            }

            // Chặn 1 sản phẩm bị áp 2 flash sale chồng thời gian nhau
            Integer excludeId = (flashSale.getId() == null) ? -1 : flashSale.getId();
            List<FlashSaleItem> overlapping = flashSaleItemRepository.findOverlapping(
                    productId, excludeId, flashSale.getStartAt(), flashSale.getEndAt());
            if (!overlapping.isEmpty()) {
                throw new LoiNghiepVu("Sản phẩm \"" + product.getName()
                        + "\" đã có 1 flash sale khác đang chạy trong khoảng thời gian này");
            }

            FlashSaleItem item = new FlashSaleItem();
            item.setFlashSale(flashSale);
            item.setProduct(product);
            item.setDiscountType(type);
            item.setDiscountValue(value);
            flashSaleItemRepository.save(item);
        }
    }

    private void validateAndNormalize(FlashSale flashSale) {
        if (flashSale.getStartAt() == null || flashSale.getEndAt() == null) {
            throw new LoiNghiepVu("Vui lòng nhập đầy đủ thời gian bắt đầu/kết thúc");
        }
        if (!flashSale.getEndAt().isAfter(flashSale.getStartAt())) {
            throw new LoiNghiepVu("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (flashSale.getDisplayType() == null) {
            throw new LoiNghiepVu("Vui lòng chọn kiểu hiển thị (Banner hoặc Text bar)");
        }

        if (flashSale.getDisplayType() == FlashSale.DisplayType.BANNER) {
            if (flashSale.getBannerImageUrl() == null || flashSale.getBannerImageUrl().isBlank()
                    || flashSale.getBannerTitle() == null || flashSale.getBannerTitle().isBlank()) {
                throw new LoiNghiepVu("Banner cần có ảnh và tiêu đề");
            }
            flashSale.setTextBarContent(null);
        } else {
            if (flashSale.getTextBarContent() == null || flashSale.getTextBarContent().isBlank()) {
                throw new LoiNghiepVu("Cần nhập nội dung dòng chữ hiển thị");
            }
            flashSale.setBannerImageUrl(null);
            flashSale.setBannerTitle(null);
            flashSale.setBannerSubtitle(null);
            flashSale.setBannerCtaText(null);
            flashSale.setBannerCtaLink(null);
        }
    }

    @Transactional
    public void deleteFlashSale(Integer id) {
        flashSaleItemRepository.deleteByFlashSaleId(id);
        flashSaleRepository.deleteById(id);
    }

    @Transactional
    public void toggleActive(Integer id) {
        flashSaleRepository.findById(id).ifPresent(fs -> {
            boolean batLen = !Boolean.TRUE.equals(fs.getIsActive());

            // BẬT LẠI phải kiểm chồng lịch lần nữa. Chốt findOverlapping chỉ đếm
            // đợt đang isActive = true, nên có kẽ hở: tắt đợt A -> tạo đợt B cùng
            // khoảng thời gian (qua được vì A đang tắt) -> bật lại A là hai đợt
            // chồng nhau. Lúc đó 1 sản phẩm có 2 mức giảm cùng lúc.
            if (batLen) {
                for (FlashSaleItem it : flashSaleItemRepository.findByFlashSaleId(id)) {
                    if (it.getProduct() == null) continue;
                    List<FlashSaleItem> trung = flashSaleItemRepository.findOverlapping(
                            it.getProduct().getId(), id, fs.getStartAt(), fs.getEndAt());
                    if (!trung.isEmpty()) {
                        throw new LoiNghiepVu("Không bật được đợt này: sản phẩm \""
                                + it.getProduct().getName() + "\" đang có flash sale khác chạy"
                                + " trùng khoảng thời gian. Hãy tắt đợt kia trước.");
                    }
                }
            }

            fs.setIsActive(batLen);
            flashSaleRepository.save(fs);
        });
    }

    public List<FlashSale> getAllFlashSales() {
        return flashSaleRepository.findAllByOrderByStartAtDesc();
    }

    public FlashSale findById(Integer id) {
        return flashSaleRepository.findById(id).orElse(null);
    }

    public List<FlashSaleItem> getItemsBySaleId(Integer id) {
        return flashSaleItemRepository.findByFlashSaleId(id);
    }

    // Map flashSaleId -> số sản phẩm áp dụng (1 query, dùng cho trang danh sách admin)
    public Map<Integer, Long> getItemCountsBySale() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : flashSaleItemRepository.countItemsGroupedBySale()) {
            counts.put((Integer) row[0], (Long) row[1]);
        }
        return counts;
    }

    // ------------------------------------------------------------
    // HIỂN THỊ TRÊN STOREFRONT
    // ------------------------------------------------------------
    public List<FlashSale> getActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository
                .findByDisplayTypeAndIsActiveTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByDisplayOrderAscStartAtAsc(
                        FlashSale.DisplayType.BANNER, now, now);
    }

    public List<FlashSale> getActiveTextBars() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository
                .findByDisplayTypeAndIsActiveTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByDisplayOrderAscStartAtAsc(
                        FlashSale.DisplayType.TEXT_BAR, now, now);
    }

    // Tính giá sau giảm cho một sản phẩm, dùng ở trang chi tiết. Luôn tính trực tiếp
    // tại thời điểm xem, không lưu lại giá này ở đâu cả, để chỉ có một nguồn duy nhất.
    public EffectivePrice getEffectivePrice(Product product) {
        List<FlashSaleItem> items = flashSaleItemRepository
                .findActiveItemsForProduct(product.getId(), LocalDateTime.now());
        FlashSaleItem item = items.isEmpty() ? null : items.get(0);
        return computeEffectivePrice(product.getPrice(), item);
    }

    // Tính giá sau giảm cho NHIỀU sản phẩm cùng lúc (trang danh sách) — chỉ
    // 1 query lấy toàn bộ flash sale item đang active, tránh N+1 query khi
    // lặp qua từng sản phẩm riêng lẻ.
    public Map<Integer, EffectivePrice> getEffectivePrices(List<Product> products) {
        List<FlashSaleItem> activeItems = flashSaleItemRepository.findAllActiveItems(LocalDateTime.now());

        Map<Integer, FlashSaleItem> itemByProductId = new HashMap<>();
        for (FlashSaleItem item : activeItems) {
            // Về lý thuyết không nên có >1 active item/sản phẩm (đã chặn overlap lúc tạo)
            itemByProductId.putIfAbsent(item.getProduct().getId(), item);
        }

        Map<Integer, EffectivePrice> result = new HashMap<>();
        for (Product p : products) {
            result.put(p.getId(), computeEffectivePrice(p.getPrice(), itemByProductId.get(p.getId())));
        }
        return result;
    }

    // Các sản phẩm ĐANG được giảm giá — dùng cho trang /flash-sale.
    // Dùng lại đúng truy vấn của getEffectivePrices (chỉ 1 query) nên không lo
    // hai chỗ hiểu "đang giảm" khác nhau. Chỉ lấy sản phẩm còn bán (isActive).
    public List<Product> getSanPhamDangGiam() {
        List<Product> ds = new ArrayList<>();
        java.util.Set<Integer> daCo = new java.util.HashSet<>();
        for (FlashSaleItem item : flashSaleItemRepository.findAllActiveItems(LocalDateTime.now())) {
            Product p = item.getProduct();
            if (p == null || !Boolean.TRUE.equals(p.getIsActive())) continue;
            if (daCo.add(p.getId())) ds.add(p);
        }
        return ds;
    }

    // Thời điểm đợt giảm giá gần nhất kết thúc — trang /flash-sale đếm ngược tới mốc này.
    // Trả null khi không có đợt nào đang chạy.
    public LocalDateTime getMocKetThucGanNhat() {
        LocalDateTime som = null;
        for (FlashSaleItem item : flashSaleItemRepository.findAllActiveItems(LocalDateTime.now())) {
            LocalDateTime ket = item.getFlashSale() != null ? item.getFlashSale().getEndAt() : null;
            if (ket != null && (som == null || ket.isBefore(som))) som = ket;
        }
        return som;
    }

    private EffectivePrice computeEffectivePrice(BigDecimal original, FlashSaleItem item) {
        if (item == null) {
            return new EffectivePrice(original, original, null);
        }

        BigDecimal salePrice;
        int percentOff;

        if (item.getDiscountType() == FlashSaleItem.DiscountType.PERCENT) {
            BigDecimal discountFraction = item.getDiscountValue().divide(new BigDecimal("100"));
            BigDecimal discountAmount = original.multiply(discountFraction);
            salePrice = original.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP);
            percentOff = item.getDiscountValue().setScale(0, RoundingMode.HALF_UP).intValue();
        } else {
            salePrice = original.subtract(item.getDiscountValue());
            if (salePrice.compareTo(BigDecimal.ZERO) < 0) salePrice = BigDecimal.ZERO;
            percentOff = original.compareTo(BigDecimal.ZERO) > 0
                    ? original.subtract(salePrice)
                        .multiply(new BigDecimal("100"))
                        .divide(original, 0, RoundingMode.HALF_UP)
                        .intValue()
                    : 0;
        }

        return new EffectivePrice(original, salePrice, percentOff);
    }

    public static class EffectivePrice {
        public final BigDecimal originalPrice;
        public final BigDecimal salePrice;
        public final Integer percentOff; // null nghĩa là không có sale đang chạy

        public EffectivePrice(BigDecimal originalPrice, BigDecimal salePrice, Integer percentOff) {
            this.originalPrice = originalPrice;
            this.salePrice = salePrice;
            this.percentOff = percentOff;
        }

        public boolean hasSale() { return percentOff != null; }

        // Getter chuẩn JavaBean để JSP EL truy cập được qua ${...hasSale}
        public boolean isHasSale() { return percentOff != null; }

        public BigDecimal getOriginalPrice() { return originalPrice; }
        public BigDecimal getSalePrice() { return salePrice; }
        public Integer getPercentOff() { return percentOff; }
    }
}
