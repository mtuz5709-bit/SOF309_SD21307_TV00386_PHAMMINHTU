package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.CartItem;
import com.titangear.model.Combo;
import com.titangear.model.ComboItem;
import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.User;
import com.titangear.repository.CartItemRepository;
import com.titangear.repository.ComboItemRepository;
import com.titangear.repository.ComboRepository;
import com.titangear.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Tính giá combo và kiểm các luật bán combo. Mọi nơi cần giá combo (trang chủ,
// trang combo, giỏ hàng, lúc đặt hàng, lúc ghi đơn) đều gọi vào đây.
@Service
public class ComboService {

    private static final Logger log = LoggerFactory.getLogger(ComboService.class);

    @Autowired private ComboRepository    comboRepository;
    @Autowired private FlashSaleService   flashSaleService;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository  productRepository;
    @Autowired private ComboItemRepository comboItemRepository;

    /* ==========================================================
       MÔ HÌNH HIỂN THỊ
       ========================================================== */

    // Một vị trí trong bộ combo: bản mặc định admin đặt sẵn, danh sách phiên bản
    // cùng dòng mà khách được đổi sang, màu mặc định, và còn bán được hay không.
    public static class LuaChonMon {
        private final ComboItem viTri;
        private final List<Product> phienBan;   // luôn có ít nhất bản mặc định
        private final Product macDinh;
        private final ProductVariant mauMacDinh;
        private final boolean banDuoc;

        public LuaChonMon(ComboItem viTri, List<Product> phienBan,
                          Product macDinh, ProductVariant mauMacDinh, boolean banDuoc) {
            this.viTri = viTri; this.phienBan = phienBan;
            this.macDinh = macDinh; this.mauMacDinh = mauMacDinh; this.banDuoc = banDuoc;
        }
        public ComboItem getViTri()            { return viTri; }
        public List<Product> getPhienBan()     { return phienBan; }
        public Product getMacDinh()            { return macDinh; }
        public ProductVariant getMauMacDinh()  { return mauMacDinh; }
        public boolean isBanDuoc()             { return banDuoc; }
        public boolean isCoTheDoiPhienBan()    { return phienBan.size() > 1; }
        public int getSoLuong()                { return viTri.getQuantity() == null ? 1 : viTri.getQuantity(); }
    }

    // Combo kèm sẵn giá đã tính, trả thẳng cho JSP đọc.
    public static class ComboView {
        private final Combo combo;
        private final List<LuaChonMon> cacMon;
        private final BigDecimal giaGoc;
        private final BigDecimal giaBan;
        private final BigDecimal tietKiem;
        private final boolean conHang;
        // Giá sau flash sale của mọi phiên bản, để trang chi tiết đổi giá tại chỗ
        // khi khách chọn bản khác mà không phải tải lại trang.
        private final Map<Integer, BigDecimal> giaHieuLuc;

        public ComboView(Combo combo, List<LuaChonMon> cacMon, BigDecimal giaGoc,
                         BigDecimal giaBan, boolean conHang, Map<Integer, BigDecimal> giaHieuLuc) {
            this.combo = combo; this.cacMon = cacMon;
            this.giaGoc = giaGoc; this.giaBan = giaBan;
            this.tietKiem = giaGoc.subtract(giaBan);
            this.conHang = conHang;
            this.giaHieuLuc = giaHieuLuc;
        }
        public Combo getCombo()             { return combo; }
        public List<LuaChonMon> getCacMon() { return cacMon; }
        public BigDecimal getGiaGoc()       { return giaGoc; }
        public BigDecimal getGiaBan()       { return giaBan; }
        public BigDecimal getTietKiem()     { return tietKiem; }
        public boolean isConHang()          { return conHang; }
        public boolean isCoGiam()           { return tietKiem.compareTo(BigDecimal.ZERO) > 0; }
        public Map<Integer, BigDecimal> getGiaHieuLuc() { return giaHieuLuc; }
    }

    /* ==========================================================
       LẤY COMBO
       ========================================================== */

    // Lấy danh sách combo đang bán, mới nhất trước. gioiHan <= 0 là lấy hết.
    public List<ComboView> layComboDangBan(int gioiHan) {
        List<Combo> combos = comboRepository.findDangBan();
        if (combos.isEmpty()) return Collections.emptyList();

        // Dựng lựa chọn cho mọi combo trước, rồi hỏi giá flash sale một lần cho
        // toàn bộ sản phẩm. Hỏi trong vòng lặp là sinh lỗi N+1 truy vấn.
        Map<Integer, List<Product>> demDong = new HashMap<>();   // cache theo line_id
        Map<Integer, List<LuaChonMon>> monTheoCombo = new LinkedHashMap<>();
        List<Product> tatCaSanPham = new ArrayList<>();

        for (Combo c : combos) {
            List<LuaChonMon> mon = dungCacMon(c, demDong);
            monTheoCombo.put(c.getId(), mon);
            for (LuaChonMon m : mon) tatCaSanPham.addAll(m.getPhienBan());
        }
        Map<Integer, FlashSaleService.EffectivePrice> priceMap =
                flashSaleService.getEffectivePrices(tatCaSanPham);

        List<ComboView> ketQua = new ArrayList<>();
        for (Combo c : combos) {
            ComboView view = tinhGia(c, monTheoCombo.get(c.getId()), priceMap);
            // Bỏ qua combo không bán được: thiếu món, dữ liệu giảm giá sai, hoặc
            // có vị trí không còn lựa chọn nào còn hàng.
            if (view == null || !view.isConHang()) continue;
            ketQua.add(view);
            if (gioiHan > 0 && ketQua.size() >= gioiHan) break;
        }
        return ketQua;
    }

    // Lấy một combo theo id. Không lọc tồn kho như danh sách, nên combo hết hàng
    // vẫn mở được bằng link cũ và trang chi tiết báo "tạm hết".
    public ComboView layTheoId(Integer id) {
        return comboRepository.findDangBanTheoId(id)
                .map(c -> {
                    List<LuaChonMon> mon = dungCacMon(c, new HashMap<>());
                    List<Product> sp = mon.stream()
                            .flatMap(m -> m.getPhienBan().stream()).collect(Collectors.toList());
                    return tinhGia(c, mon, flashSaleService.getEffectivePrices(sp));
                })
                .orElse(null);
    }

    // Dựng danh sách lựa chọn cho từng vị trí trong bộ. Lấy bản admin đặt sẵn làm
    // mặc định; bản đó hết hàng mà dòng còn bản khác thì đẩy bản còn hàng lên thay,
    // để giá hiện trên thẻ đúng với thứ khách mua được.
    private List<LuaChonMon> dungCacMon(Combo combo, Map<Integer, List<Product>> demDong) {
        List<LuaChonMon> ketQua = new ArrayList<>();
        if (combo.getItems() == null) return ketQua;

        for (ComboItem it : combo.getItems()) {
            Product goc = it.getProduct();
            if (goc == null) continue;

            List<Product> tuyChon;
            if (goc.getLine() != null) {
                tuyChon = demDong.computeIfAbsent(goc.getLine().getId(),
                        id -> productRepository.findByLineIdAndIsActiveTrueOrderByIdAsc(id));
                if (tuyChon.isEmpty()) tuyChon = List.of(goc);
            } else {
                tuyChon = List.of(goc);
            }
            final List<Product> phienBan = tuyChon;

            Product macDinh = phienBan.stream()
                    .filter(p -> p.getId().equals(goc.getId()) && banDuoc(p))
                    .findFirst()
                    .orElseGet(() -> phienBan.stream().filter(this::banDuoc).findFirst().orElse(goc));

            ProductVariant mau = chonMauMacDinh(macDinh, it.getVariant());
            boolean banDuoc = banDuoc(macDinh) && tonKho(macDinh, mau) >= (it.getQuantity() == null ? 1 : it.getQuantity());

            ketQua.add(new LuaChonMon(it, phienBan, macDinh, mau, banDuoc));
        }
        return ketQua;
    }

    // Chọn màu gợi ý: giữ màu admin đặt nếu còn dùng được, không thì lấy màu còn
    // hàng đầu tiên.
    private ProductVariant chonMauMacDinh(Product p, ProductVariant admin) {
        List<ProductVariant> dangBan = bienTheDangBan(p);
        if (dangBan.isEmpty()) return null;
        if (admin != null && admin.getProduct() != null
                && admin.getProduct().getId().equals(p.getId())
                && Boolean.TRUE.equals(admin.getIsActive())
                && admin.getStock() != null && admin.getStock() > 0) {
            return admin;
        }
        return dangBan.stream()
                .filter(v -> v.getStock() != null && v.getStock() > 0)
                .findFirst().orElse(dangBan.get(0));
    }

    private List<ProductVariant> bienTheDangBan(Product p) {
        if (p.getVariants() == null) return List.of();
        return p.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .collect(Collectors.toList());
    }

    private boolean banDuoc(Product p) {
        return p != null && Boolean.TRUE.equals(p.getIsActive());
    }

    // Đọc tồn kho của một món. Sản phẩm có biến thể thì tồn nằm ở biến thể, không
    // có biến thể thì đọc ở mức sản phẩm.
    private int tonKho(Product p, ProductVariant v) {
        if (!bienTheDangBan(p).isEmpty()) {
            return (v == null || v.getStock() == null) ? 0 : v.getStock();
        }
        return p.getStockQuantity() == null ? 0 : p.getStockQuantity();
    }

    /* ==========================================================
       TÍNH GIÁ
       ========================================================== */

    // Tính giá hiển thị của combo theo các bản mặc định. Trả null nếu combo không
    // bán được. Công thức:
    //     giá bộ = MIN(tổng giá niêm yết − tiền giảm, tổng giá đang bán lẻ)
    // Lấy MIN để combo không bao giờ đắt hơn mua lẻ, cũng không cộng dồn khuyến mãi
    // tới mức bán dưới giá vốn.
    private ComboView tinhGia(Combo combo, List<LuaChonMon> cacMon,
                              Map<Integer, FlashSaleService.EffectivePrice> priceMap) {
        if (cacMon == null || cacMon.isEmpty()) {
            log.warn("Combo #{} '{}' không có sản phẩm nào -> bỏ qua", combo.getId(), combo.getName());
            return null;
        }

        BigDecimal giaNiemYet = BigDecimal.ZERO;
        BigDecimal giaBanLe   = BigDecimal.ZERO;
        boolean conHang = true;
        Map<Integer, BigDecimal> bangGia = new HashMap<>();

        for (LuaChonMon m : cacMon) {
            Product p = m.getMacDinh();
            if (p == null || p.getPrice() == null) {
                log.warn("Combo #{} có vị trí thiếu sản phẩm/giá -> bỏ qua", combo.getId());
                return null;
            }
            BigDecimal soLuong = BigDecimal.valueOf(m.getSoLuong());
            giaNiemYet = giaNiemYet.add(p.getPrice().multiply(soLuong));
            giaBanLe   = giaBanLe.add(giaHieuLuc(p, priceMap).multiply(soLuong));
            if (!m.isBanDuoc()) conHang = false;

            // Giá của MỌI phiên bản, không chỉ bản mặc định — trang chi tiết cần
            // để đổi bản là giá nhảy ngay, không phải tải lại trang.
            for (Product pb : m.getPhienBan()) {
                bangGia.put(pb.getId(), giaHieuLuc(pb, priceMap));
            }
        }

        BigDecimal giaBan = chotGiaBo(combo, giaNiemYet, giaBanLe);
        if (giaBan == null) return null;
        return new ComboView(combo, cacMon, giaNiemYet, giaBan, conHang, bangGia);
    }

    // Chốt giá bán của bộ. Trả null nếu tiền giảm lớn hơn hoặc bằng tổng giá bộ —
    // combo đó bị ẩn đi thay vì bán giá 0đ. Form admin đã chặn từ đầu, đây là lớp
    // chặn thứ hai phòng khi sau này hạ giá sản phẩm xuống dưới mức giảm.
    private BigDecimal chotGiaBo(Combo combo, BigDecimal giaNiemYet, BigDecimal giaBanLe) {
        BigDecimal tienGiam = combo.getDiscountAmount() == null
                ? BigDecimal.ZERO : combo.getDiscountAmount();
        if (tienGiam.compareTo(giaNiemYet) >= 0) {
            log.warn("Combo #{} '{}' có tiền giảm {} ≥ tổng giá bộ {} -> ẩn combo",
                    combo.getId(), combo.getName(), tienGiam, giaNiemYet);
            return null;
        }
        return giaNiemYet.subtract(tienGiam).min(giaBanLe);
    }

    private BigDecimal giaHieuLuc(Product p, Map<Integer, FlashSaleService.EffectivePrice> priceMap) {
        FlashSaleService.EffectivePrice ep = priceMap.get(p.getId());
        return (ep != null && ep.getSalePrice() != null) ? ep.getSalePrice() : p.getPrice();
    }

    /* ==========================================================
       MUA TRỌN BỘ
       ========================================================== */

    // Đẩy cả bộ combo vào giỏ theo đúng phiên bản và màu khách chọn. Mỗi món thành
    // một dòng giỏ riêng như mua lẻ, chỉ khác là cùng combo_id và có combo_item_id
    // trỏ về vị trí trong bộ, nhờ vậy tồn kho và xuất kho FIFO dùng chung đường cũ.
    // Hai tham số chonSanPham/chonMau có khóa là combo_item_id, giá trị là id khách chọn.
    @Transactional
    public void themVaoGio(Integer userId, Integer comboId,
                           Map<Integer, Integer> chonSanPham,
                           Map<Integer, Integer> chonMau) {
        ComboView view = layTheoId(comboId);
        if (view == null) {
            throw new LoiNghiepVu("Combo không tồn tại hoặc đã ngừng bán.");
        }
        if (cartItemRepository.existsByUserIdAndComboId(userId, comboId)) {
            throw new LoiNghiepVu("Combo này đã có trong giỏ hàng của bạn rồi.");
        }

        User user = new User();
        user.setId(userId);
        List<CartItem> sePhaiLuu = new ArrayList<>();

        for (LuaChonMon m : view.getCacMon()) {
            Integer viTriId = m.getViTri().getId();
            int soLuong = m.getSoLuong();

            // --- Phiên bản ---
            // CHỈ chấp nhận id nằm trong danh sách phiên bản của ĐÚNG vị trí này.
            // Không kiểm thì khách sửa request là nhét được sản phẩm 100k vào bộ
            // rồi vẫn hưởng nguyên mức giảm.
            Integer spId = chonSanPham == null ? null : chonSanPham.get(viTriId);
            Product sp = (spId == null)
                    ? m.getMacDinh()
                    : m.getPhienBan().stream().filter(p -> p.getId().equals(spId))
                        .findFirst().orElse(null);
            if (sp == null || !banDuoc(sp)) {
                throw new LoiNghiepVu("Phiên bản bạn chọn không hợp lệ hoặc đã ngừng bán.");
            }

            // --- Màu ---
            List<ProductVariant> mauCoThe = bienTheDangBan(sp);
            ProductVariant mau = null;
            if (!mauCoThe.isEmpty()) {
                Integer mauId = chonMau == null ? null : chonMau.get(viTriId);
                mau = (mauId == null)
                        ? chonMauMacDinh(sp, null)
                        : mauCoThe.stream().filter(v -> v.getId().equals(mauId))
                            .findFirst().orElse(null);
                if (mau == null) {
                    throw new LoiNghiepVu(
                            "Vui lòng chọn màu cho \"" + sp.getName() + "\".");
                }
            }

            // Kiểm tồn kho ngay lúc bấm mua, vì từ lúc khách mở trang tới giờ có thể
            // đã có người mua mất.
            if (tonKho(sp, mau) < soLuong) {
                throw new LoiNghiepVu(
                        "\"" + sp.getName() + "\"" + (mau != null ? " (" + mau.getColor() + ")" : "")
                                + " đã hết hàng, mời bạn chọn phiên bản hoặc màu khác.");
            }

            CartItem dong = new CartItem();
            dong.setUser(user);
            dong.setProduct(sp);
            dong.setVariant(mau);
            dong.setCombo(view.getCombo());
            dong.setComboItem(m.getViTri());
            dong.setQuantity(soLuong);
            dong.setAddedAt(LocalDateTime.now());
            sePhaiLuu.add(dong);
        }

        // Lưu sau khi tất cả các món đã qua kiểm, để không có cảnh thêm được 2 món
        // rồi món thứ 3 lỗi và để lại nửa bộ trong giỏ.
        cartItemRepository.saveAll(sePhaiLuu);
        log.info("Khách #{} thêm combo #{} '{}' vào giỏ ({} món)",
                userId, comboId, view.getCombo().getName(), sePhaiLuu.size());
    }

    /* ==========================================================
       GIÁ TỪNG DÒNG GIỎ HÀNG
       ========================================================== */

    // Giá đã chốt cho một dòng giỏ hàng.
    public static class GiaDong {
        private final BigDecimal donGia;
        private final BigDecimal thanhTien;
        public GiaDong(BigDecimal donGia, BigDecimal thanhTien) {
            this.donGia = donGia; this.thanhTien = thanhTien;
        }
        public BigDecimal getDonGia()    { return donGia; }
        public BigDecimal getThanhTien() { return thanhTien; }
    }

    // Tính giá thật của từng dòng giỏ hàng. Trang giỏ, trang đặt hàng và lúc ghi đơn
    // đều gọi vào đây để lấy cùng một con số.
    //   dòng mua lẻ -> giá hiệu lực đã tính flash sale
    //   dòng combo  -> chia tiền giảm của bộ vào từng dòng theo tỉ lệ giá
    // Chia vào từng dòng chứ không dồn một cục, vì line_total là căn cứ tính lãi và
    // hoàn tiền cho từng sản phẩm.
    public Map<Integer, GiaDong> tinhGiaTungDong(List<CartItem> gio) {
        Map<Integer, GiaDong> ketQua = new HashMap<>();
        if (gio == null || gio.isEmpty()) return ketQua;

        List<Product> sanPham = gio.stream().map(CartItem::getProduct).collect(Collectors.toList());
        Map<Integer, FlashSaleService.EffectivePrice> priceMap =
                flashSaleService.getEffectivePrices(sanPham);

        for (CartItem d : gio) {
            BigDecimal donGia = giaHieuLuc(d.getProduct(), priceMap);
            ketQua.put(d.getId(), new GiaDong(donGia,
                    donGia.multiply(BigDecimal.valueOf(d.getQuantity()))));
        }

        Map<Integer, List<CartItem>> theoCombo = gio.stream()
                .filter(d -> d.getCombo() != null)
                .collect(Collectors.groupingBy(d -> d.getCombo().getId()));
        for (Map.Entry<Integer, List<CartItem>> e : theoCombo.entrySet()) {
            phanBoGiamGia(e.getKey(), e.getValue(), ketQua);
        }
        return ketQua;
    }

    // Cộng tổng tiền giỏ sau khi đã trừ giảm giá combo.
    public BigDecimal tongTienGio(List<CartItem> gio) {
        Map<Integer, GiaDong> gia = tinhGiaTungDong(gio);
        return gio.stream()
                .map(d -> gia.get(d.getId()).getThanhTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Chia phần giảm của một bộ vào các dòng của bộ đó. Giá bộ tính từ chính các
    // dòng trong giỏ vì khách có thể đã đổi sang bản đắt hơn; combo chỉ cung cấp
    // số tiền giảm. Dòng cuối nhận phần lẻ để tổng khớp tuyệt đối với giá bộ.
    // Nếu các dòng không còn khớp với combo thì bỏ giảm giá, bán theo giá lẻ.
    private void phanBoGiamGia(Integer comboId, List<CartItem> cacDong,
                               Map<Integer, GiaDong> ketQua) {
        Combo combo = comboRepository.findDangBanTheoId(comboId).orElse(null);
        if (combo == null || !hopLeTheoViTri(combo, cacDong)) {
            log.warn("Giỏ hàng chứa combo #{} nhưng không còn hợp lệ -> tính giá lẻ", comboId);
            return;
        }

        BigDecimal giaNiemYet = cacDong.stream()
                .map(d -> d.getProduct().getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tong = cacDong.stream()
                .map(d -> ketQua.get(d.getId()).getThanhTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mucTieu = chotGiaBo(combo, giaNiemYet, tong);
        if (mucTieu == null || tong.compareTo(BigDecimal.ZERO) <= 0
                || mucTieu.compareTo(tong) >= 0) return;

        BigDecimal daChia = BigDecimal.ZERO;
        for (int i = 0; i < cacDong.size(); i++) {
            CartItem d = cacDong.get(i);
            BigDecimal thanhTienGoc = ketQua.get(d.getId()).getThanhTien();
            BigDecimal thanhTienMoi;
            if (i == cacDong.size() - 1) {
                // Dòng cuối nhận phần lẻ để tổng khớp TUYỆT ĐỐI với giá combo
                // (chia theo tỉ lệ rồi làm tròn từng dòng sẽ lệch vài đồng).
                thanhTienMoi = mucTieu.subtract(daChia);
            } else {
                thanhTienMoi = thanhTienGoc.multiply(mucTieu)
                        .divide(tong, 0, RoundingMode.HALF_UP);
                daChia = daChia.add(thanhTienMoi);
            }
            BigDecimal donGiaMoi = thanhTienMoi.divide(
                    BigDecimal.valueOf(d.getQuantity()), 2, RoundingMode.HALF_UP);
            ketQua.put(d.getId(), new GiaDong(donGiaMoi, thanhTienMoi));
        }
    }

    // Kiểm các dòng trong giỏ có đúng là một bộ hợp lệ của combo này không: đủ mỗi
    // vị trí một dòng, đúng số lượng, và sản phẩm nằm trong nhóm được phép của vị
    // trí đó (chính nó hoặc cùng dòng sản phẩm).
    private boolean hopLeTheoViTri(Combo combo, List<CartItem> cacDong) {
        Set<ComboItem> viTri = combo.getItems();
        if (viTri == null || viTri.size() != cacDong.size()) return false;

        Set<Integer> daDung = new HashSet<>();
        for (CartItem d : cacDong) {
            if (d.getComboItem() == null) return false;
            Integer viTriId = d.getComboItem().getId();
            if (!daDung.add(viTriId)) return false;          // một vị trí xuất hiện 2 lần

            ComboItem goc = viTri.stream()
                    .filter(v -> v.getId().equals(viTriId)).findFirst().orElse(null);
            if (goc == null) return false;                    // vị trí không thuộc combo này
            if (!d.getQuantity().equals(goc.getQuantity() == null ? 1 : goc.getQuantity())) return false;
            if (!hopLeChoViTri(goc, d.getProduct())) return false;
        }
        return daDung.size() == viTri.size();
    }

    /* ==========================================================
       QUẢN TRỊ
       ========================================================== */

    // Lấy danh sách combo cho trang quản trị, gồm cả combo đã tắt.
    public List<Combo> layTatCaChoAdmin() {
        return comboRepository.findTatCaKemMon();
    }

    public Combo layDeSua(Integer id) {
        return comboRepository.findKemMonTheoId(id).orElse(null);
    }

    // Cộng tổng giá niêm yết của các sản phẩm được chọn, dùng để kiểm mức giảm.
    public BigDecimal tongGiaNiemYet(List<Integer> sanPhamIds, List<Integer> soLuongs) {
        BigDecimal tong = BigDecimal.ZERO;
        for (int i = 0; i < sanPhamIds.size(); i++) {
            Product p = productRepository.findById(sanPhamIds.get(i)).orElse(null);
            if (p == null || p.getPrice() == null) continue;
            int sl = (soLuongs != null && i < soLuongs.size() && soLuongs.get(i) != null)
                    ? soLuongs.get(i) : 1;
            tong = tong.add(p.getPrice().multiply(BigDecimal.valueOf(Math.max(1, sl))));
        }
        return tong;
    }

    // Lưu combo (thêm mới hoặc sửa) kèm danh sách món. Xóa sạch món cũ rồi ghi lại
    // thay vì so từng dòng. Chạy trong transaction nên lỗi giữa chừng là hoàn tác hết,
    // không có cảnh xóa xong rồi mất luôn danh sách món.
    @Transactional
    public Combo luuCombo(Combo combo, List<Integer> sanPhamIds,
                          List<Integer> bienTheIds, List<Integer> soLuongs) {
        if (sanPhamIds == null || sanPhamIds.isEmpty()) {
            throw new LoiNghiepVu("Combo phải có ít nhất một sản phẩm.");
        }
        // Trùng sản phẩm trong cùng combo -> vi phạm UNIQUE(combo_id, product_id),
        // chặn ở đây để báo lỗi tiếng Việt thay vì để lỗi SQL thô bắn lên màn hình.
        if (new HashSet<>(sanPhamIds).size() != sanPhamIds.size()) {
            throw new LoiNghiepVu("Một sản phẩm chỉ được xuất hiện một lần trong combo.");
        }

        // Chặn mức giảm âm hoặc lớn hơn tổng giá bộ, ngay tại form admin. Hàm
        // chotGiaBo() kiểm lại lần nữa lúc hiển thị.
        BigDecimal tong = tongGiaNiemYet(sanPhamIds, soLuongs);
        BigDecimal giam = combo.getDiscountAmount() == null ? BigDecimal.ZERO : combo.getDiscountAmount();
        if (giam.compareTo(BigDecimal.ZERO) < 0) {
            throw new LoiNghiepVu("Số tiền giảm không được âm.");
        }
        if (giam.compareTo(tong) >= 0) {
            throw new LoiNghiepVu(
                    "Số tiền giảm (" + giam.toBigInteger() + "đ) phải NHỎ HƠN tổng giá bộ ("
                            + tong.toBigInteger() + "đ), nếu không combo sẽ bán giá 0đ hoặc âm.");
        }

        if (combo.getCreatedAt() == null) combo.setCreatedAt(LocalDateTime.now());
        if (combo.getIsActive() == null)  combo.setIsActive(true);
        Combo daLuu = comboRepository.save(combo);

        comboItemRepository.deleteByComboId(daLuu.getId());
        comboItemRepository.flush();   // xóa phải xuống DB TRƯỚC khi chèn lại, không thì đụng UNIQUE

        for (int i = 0; i < sanPhamIds.size(); i++) {
            Product p = productRepository.findById(sanPhamIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));

            ComboItem it = new ComboItem();
            it.setCombo(daLuu);
            it.setProduct(p);
            it.setQuantity((soLuongs != null && i < soLuongs.size() && soLuongs.get(i) != null)
                    ? Math.max(1, soLuongs.get(i)) : 1);

            // Sản phẩm có biến thể thì bắt buộc chốt một màu làm mặc định. Thiếu màu
            // là combo bị ẩn khỏi trang khách.
            Integer btId = (bienTheIds != null && i < bienTheIds.size()) ? bienTheIds.get(i) : null;
            List<ProductVariant> mauCoThe = bienTheDangBan(p);
            if (!mauCoThe.isEmpty()) {
                ProductVariant mau = (btId == null) ? null
                        : mauCoThe.stream().filter(v -> v.getId().equals(btId)).findFirst().orElse(null);
                if (mau == null) {
                    throw new LoiNghiepVu(
                            "Sản phẩm \"" + p.getName() + "\" có nhiều màu, phải chọn một màu mặc định.");
                }
                it.setVariant(mau);
            }
            comboItemRepository.save(it);
        }
        log.info("Đã lưu combo #{} '{}' với {} món", daLuu.getId(), daLuu.getName(), sanPhamIds.size());
        return daLuu;
    }

    // Bật/tắt combo. KHÔNG xóa cứng vì order_items trỏ tới combos.
    @Transactional
    public void doiTrangThai(Integer id) {
        comboRepository.findById(id).ifPresent(c -> {
            c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
            comboRepository.save(c);
        });
    }

    // Sản phẩm có được phép đứng ở vị trí này không: chính nó, hoặc cùng dòng sản phẩm.
    private boolean hopLeChoViTri(ComboItem viTri, Product chon) {
        if (chon == null || viTri.getProduct() == null) return false;
        if (chon.getId().equals(viTri.getProduct().getId())) return true;
        return viTri.getProduct().getLine() != null
                && chon.getLine() != null
                && viTri.getProduct().getLine().getId().equals(chon.getLine().getId());
    }
}
