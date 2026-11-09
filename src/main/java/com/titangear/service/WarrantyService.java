package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.OrderItem;
import com.titangear.model.Product;
import com.titangear.model.Warranty;
import com.titangear.model.WarrantyRepair;
import com.titangear.repository.OrderItemRepository;
import com.titangear.repository.OrderRepository;
import com.titangear.repository.WarrantyRepairRepository;
import com.titangear.repository.WarrantyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WarrantyService {

    private static final Logger log = LoggerFactory.getLogger(WarrantyService.class);

    @Autowired private WarrantyRepository warrantyRepository;
    @Autowired private WarrantyRepairRepository repairRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;

    // Tạo phiếu bảo hành cho một đơn, gọi khi đơn chuyển sang Hoàn thành. Mỗi chiếc
    // một phiếu nên mua 3 con thì sinh 3 serial khác nhau. Sản phẩm không khai số
    // tháng bảo hành thì bỏ qua. Gọi lại lần hai không sinh trùng. Trả về số phiếu đã tạo.
    @Transactional
    public int taoChoDonHang(Integer orderId) {
        var donOpt = orderRepository.findById(orderId);
        if (donOpt.isEmpty()) return 0;
        Integer userId = donOpt.get().getUser().getId();

        int daTao = 0;
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            // Đã tạo cho dòng này rồi -> bỏ qua (tránh nhân đôi phiếu)
            if (warrantyRepository.existsByOrderItemId(item.getId())) continue;

            Product sp = item.getProduct();
            if (sp == null || sp.getWarrantyMonths() == null || sp.getWarrantyMonths() <= 0) {
                continue; // sản phẩm không có bảo hành
            }

            LocalDate batDau = LocalDate.now();
            LocalDate ketThuc = batDau.plusMonths(sp.getWarrantyMonths());
            int soLuong = item.getQuantity() != null ? item.getQuantity() : 1;

            for (int i = 0; i < soLuong; i++) {
                Warranty w = new Warranty();
                w.setOrderItemId(item.getId());
                w.setProductId(sp.getId());
                w.setUserId(userId);
                w.setStartDate(batDau);
                w.setEndDate(ketThuc);
                w.setCreatedAt(LocalDateTime.now());
                // Lưu lần 1 để lấy id tự tăng, rồi mới đặt serial theo id
                // -> serial LUÔN duy nhất, không cần khoá, không lo trùng
                w.setSerialNo("TAM-" + System.nanoTime() + "-" + i);
                w = warrantyRepository.save(w);
                w.setSerialNo(String.format("SR%06d-%s", w.getId(), hauToNgauNhien()));
                warrantyRepository.save(w);
                daTao++;
            }
        }
        if (daTao > 0) {
            log.info("Đã tạo {} phiếu bảo hành cho đơn #{}", daTao, orderId);
        }
        return daTao;
    }

    // Tra cứu phiếu theo mã bảo hành khách nhập.
    // Bảng ký tự dùng sinh hậu tố mã bảo hành. Bảng này KHÔNG chứa các ký tự dễ đọc
    // nhầm khi khách nhìn phiếu in rồi gõ lại: số 0 với chữ O, số 1 với chữ I và L.
    private static final String BANG_KY_TU = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final java.security.SecureRandom NGAU_NHIEN = new java.security.SecureRandom();

    // Sinh hậu tố ngẫu nhiên gắn sau số thứ tự, cho ra mã dạng SR000123-K7QP.
    // Phần id bảo đảm không trùng, phần ngẫu nhiên khiến không dò được mã kế tiếp —
    // trang tra cứu bảo hành mở công khai nên mã đoán được là lộ số liệu kinh doanh.
    // Dùng SecureRandom chứ không dùng Random, vì Random sinh theo công thức đoán được.
    private String hauToNgauNhien() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(BANG_KY_TU.charAt(NGAU_NHIEN.nextInt(BANG_KY_TU.length())));
        }
        return sb.toString();
    }

    public Optional<Warranty> traCuu(String serial) {
        if (serial == null || serial.trim().isEmpty()) return Optional.empty();
        return warrantyRepository.findBySerialNoIgnoreCase(serial.trim());
    }

    public List<Warranty> cuaKhach(Integer userId) {
        return warrantyRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<Warranty> tatCa() {
        return warrantyRepository.findAllByOrderByIdDesc();
    }

    public List<Warranty> tim(String q) {
        return warrantyRepository.timTheoSerial(q);
    }

    // ---- Lọc theo tình trạng hạn (tính sống, không có cột trạng thái trong CSDL) ----

    public static final String CON_HAN = "CON_HAN";
    public static final String SAP_HET = "SAP_HET";
    public static final String HET_HAN = "HET_HAN";

    // Lọc phiếu theo trạng thái hạn bảo hành. Lọc ở Java trên danh sách đã lấy sẵn
    // thay vì viết truy vấn ngày tháng trong SQL, để con số luôn khớp với nhãn đang
    // hiện trên màn hình.
    public List<Warranty> loc(List<Warranty> ds, String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return ds;
        return switch (trangThai) {
            case CON_HAN -> ds.stream().filter(Warranty::isConHan).toList();
            case SAP_HET -> ds.stream().filter(Warranty::isSapHetHan).toList();
            case HET_HAN -> ds.stream().filter(w -> !w.isConHan()).toList();
            default      -> ds;
        };
    }

    public Optional<Warranty> findById(Integer id) {
        return warrantyRepository.findById(id);
    }

    // Kiểm dòng đơn này đã phát phiếu bảo hành chưa, để trang đơn của khách chỉ hiện
    // nút tra cứu khi thật sự có phiếu.
    public boolean coPhieuChoDongDon(Integer orderItemId) {
        return warrantyRepository.existsByOrderItemId(orderItemId);
    }

    // Đếm số phiếu sắp hết hạn trong 30 ngày tới, hiện làm số cảnh báo trên menu quản trị.
    public long demSapHetHan() {
        LocalDate homNay = LocalDate.now();
        return warrantyRepository.countByEndDateBetween(homNay, homNay.plusDays(Warranty.NGUONG_SAP_HET));
    }

    // Lấy các phiếu phát ra từ một dòng đơn, để trang chi tiết đơn liệt kê serial.
    public List<Warranty> theoDongDon(Integer orderItemId) {
        return warrantyRepository.findByOrderItemId(orderItemId);
    }

    // ---- Lịch sử sửa chữa ----

    public List<WarrantyRepair> lichSuSua(Integer warrantyId) {
        return repairRepository.findByWarrantyIdOrderByIdDesc(warrantyId);
    }

    // Ghi nhận 1 lần sửa chữa.
    //
    // HẾT HẠN THÌ TỰ CHẶN: kiểm tra ở đây (không chỉ ẩn nút ngoài giao diện) vì
    // ẩn nút thì gõ thẳng URL vẫn POST được. Admin khỏi phải tự dò hạn từng phiếu.
    //
    // @throws IllegalStateException khi phiếu không tồn tại hoặc đã hết hạn
    @Transactional
    public void themLanSua(Integer warrantyId, String noiDung, String trangThai) {
        Warranty w = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy phiếu bảo hành #" + warrantyId));
        if (!w.isConHan()) {
            throw new LoiNghiepVu("Phiếu " + w.getSerialNo()
                    + " đã hết hạn bảo hành từ ngày " + w.getEndDate() + " — không nhận sửa chữa bảo hành.");
        }

        WarrantyRepair r = new WarrantyRepair();
        r.setWarrantyId(warrantyId);
        r.setNoiDung(noiDung);
        r.setTrangThai(trangThai != null ? trangThai : WarrantyRepair.TIEP_NHAN);
        r.setCreatedAt(LocalDateTime.now());
        repairRepository.save(r);
    }
}
