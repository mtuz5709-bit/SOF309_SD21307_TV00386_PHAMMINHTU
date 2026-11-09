<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>

<!-- FOOTER -->
<footer style="margin-top:60px;background:var(--surface);border-top:1px solid var(--line-new);padding:52px 60px 30px;">
    <div class="tg-wrap">
        <div class="tg-foot-grid" style="display:grid;grid-template-columns:1.5fr 1fr 1fr 1fr;gap:40px;padding-bottom:36px;border-bottom:1px solid var(--line-soft);">
            <div>
                <img src="${pageContext.request.contextPath}/static/images/logo-lockup.svg" alt="TiTanGEAR"
                     style="height:46px;display:block;margin-bottom:16px;"/>
                <p style="font-size:13px;color:var(--ink-3);line-height:1.8;margin:0 0 16px;max-width:300px;">
                    Nhà phân phối chính hãng thiết bị ngoại vi hiệu năng cao tại Việt Nam.
                    Bảo hành chính hãng 12 tháng.
                </p>
                <div style="display:flex;gap:8px;flex-wrap:wrap;">
                    <span class="tg-chip" style="padding:6px 13px;">ATK CHÍNH HÃNG</span>
                    <span class="tg-chip" style="padding:6px 13px;">SHIP TOÀN QUỐC</span>
                    <span class="tg-chip" style="padding:6px 13px;">TRẢ GÓP 0%</span>
                </div>
            </div>

            <div>
                <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">SẢN PHẨM</div>
                <div style="display:flex;flex-direction:column;gap:11px;">
                    <a href="/products?category=1" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Chuột Gaming</a>
                    <a href="/products?category=2" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Bàn Phím Cơ</a>
                    <a href="/products?category=3" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Tai Nghe</a>
                    <a href="/flash-sale"          style="font-size:13px;color:var(--ink-2);text-decoration:none;">Flash Sale</a>
                </div>
            </div>

            <div>
                <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">HỖ TRỢ</div>
                <div style="display:flex;flex-direction:column;gap:11px;">
                    <a href="/bao-hanh"  style="font-size:13px;color:var(--ink-2);text-decoration:none;">Tra cứu bảo hành</a>
                    <a href="/my-orders" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Đơn hàng của tôi</a>
                    <a href="/chinh-sach-bao-hanh" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Chính sách bảo hành</a>
                    <a href="/dieu-khoan" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Điều khoản sử dụng</a>
                    <a href="/chinh-sach-bao-mat" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Chính sách bảo mật</a>
                    <a href="tel:0900000011" style="font-size:13px;color:var(--ink-2);text-decoration:none;">Liên hệ: 0900 000 011</a>
                </div>
            </div>

            <div>
                <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">NHẬN TIN</div>
                <p style="font-size:13px;color:var(--ink-3);line-height:1.7;margin:0 0 14px;">
                    Tin hàng mới và deal sớm 24h. Không spam.
                </p>
                <div style="display:flex;gap:8px;">
                    <input type="email" placeholder="email@cua.ban"
                           style="flex:1;min-width:0;border:1px solid var(--line-new);border-radius:var(--r-md);padding:11px 14px;font-size:13px;font-family:inherit;outline:none;"/>
                    <button type="button" class="tg-btn tg-primary" style="padding:11px 18px;font-size:13px;">→</button>
                </div>
            </div>
        </div>

        <div style="display:flex;justify-content:space-between;align-items:center;padding-top:22px;font-size:12px;color:var(--ink-4);flex-wrap:wrap;gap:10px;">
            <span>© 2026 TiTanGEAR. Gear Up. Game On.</span>
            <span class="tg-mono" style="text-align:right;">
                ${diaChiShop}<br/>0900 000 011 · support@titangear.vn
            </span>
        </div>
    </div>
</footer>

<%-- Chặn gửi trùng cho các form phía khách (đặt hàng, đăng ký, đổi mật khẩu…):
     bấm "Đặt hàng" hai lần là tạo 2 đơn giống hệt. --%>
<script src="/static/js/form-guard.js"></script>

<%-- Token CSRF cho post-link.js đọc (xóa món khỏi giỏ, hủy đơn). --%>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_param" content="${_csrf.parameterName}">
<script src="/static/js/post-link.js"></script>

</body>
</html>
