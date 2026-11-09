package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.dao.StockReportDao;
import com.titangear.model.*;
import com.titangear.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    @Autowired private StockInRepository  stockInRepository;
    @Autowired private StockOutRepository stockOutRepository;
    @Autowired private StockReportDao     stockReportDao;

    // Ghi một lô hàng mới vào kho. Mỗi dòng stock_in là một lô riêng, số còn lại
    // ban đầu bằng số nhập và sẽ trừ dần khi xuất FIFO.
    @Transactional
    public void stockIn(StockIn stockIn) {
        stockIn.setImportedAt(LocalDateTime.now());
        stockIn.setRemainingQuantity(stockIn.getQuantity());
        stockInRepository.save(stockIn);

        // Cộng tồn kho do trigger TRG_StockIn_SyncProductStock làm. Không cộng tay
        // ở đây, sẽ thành cộng hai lần.
    }

    // Một dòng trong phiếu xuất kho: lấy bao nhiêu, từ lô nào.
    public static class PhanLo {
        private final StockIn lo;
        private final int soLuong;
        public PhanLo(StockIn lo, int soLuong) { this.lo = lo; this.soLuong = soLuong; }
        public StockIn  getLo()      { return lo; }
        public int      getSoLuong() { return soLuong; }
        public Integer  getMaLo()    { return lo.getId(); }
        public String   getViTri()   { return lo.getViTri(); }
        public boolean  isCoViTri()  { return lo.isCoViTri(); }
        public java.time.LocalDateTime getNgayNhap() { return lo.getImportedAt(); }
        public java.math.BigDecimal    getGiaVon()   { return lo.getImportPrice(); }
    }

    // Chọn các lô theo FIFO cho một dòng hàng: duyệt lô từ cũ nhất, lấy dần cho đủ
    // số lượng cần. Cả phiếu xem trước lẫn lúc xuất kho thật đều gọi vào hàm này,
    // để phiếu ghi lô nào thì hệ thống trừ đúng lô đó.
    // kiemDuHang = true : thiếu hàng thì ném lỗi ngay (lúc xuất thật).
    // kiemDuHang = false: còn bao nhiêu chia bấy nhiêu (lúc xem trước).
    public List<PhanLo> chonLoFIFO(Product product, ProductVariant variant,
                                    int soLuongCan, boolean kiemDuHang) {
        List<StockIn> lots = (variant != null)
                ? stockInRepository.findVariantLotsFIFO(variant.getId())
                : stockInRepository.findProductLotsFIFO(product.getId());

        if (kiemDuHang) {
            int tongCon = lots.stream().mapToInt(StockIn::getRemainingQuantity).sum();
            if (tongCon < soLuongCan) {
                String what = (variant != null)
                        ? "bien the #" + variant.getId() + " (san pham #" + product.getId() + ")"
                        : "san pham #" + product.getId();
                throw new LoiNghiepVu("Khong du ton kho de xuat: " + what
                        + " can " + soLuongCan + " nhung cac lo chi con " + tongCon);
            }
        }

        List<PhanLo> ketQua = new java.util.ArrayList<>();
        int conCan = soLuongCan;
        for (StockIn lot : lots) {
            if (conCan <= 0) break;
            int lay = Math.min(conCan, lot.getRemainingQuantity());
            if (lay <= 0) continue;
            ketQua.add(new PhanLo(lot, lay));
            conCan -= lay;
        }
        return ketQua;
    }

    // Xuất kho theo FIFO cho một dòng đơn hàng: lấy từ lô cũ nhất trước, mỗi phần
    // ghi một dòng stock_out kèm giá vốn thực tế của lô đó.
    @Transactional
    public void stockOutFIFO(OrderItem orderItem, Integer quantityNeeded, User exportedBy) {

        ProductVariant variant = orderItem.getVariant();

        // Kiểm đủ hàng trước khi ghi bất kỳ dòng nào, để thiếu hàng thì hoàn tác sạch
        // chứ không để lại dữ liệu xuất kho nửa vời.
        List<PhanLo> phanLo = chonLoFIFO(orderItem.getProduct(), variant, quantityNeeded, true);

        for (PhanLo phan : phanLo) {
            StockIn lot = phan.getLo();
            int takeFromLot = phan.getSoLuong();

            // Ghi một dòng xuất kho, trỏ về đúng order_item và đúng lô đã lấy hàng.
            StockOut out = new StockOut();
            out.setOrderItem(orderItem);
            out.setStockIn(lot);
            out.setQuantity(takeFromLot);
            // Chốt giá vốn của lô ngay lúc xuất, để báo cáo tháng cũ không đổi theo
            // giá nhập mới sau này.
            out.setImportCost(lot.getImportPrice());
            out.setExportedBy(exportedBy);
            out.setExportedAt(LocalDateTime.now());
            out.setNote("FIFO " + takeFromLot + " san pham tu lo nhap #" + lot.getId()
                    + " gia " + lot.getImportPrice() + "d");
            stockOutRepository.save(out);

            // Cập nhật số còn lại trong bộ nhớ để vòng lặp đọc đúng con số. Việc trừ
            // thật trong DB do trigger làm, không trừ thêm ở đây.
            lot.setRemainingQuantity(lot.getRemainingQuantity() - takeFromLot);
        }

    }

    // Xem trước FIFO sẽ lấy hàng từ những lô nào. Chỉ tính, không ghi gì vào DB.
    // Màn hình duyệt đơn gọi hàm này để chỉ cho thủ kho ra khu nào, kệ nào, lấys
    // thùng có số lô nào.
    public List<PhanLo> xemTruocPhieuXuat(OrderItem orderItem) {
        return chonLoFIFO(orderItem.getProduct(), orderItem.getVariant(),
                orderItem.getQuantity() == null ? 0 : orderItem.getQuantity(), false);
    }

    // Tìm một lô theo mã, dùng để hiện tem dán thùng.
    public java.util.Optional<StockIn> timLo(Integer loId) {
        return stockInRepository.findById(loId);
    }

    // Lấy các dòng xuất kho đã ghi của một đơn, dùng để truy ngược đã lấy từ lô nào.
    public List<StockOut> daXuatTheoDon(Integer orderId) {
        return stockOutRepository.findByOrderItem_Order_Id(orderId);
    }

    // Đổi vị trí cất hàng của một lô. Dùng khi dọn kho, sắp xếp lại kệ, hoặc gõ nhầm
    // lúc nhập. Số lô không đổi nên tem đã dán trên thùng vẫn đúng.
    @Transactional
    public void doiViTri(Integer loId, String khu, Integer ke) {
        StockIn lo = stockInRepository.findById(loId)
                .orElseThrow(() -> new LoiNghiepVu("Không tìm thấy lô #" + loId));
        if (!com.titangear.constant.KhoViTri.hopLe(khu, ke)) {
            throw new LoiNghiepVu(com.titangear.constant.KhoViTri.loiViTri());
        }
        lo.setKhu(khu.trim().toUpperCase());
        lo.setKe(ke);
        stockInRepository.save(lo);
    }

    // Lấy lịch sử các lô nhập của một sản phẩm, cho màn hình kho.
    public List<StockIn> getLotHistory(Integer productId) {
        return stockInRepository.findByProductId(productId);
    }

    // Lấy các lô nhập ở mức sản phẩm (chưa gắn biến thể) mà vẫn còn hàng. Đây là
    // hàng nhập trước khi tạo biến thể nên đang bị kẹt, cần gắn vào một biến thể
    // mới bán được.
    public List<StockIn> getStrandedLots(Integer productId) {
        return stockInRepository.findProductLotsFIFO(productId);
    }

    // Gắn một lô hàng vào một biến thể. Trigger DB tự tính lại tồn kho sau đó.
    @Transactional
    public int assignLotToVariant(Integer lotId, Integer productId, Integer variantId) {
        return stockInRepository.assignLotToVariant(lotId, productId, variantId);
    }

    // Hoàn hàng về kho khi hủy đơn đã xuất: cộng trả đúng lô đã lấy, đánh dấu dòng
    // stock_out là đã hoàn chứ không xóa đi để giữ lịch sử. Đơn chưa xuất kho thì
    // không có dòng nào nên hàm này không làm gì.
    @Transactional
    public int returnStockForOrder(Integer orderId) {
        List<StockOut> outs = stockOutRepository.findByOrderItem_Order_IdAndReturnedFalse(orderId);
        for (StockOut out : outs) {
            // Trả về đúng lô đã lấy, trigger tự tính lại tồn kho.
            stockInRepository.restoreLot(out.getStockIn().getId(), out.getQuantity());
            out.setReturned(true);
            out.setReturnedAt(LocalDateTime.now());
            stockOutRepository.save(out);
        }
        return outs.size();
    }

    public List<Map<String, Object>> getStockReport(int year, int month) {
        return stockReportDao.getStockReport(year, month);
    }

    public List<Map<String, Object>> getStockInHistory(Integer productId) {
        return stockReportDao.getStockInHistory(productId);
    }

    public List<Map<String, Object>> getStockOutHistory(Integer productId) {
        return stockReportDao.getStockOutHistory(productId);
    }

    public List<StockIn> getAllStockIns() {
        return stockInRepository.findAll();
    }

    // Lịch sử nhập lô, lọc theo từ khóa là tên sản phẩm hoặc số lô. Truyền null lấy hết.
    public List<Map<String, Object>> getAllStockInHistory(String tim) {
        return stockReportDao.getAllStockInHistory(tim);
    }

    public List<Map<String, Object>> getAllStockOutHistory() {
        return stockReportDao.getAllStockOutHistory();
    }

    public List<Map<String, Object>> getLowStockProducts() {
        return stockReportDao.getLowStockProducts();
    }
}
