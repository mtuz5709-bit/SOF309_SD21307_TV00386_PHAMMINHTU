package com.titangear.controller;

import com.titangear.constant.ProductAttr;
import com.titangear.model.Product;
import com.titangear.service.CategoryService;
import com.titangear.service.FlashSaleService;
import com.titangear.service.ProductService;
import com.titangear.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private FlashSaleService flashSaleService;
    @Autowired private ReviewService reviewService;

    // Danh sách sản phẩm + BỘ LỌC.
    // Quy tắc lọc chuẩn thương mại điện tử: trong CÙNG 1 nhóm là "hoặc" (chọn
    // Linear + Tactile thì ra cả hai), giữa CÁC nhóm là "và" (Linear + Không dây
    // thì phải thoả cả hai).
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> stock,
            @RequestParam(required = false) List<String> switchType,
            @RequestParam(required = false) List<String> weight,
            @RequestParam(required = false) List<String> conn,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            Model model) {
        return danhSach(false, keyword, category, minPrice, maxPrice, stock, switchType, weight, conn, sort, page, model);
    }

    // Trang Flash Sale — CÙNG bộ lọc với /products, chỉ khác ở chỗ danh sách gốc
    // là "các sản phẩm đang giảm giá" thay vì toàn bộ hàng. Gọi lại đúng một hàm
    // nên sau này sửa bộ lọc chỉ phải sửa một chỗ.
    @GetMapping("/flash-sale")
    public String flashSale(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> stock,
            @RequestParam(required = false) List<String> switchType,
            @RequestParam(required = false) List<String> weight,
            @RequestParam(required = false) List<String> conn,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            Model model) {
        model.addAttribute("mocKetThuc", flashSaleService.getMocKetThucGanNhat());
        return danhSach(true, keyword, category, minPrice, maxPrice, stock, switchType, weight, conn, sort, page, model);
    }

    private String danhSach(
            boolean chiHangGiamGia,
            String keyword, Integer category, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> stock, List<String> switchType, List<String> weight, List<String> conn,
            String sort, Integer page,
            Model model) {

        // Danh sách gốc để ĐẾM (số cạnh mỗi lựa chọn tính trên toàn bộ sản phẩm
        // của TRANG NÀY, không phụ thuộc bộ lọc đang chọn — giống mẫu thiết kế).
        List<Product> tatCa = chiHangGiamGia
                ? flashSaleService.getSanPhamDangGiam()
                : productService.getAllProducts();

        // Tìm theo tên NGAY TRÊN danh sách gốc thay vì gọi productService.search():
        // search() trả cả hàng đã ngừng bán (isActive = false) và không biết giới hạn
        // trong nhóm hàng đang giảm giá của trang Flash Sale.
        List<Product> products = tatCa;
        if (keyword != null && !keyword.isEmpty()) {
            final String tuKhoa = keyword.toLowerCase();
            products = tatCa.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(tuKhoa))
                    .toList();
        }

        if (category != null) {
            final Integer cat = category;
            products = products.stream()
                    .filter(p -> p.getCategory() != null && cat.equals(p.getCategory().getId()))
                    .toList();
        }
        if (minPrice != null) {
            final BigDecimal min = minPrice;
            products = products.stream().filter(p -> p.getPrice().compareTo(min) >= 0).toList();
        }
        if (maxPrice != null) {
            final BigDecimal max = maxPrice;
            products = products.stream().filter(p -> p.getPrice().compareTo(max) <= 0).toList();
        }
        if (coChon(switchType)) {
            products = products.stream().filter(p -> switchType.contains(p.getSwitchType())).toList();
        }
        if (coChon(weight)) {
            products = products.stream().filter(p -> weight.contains(p.getWeightGroup())).toList();
        }
        if (coChon(conn)) {
            products = products.stream().filter(p -> conn.contains(p.getConnectivity())).toList();
        }
        if (coChon(stock)) {
            // Tick cả 2 ô = xem tất cả, nên chỉ lọc khi chọn đúng 1 bên
            boolean con = stock.contains("instock");
            boolean het = stock.contains("outofstock");
            if (con ^ het) {
                products = products.stream()
                        .filter(p -> con ? conHang(p) : !conHang(p))
                        .toList();
            }
        }

        // ---- Sắp xếp + phân trang ----
        // PHẢI làm TRƯỚC mọi model.addAttribute("products", ...): model giữ tham
        // chiếu tới danh sách tại thời điểm gán, gán xong rồi mới sắp xếp thì JSP
        // vẫn nhận danh sách cũ (đúng lỗi "bấm Giá tăng/giảm không đổi gì").
        products = sapXep(products, sort);

        int tong      = products.size();
        int tongTrang = Math.max(1, (int) Math.ceil(tong / (double) MOI_TRANG));
        int trang     = (page == null || page < 1) ? 1 : Math.min(page, tongTrang);
        int tu        = (trang - 1) * MOI_TRANG;
        products      = products.subList(tu, Math.min(tu + MOI_TRANG, tong));

        model.addAttribute("tongKetQua", tong);
        model.addAttribute("trang",      trang);
        model.addAttribute("tongTrang",  tongTrang);
        model.addAttribute("sort",       sort);

        // Điểm đánh giá CHỈ của các sản phẩm đang hiện trên trang — 1 truy vấn
        model.addAttribute("danhGiaMap", reviewService.diemTheoDanhSach(
                products.stream().map(Product::getId).toList()));

        // ---- Số đếm cho từng lựa chọn ----
        model.addAttribute("demTatCa", tatCa.size());
        Map<Integer, Long> demDanhMuc = new HashMap<>();
        for (Product p : tatCa) {
            if (p.getCategory() != null) {
                demDanhMuc.merge(p.getCategory().getId(), 1L, Long::sum);
            }
        }
        model.addAttribute("demDanhMuc",   demDanhMuc);
        model.addAttribute("demSwitch",    demTheo(tatCa, Product::getSwitchType));
        model.addAttribute("demTrongLuong",demTheo(tatCa, Product::getWeightGroup));
        model.addAttribute("demKetNoi",    demTheo(tatCa, Product::getConnectivity));
        model.addAttribute("demConHang",   tatCa.stream().filter(this::conHang).count());
        model.addAttribute("demHetHang",   tatCa.stream().filter(p -> !conHang(p)).count());

        // ---- Nhãn hiển thị của các nhóm thuộc tính ----
        model.addAttribute("nhomSwitch",  ProductAttr.SWITCH);
        model.addAttribute("nhomTrongLuong", ProductAttr.WEIGHT);
        model.addAttribute("nhomKetNoi",  ProductAttr.CONNECT);

        // ---- Giữ lại lựa chọn đang chọn để tick lại ô ----
        model.addAttribute("products",         products);
        model.addAttribute("categories",       categoryService.getAll());
        model.addAttribute("keyword",          keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice",         minPrice);
        model.addAttribute("maxPrice",         maxPrice);
        model.addAttribute("chonStock",      stock      == null ? List.of() : stock);
        model.addAttribute("chonSwitch",     switchType == null ? List.of() : switchType);
        model.addAttribute("chonTrongLuong", weight     == null ? List.of() : weight);
        model.addAttribute("chonKetNoi",     conn       == null ? List.of() : conn);
        model.addAttribute("priceMap",         flashSaleService.getEffectivePrices(products));

        // Dùng CHUNG view products.jsp cho cả 2 trang, chỉ đổi đường dẫn của form lọc
        // và các link "xoá lọc". Nếu chép ra file JSP thứ hai thì mỗi lần sửa bộ lọc
        // phải sửa 2 nơi, sớm muộn cũng lệch nhau.
        model.addAttribute("duongDan", chiHangGiamGia ? "/flash-sale" : "/products");
        model.addAttribute("laFlashSale", chiHangGiamGia);
        return "customer/products";
    }

    // Số sản phẩm mỗi trang — 9 để lấp đầy đúng 3 hàng của lưới 3 cột.
    private static final int MOI_TRANG = 9;

    // Sắp xếp danh sách đang hiển thị.
    // "noi-bat" (mặc định) giữ nguyên thứ tự gốc của kho — không đảo lung tung.
    private List<Product> sapXep(List<Product> ds, String sort) {
        if (sort == null) return ds;
        return switch (sort) {
            case "gia-tang"  -> ds.stream().sorted(java.util.Comparator.comparing(Product::getPrice)).toList();
            case "gia-giam"  -> ds.stream().sorted(java.util.Comparator.comparing(Product::getPrice).reversed()).toList();
            default          -> ds;
        };
    }

    private boolean coChon(List<String> ds) {
        return ds != null && !ds.isEmpty();
    }

    private boolean conHang(Product p) {
        return p.getStockQuantity() != null && p.getStockQuantity() > 0;
    }

    // Đếm số sản phẩm theo từng mã thuộc tính (bỏ qua sản phẩm để trống).
    private Map<String, Long> demTheo(List<Product> ds, java.util.function.Function<Product, String> lay) {
        Map<String, Long> dem = new HashMap<>();
        for (Product p : ds) {
            String v = lay.apply(p);
            if (v != null && !v.isBlank()) dem.merge(v, 1L, Long::sum);
        }
        return dem;
    }
}
