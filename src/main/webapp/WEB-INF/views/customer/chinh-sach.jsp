<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${tieuDe} – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<section class="tg-sect">
    <div class="tg-wrap cs-wrap">

        <div class="tg-mono" style="font-size:11px;letter-spacing:.1em;color:var(--ink-4);margin-bottom:10px;">
            <a href="/home" style="color:var(--ink-4);text-decoration:none;">TRANG CHỦ</a> / ${tieuDe}
        </div>
        <h1 style="font-size:32px;font-weight:700;margin:0 0 8px;letter-spacing:-.02em;">${tieuDe}</h1>
        <p style="font-size:13px;color:var(--ink-3);margin:0 0 26px;">Áp dụng từ ${ngayApDung}</p>

        <div class="cs-nav">
            <a href="/dieu-khoan"            class="${trang == 'dieu-khoan' ? 'is-on' : ''}">Điều khoản sử dụng</a>
            <a href="/chinh-sach-bao-mat"    class="${trang == 'bao-mat'    ? 'is-on' : ''}">Chính sách bảo mật</a>
            <a href="/chinh-sach-bao-hanh"   class="${trang == 'bao-hanh'   ? 'is-on' : ''}">Chính sách bảo hành</a>
        </div>

        <div class="tg-card cs-body">
        <c:choose>

        <%-- ================= ĐIỀU KHOẢN SỬ DỤNG ================= --%>
        <c:when test="${trang == 'dieu-khoan'}">
            <h2>1. Về chúng tôi</h2>
            <p>TiTanGEAR là cửa hàng bán lẻ thiết bị ngoại vi máy tính (chuột, bàn phím, tai nghe).
               Địa chỉ: <strong>${diaChiShop}</strong>. Liên hệ: <strong>${sdt}</strong> · ${email}</p>

            <h2>2. Tài khoản</h2>
            <ul>
                <li>Bạn cần đăng ký tài khoản bằng email có thật để đặt hàng. Email được dùng để xác thực và gửi thông tin đơn hàng.</li>
                <li>Bạn chịu trách nhiệm giữ bí mật mật khẩu của mình. Mọi đơn hàng phát sinh từ tài khoản được xem là do bạn thực hiện.</li>
                <li>Chúng tôi có quyền khóa tài khoản có dấu hiệu gian lận, đặt hàng ảo, hoặc phá hoại hệ thống.</li>
            </ul>

            <h2>3. Đặt hàng và thanh toán</h2>
            <ul>
                <li>Giá hiển thị là giá cuối cùng đã bao gồm thuế, chưa gồm phí vận chuyển.</li>
                <li>Phí vận chuyển được tính khi bạn chọn tỉnh/thành giao hàng ở bước đặt hàng.</li>
                <li>Hai hình thức thanh toán: <strong>thu tiền khi nhận hàng (COD)</strong> và <strong>chuyển khoản trước</strong>.</li>
                <li>Đơn giá trị lớn hoặc số lượng lớn bắt buộc chuyển khoản trước — nhằm hạn chế đặt hàng ảo.</li>
                <li>Mỗi khách được có tối đa một số lượng đơn COD đang chờ giao cùng lúc.</li>
                <li>Đơn chuyển khoản không thanh toán trong <strong>15 phút</strong> sẽ tự động hủy để trả hàng về kho.</li>
            </ul>

            <h2>4. Giao hàng</h2>
            <ul>
                <li>Chúng tôi giao hàng toàn quốc. Thời gian giao phụ thuộc đơn vị vận chuyển và khu vực.</li>
                <li>Vui lòng ghi địa chỉ đầy đủ: số nhà, đường, phường/xã, quận/huyện. Địa chỉ thiếu thông tin có thể làm chậm hoặc thất bại việc giao hàng.</li>
                <li>Đơn hàng chỉ được xuất kho sau khi nhân viên duyệt. Trước thời điểm đó bạn có thể tự hủy đơn.</li>
            </ul>

            <h2>5. Hủy đơn</h2>
            <ul>
                <li>Bạn tự hủy được đơn đang ở trạng thái <strong>Chờ xác nhận</strong> trong mục "Đơn hàng của tôi".</li>
                <li>Đơn đã chuyển sang <strong>Đang giao</strong> vui lòng liên hệ ${sdt} để được hỗ trợ.</li>
                <li>Đơn đã <strong>Hoàn thành</strong> không hủy được.</li>
            </ul>

            <h2>6. Đánh giá sản phẩm</h2>
            <p>Chỉ khách đã mua và nhận hàng thành công mới đánh giá được sản phẩm đó.
               Chúng tôi có quyền gỡ đánh giá có nội dung xúc phạm, quảng cáo hoặc sai sự thật.</p>

            <h2>7. Thay đổi điều khoản</h2>
            <p>Điều khoản có thể được cập nhật. Bản mới có hiệu lực kể từ ngày đăng trên trang này.</p>
        </c:when>

        <%-- ================= CHÍNH SÁCH BẢO MẬT ================= --%>
        <c:when test="${trang == 'bao-mat'}">
            <p>Chính sách này mô tả cách TiTanGEAR thu thập, sử dụng và bảo vệ dữ liệu cá nhân của bạn,
               theo <strong>Nghị định 13/2023/NĐ-CP</strong> về bảo vệ dữ liệu cá nhân.</p>

            <h2>1. Dữ liệu chúng tôi thu thập</h2>
            <ul>
                <li><strong>Khi đăng ký:</strong> họ tên, email, số điện thoại, mật khẩu.</li>
                <li><strong>Khi đặt hàng:</strong> tên người nhận, số điện thoại, địa chỉ giao hàng, tỉnh/thành, ghi chú đơn.</li>
                <li><strong>Khi mua hàng:</strong> lịch sử đơn hàng, sản phẩm đã mua, đánh giá bạn viết.</li>
                <li>Chúng tôi <strong>không thu thập</strong> thông tin thẻ ngân hàng — mọi giao dịch chuyển khoản diễn ra trên ứng dụng ngân hàng của bạn.</li>
            </ul>

            <h2>2. Mục đích sử dụng</h2>
            <ul>
                <li>Xử lý và giao đơn hàng của bạn.</li>
                <li>Xác thực tài khoản và khôi phục mật khẩu qua mã OTP gửi email.</li>
                <li>Phát hành và tra cứu phiếu bảo hành.</li>
                <li>Liên hệ khi có vấn đề với đơn hàng.</li>
            </ul>
            <p>Chúng tôi <strong>không bán, không trao đổi</strong> dữ liệu của bạn cho bên thứ ba vì mục đích thương mại.</p>

            <h2>3. Chia sẻ dữ liệu</h2>
            <p>Dữ liệu chỉ được chia sẻ với đơn vị vận chuyển (tên, số điện thoại, địa chỉ) để giao hàng,
               và với cơ quan nhà nước khi có yêu cầu hợp pháp.</p>

            <h2>4. Bảo vệ dữ liệu</h2>
            <ul>
                <li>Mật khẩu được <strong>mã hóa một chiều bằng thuật toán BCrypt</strong> — kể cả quản trị viên cũng không xem được mật khẩu của bạn.</li>
                <li>Mã OTP có hiệu lực 5 phút và bị hủy sau 5 lần nhập sai.</li>
                <li>Mỗi tài khoản chỉ xem được đơn hàng của chính mình.</li>
            </ul>

            <h2>5. Thời gian lưu trữ</h2>
            <p>Dữ liệu đơn hàng và bảo hành được lưu trong suốt thời gian bảo hành sản phẩm và tối thiểu theo
               quy định về lưu trữ chứng từ. Tài khoản không hoạt động vẫn được giữ cho tới khi bạn yêu cầu xóa.</p>

            <h2>6. Quyền của bạn</h2>
            <ul>
                <li>Xem và sửa thông tin cá nhân trong mục <strong>Hồ sơ</strong>.</li>
                <li>Yêu cầu xóa tài khoản bằng cách liên hệ ${sdt} hoặc ${email}.</li>
                <li>Yêu cầu biết dữ liệu nào của bạn đang được lưu.</li>
            </ul>
            <p>Lưu ý: dữ liệu gắn với đơn hàng đã phát sinh (hóa đơn, phiếu bảo hành) phải được giữ theo
               quy định kế toán, không xóa được ngay cả khi bạn đóng tài khoản.</p>

            <h2>7. Liên hệ</h2>
            <p>Mọi thắc mắc về dữ liệu cá nhân: <strong>${email}</strong> hoặc <strong>${sdt}</strong>.</p>
        </c:when>

        <%-- ================= CHÍNH SÁCH BẢO HÀNH ================= --%>
        <c:otherwise>
            <h2>1. Phạm vi bảo hành</h2>
            <ul>
                <li>Thời gian bảo hành ghi rõ trên từng sản phẩm (thường <strong>12 tháng</strong>), tính từ <strong>ngày giao hàng thành công</strong>.</li>
                <li>Mỗi chiếc được cấp <strong>một mã bảo hành riêng</strong> dạng <span class="tg-mono">SR000123-K7QP</span>.
                    Mua 3 chiếc sẽ có 3 mã khác nhau.</li>
                <li>Tra cứu tại <a href="/bao-hanh" style="color:var(--accent);">Tra cứu bảo hành</a> hoặc xem trong chi tiết đơn hàng của bạn.</li>
            </ul>

            <h2>2. Được bảo hành</h2>
            <ul>
                <li>Lỗi kỹ thuật do nhà sản xuất: nút bấm không ăn, cảm biến lỗi, kết nối chập chờn, pin chai bất thường.</li>
                <li>Sản phẩm còn trong thời hạn bảo hành và còn nguyên mã bảo hành.</li>
            </ul>

            <h2>3. KHÔNG được bảo hành</h2>
            <ul>
                <li>Hư hỏng do rơi vỡ, va đập, vào nước, cháy nổ, thiên tai.</li>
                <li>Tự ý tháo mở, sửa chữa tại nơi khác.</li>
                <li>Hao mòn tự nhiên: mòn chân trượt, bong lớp phủ, xước vỏ.</li>
                <li>Hết thời hạn bảo hành, hoặc không xác định được mã bảo hành.</li>
            </ul>

            <h2>4. Quy trình</h2>
            <ul>
                <li>Liên hệ <strong>${sdt}</strong> hoặc mang máy tới <strong>${diaChiShop}</strong>.</li>
                <li>Cung cấp mã bảo hành để chúng tôi tra cứu tình trạng.</li>
                <li>Toàn bộ quá trình tiếp nhận và sửa chữa được ghi lại, bạn xem được lịch sử khi tra cứu mã.</li>
            </ul>

            <h2>5. Lưu ý</h2>
            <p>Mã bảo hành hết hạn sẽ <strong>tự động khóa</strong> trên hệ thống và không tiếp nhận yêu cầu sửa chữa mới.
               Vui lòng kiểm tra hạn bảo hành trước khi mang máy tới.</p>
        </c:otherwise>

        </c:choose>
        </div>

        <div style="margin-top:22px;font-size:13px;color:var(--ink-3);line-height:1.8;">
            Cần hỗ trợ? <strong>${sdt}</strong> · ${email}<br/>
            ${diaChiShop}
        </div>
    </div>
</section>
</div>

<style>
    .cs-wrap { max-width: 880px; }
    .cs-nav  { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 22px; }
    .cs-nav a {
        padding: 9px 17px; border-radius: var(--r-pill); border: 1px solid var(--line-new);
        background: var(--surface); color: var(--ink-2); font-size: 13px; text-decoration: none;
        transition: border-color .2s ease, color .2s ease, background .2s ease;
    }
    .cs-nav a:hover { border-color: var(--accent); color: var(--accent); }
    .cs-nav a.is-on { background: var(--accent); border-color: var(--accent); color: #fff; font-weight: 600; }
    .cs-body { padding: 34px 38px; }
    .cs-body h2 { font-size: 17px; font-weight: 700; margin: 26px 0 10px; letter-spacing: -.01em; }
    .cs-body h2:first-child { margin-top: 0; }
    .cs-body p, .cs-body li { font-size: 14px; line-height: 1.85; color: var(--ink-2); }
    .cs-body ul { margin: 8px 0 10px 20px; }
    .cs-body li { margin-bottom: 7px; }
    .cs-body strong { color: var(--ink); font-weight: 600; }
    @media (max-width: 700px) { .cs-body { padding: 24px 20px; } }
</style>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
