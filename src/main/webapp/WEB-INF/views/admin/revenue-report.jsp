<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Báo Cáo Doanh Thu – TiTanGEAR</title>
  <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
  <link rel="stylesheet" href="/static/css/admin.css"/>
  <%-- Hai biểu đồ trong trang vẽ bằng CSS và SVG thuần, không nạp thư viện
       biểu đồ nào từ mạng ngoài — nhẹ hơn, chạy được cả khi máy không có
       mạng, và cùng kiểu dáng với biểu đồ ở trang Tổng quan. --%>
  <style>
    /* ---- Biểu đồ cột 12 tháng ---- */
    .rr-bars   { display:flex; align-items:flex-end; gap:10px; height:220px; padding:0 2px; }
    .rr-bcol   { flex:1; display:flex; flex-direction:column; align-items:center; justify-content:flex-end;
                 height:100%; position:relative; cursor:pointer; transition:transform .18s ease; }
    .rr-bcol:hover { transform:translateY(-2px); }
    .rr-bar    { width:100%; border-radius:8px 8px 4px 4px;
                 background:linear-gradient(180deg,var(--accent),var(--accent-2));
                 transform-origin:bottom; animation:rr-moc .55s var(--ease) both;
                 transition:filter .2s ease; }
    @keyframes rr-moc { from { transform:scaleY(0); } to { transform:scaleY(1); } }
    .rr-bcol:hover .rr-bar { filter:brightness(1.15); }
    .rr-tip    { position:absolute; bottom:calc(100% + 6px); background:var(--ink); color:#fff;
                 font-family:var(--font-mono); font-size:10px; padding:4px 9px; border-radius:7px;
                 white-space:nowrap; z-index:5; box-shadow:0 6px 16px rgba(20,23,26,.22);
                 opacity:0; pointer-events:none; transition:opacity .16s ease; }
    .rr-bcol:hover .rr-tip { opacity:1; }
    .rr-blabels { display:flex; gap:10px; padding-top:10px; border-top:1px solid var(--line-soft); }
    .rr-blabels span { flex:1; text-align:center; font-family:var(--font-mono); font-size:10px; color:var(--ink-4); }

    /* ---- Donut danh mục ---- */
    .rr-donut-wrap { display:flex; align-items:center; gap:24px; flex-wrap:wrap; }
    .rr-donut-hole { position:absolute; inset:0; display:flex; flex-direction:column;
                     align-items:center; justify-content:center; }
    #rrDonut circle[data-veDan] { transition:stroke-dasharray .8s var(--ease); }
    .rr-legend { display:flex; flex-direction:column; gap:9px; flex:1; min-width:150px; }
    .rr-legend > div { display:flex; align-items:center; gap:9px; font-size:12px;
                       border-radius:7px; padding:3px 6px; margin:-3px -6px;
                       transition:background .18s ease; animation:tg-rise .45s var(--ease) both; }
    .rr-legend > div:hover { background:var(--surface-3); }
    .rr-cham { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
    .rr-trong { color:var(--ink-3); font-size:13px; padding:30px 0; text-align:center; }
  </style>
</head>
<body>

<div class="admin-layout">
  <c:set var="adminActive" value="report"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

  <div class="admin-main">
    <c:set var="adminTitle" value="Báo Cáo Doanh Thu"/>
    <c:set var="adminCrumb" value="ADMIN / REPORT"/>
    <c:set var="adminAction" value="Xuất Excel"/>
    <c:set var="adminActionUrl" value="/admin/report/export?month=${selectedMonth}&year=${selectedYear}"/>
    <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

    <!-- TỔNG QUAN -->
    <div class="stat-cards" style="grid-template-columns:repeat(3,1fr);">
      <div class="stat-card tg-rise">
        <div class="ad-bar" style="background:var(--accent);"></div>
        <div class="ad-top">
          <span class="stat-label">TỔNG DOANH THU</span>
          <span class="ad-ico" style="background:var(--accent-soft-2);">
            <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8v6.4M6.3 6.4h3M6.3 9.6h3"/></svg>
          </span>
        </div>
        <div class="stat-value"><fmt:formatNumber value="${summary.tong_doanh_thu}" pattern="#,##0"/>đ</div>
        <div class="stat-sub">từ các đơn đã hoàn thành</div>
      </div>

      <div class="stat-card tg-rise" style="animation-delay:80ms;">
        <div class="ad-bar" style="background:var(--ok-2);"></div>
        <div class="ad-top">
          <span class="stat-label">ĐƠN HOÀN THÀNH</span>
          <span class="ad-ico" style="background:var(--ok-bg-2);">
            <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M5.4 8.2 7.2 10l3.4-3.6"/></svg>
          </span>
        </div>
        <div class="stat-value">${summary.tong_don}</div>
        <div class="stat-sub">đã giao tới khách</div>
      </div>

      <div class="stat-card tg-rise" style="animation-delay:160ms;">
        <div class="ad-bar" style="background:var(--violet);"></div>
        <div class="ad-top">
          <span class="stat-label">GIÁ TRỊ ĐƠN TRUNG BÌNH</span>
          <span class="ad-ico" style="background:var(--violet-bg);">
            <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 13h12"/><path d="M4 10.5V7M7.3 10.5V4.5M10.6 10.5V8"/></svg>
          </span>
        </div>
        <%-- Tính ngay tại chỗ vì chỉ là phép chia của 2 số đã có sẵn, không đáng
             thêm truy vấn. Chặn chia cho 0 khi chưa có đơn nào. --%>
        <div class="stat-value">
          <c:choose>
            <c:when test="${summary.tong_don > 0}">
              <fmt:formatNumber value="${summary.tong_doanh_thu / summary.tong_don}" pattern="#,##0"/>đ
            </c:when>
            <c:otherwise>0đ</c:otherwise>
          </c:choose>
        </div>
        <div class="stat-sub">bình quân mỗi đơn</div>
      </div>
    </div>

    <!-- BIỂU ĐỒ SO SÁNH DOANH THU 12 THÁNG -->
    <div class="report-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;flex-wrap:wrap;gap:12px;">
        <h2 style="margin:0;">So Sánh Doanh Thu 12 Tháng — Năm ${chartYear}</h2>
        <form method="get" action="/admin/report" style="display:flex;align-items:center;gap:8px;">
          <input type="hidden" name="month" value="${selectedMonth}"/>
          <input type="hidden" name="year" value="${selectedYear}"/>
          <label style="font-size:13px;color:var(--ink-2);">Chọn năm:</label>
          <select name="chartYear" class="filter-select" onchange="this.form.submit()">
            <c:forEach var="y" items="${years}">
              <option value="${y}" ${chartYear == y ? 'selected' : ''}>${y}</option>
            </c:forEach>
          </select>
        </form>
      </div>
      <div class="rr-bars"    id="rrBars"></div>
      <div class="rr-blabels" id="rrLabels"></div>
      <div class="rr-trong"   id="rrTrong" style="display:none;">Chưa có doanh thu nào trong năm này</div>
    </div>

    <!-- LỢI NHUẬN THEO THÁNG -->
    <div class="report-card">
      <h2 style="margin-bottom:6px;">Lợi Nhuận Theo Tháng</h2>
      <p style="font-size:13px;color:var(--ink-3);margin-bottom:20px;line-height:1.6;">
        Lợi nhuận = Doanh thu − Giá vốn thực tế (lấy đúng giá nhập của từng lô hàng đã xuất qua FIFO,
        không phải giá nhập trung bình hay ước tính). Doanh thu cao chưa chắc đã lời nếu giá vốn cũng tăng.
      </p>
      <table class="admin-table">
        <thead>
        <tr>
          <th>Tháng</th>
          <th>Doanh thu</th>
          <th>Giá vốn</th>
          <th>Lợi nhuận</th>
          <th>Kết quả</th>
          <th>So với tháng trước</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
          <c:when test="${empty profitByMonth}">
            <tr><td colspan="6" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có dữ liệu đơn hàng hoàn thành</td></tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="pm" items="${profitByMonth}">
              <tr>
                <td style="font-weight:600;">Tháng ${pm.thang}/${pm.nam}</td>
                <td><fmt:formatNumber value="${pm.doanh_thu}" pattern="#,##0"/>đ</td>
                <td style="color:var(--ink-3);"><fmt:formatNumber value="${pm.gia_von}" pattern="#,##0"/>đ</td>
                <td style="font-weight:700;"><fmt:formatNumber value="${pm.loi_nhuan}" pattern="#,##0"/>đ</td>
                <td>
                  <c:choose>
                    <c:when test="${pm.la_loi}"><span class="badge-green">Lời</span></c:when>
                    <c:otherwise><span class="badge-red">Lỗ</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${pm.thay_doi == null}">
                      <span style="color:var(--ink-3);">— (tháng đầu tiên có dữ liệu)</span>
                    </c:when>
                    <c:when test="${pm.tang_so_thang_truoc}">
                      <span style="color:var(--ok-deep);">▲ Tăng <fmt:formatNumber value="${pm.thay_doi}" pattern="#,##0"/>đ</span>
                    </c:when>
                    <c:otherwise>
                      <span style="color:var(--err);">▼ Giảm <fmt:formatNumber value="${pm.thay_doi}" pattern="#,##0"/>đ</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
        </tbody>
      </table>
    </div>

    <!-- LÃI THEO TỪNG PHIÊN BẢN -->
    <div class="report-card">
      <h2>Lãi Theo Từng Phiên Bản</h2>
      <p style="font-size:13px;color:var(--ink-2);margin:0 0 14px;">
        Gộp chung các màu của cùng một phiên bản. Giá vốn lấy từ <strong>giá nhập thật</strong>
        của đúng lô hàng đã xuất (FIFO), không phải ước tính.
      </p>
      <table class="admin-table">
        <thead>
        <tr>
          <th>Sản phẩm</th>
          <th>Phiên bản</th>
          <th style="text-align:right;">Đã bán</th>
          <th style="text-align:right;">Doanh thu</th>
          <th style="text-align:right;">Giá vốn</th>
          <th style="text-align:right;">Lợi nhuận</th>
          <th style="text-align:right;">Tỷ suất</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
          <c:when test="${empty profitByVariant}">
            <tr><td colspan="7" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có đơn hàng hoàn thành nào</td></tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="pv" items="${profitByVariant}">
              <tr>
                <td style="font-weight:600;">${pv.san_pham}</td>
                <td>${pv.phien_ban}</td>
                <td style="text-align:right;">${pv.so_luong_ban}</td>
                <td style="text-align:right;"><fmt:formatNumber value="${pv.doanh_thu}" pattern="#,##0"/>đ</td>
                <td style="text-align:right;color:var(--ink-3);"><fmt:formatNumber value="${pv.gia_von}" pattern="#,##0"/>đ</td>
                <td style="text-align:right;font-weight:700;">
                  <c:choose>
                    <%-- Không có sổ giá vốn thì lãi hiển thị sẽ bằng đúng doanh thu,
                         nói thẳng ra thay vì để người xem tưởng lãi 100% --%>
                    <c:when test="${pv.thieu_gia_von}">
                      <span style="color:var(--ink-3);font-weight:500;">Chưa có giá vốn</span>
                    </c:when>
                    <c:when test="${pv.la_loi}">
                      <span style="color:var(--ok-deep);"><fmt:formatNumber value="${pv.loi_nhuan}" pattern="#,##0"/>đ</span>
                    </c:when>
                    <c:otherwise>
                      <span style="color:var(--err);"><fmt:formatNumber value="${pv.loi_nhuan}" pattern="#,##0"/>đ</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="text-align:right;">
                  <c:choose>
                    <c:when test="${pv.thieu_gia_von}"><span style="color:var(--ink-3);">—</span></c:when>
                    <c:otherwise>${pv.ty_suat}%</c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
        </tbody>
      </table>
    </div>

    <!-- DOANH THU THEO DANH MỤC -->
    <div class="report-card">
      <h2>Doanh Thu Theo Danh Mục</h2>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:24px;align-items:center;">
        <div class="rr-donut-wrap">
          <div style="position:relative;width:150px;height:150px;flex-shrink:0;">
            <svg width="150" height="150" viewBox="0 0 150 150" style="transform:rotate(-90deg);" id="rrDonut">
              <circle cx="75" cy="75" r="58" fill="none" stroke="var(--line-soft)" stroke-width="20"></circle>
            </svg>
            <div class="rr-donut-hole">
              <span class="tg-mono" style="font-size:9px;color:var(--ink-4);letter-spacing:.1em;">DANH MỤC</span>
              <span style="font-size:22px;font-weight:700;letter-spacing:-.02em;" id="rrSoDanhMuc">0</span>
            </div>
          </div>
          <div class="rr-legend" id="rrLegend"></div>
        </div>
        <table class="admin-table">
          <thead>
          <tr>
            <th>Danh mục</th>
            <th>Số lượng bán</th>
            <th>Doanh thu</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty revenueByCategory}">
              <tr><td colspan="3" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có dữ liệu</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="dm" items="${revenueByCategory}">
                <tr>
                  <td style="font-weight:500;">${dm.danh_muc}</td>
                  <td style="font-weight:600;">${dm.so_luong_ban}</td>
                  <td style="font-weight:600;color:var(--accent);"><fmt:formatNumber value="${dm.doanh_thu}" pattern="#,##0"/>đ</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>
    </div>

    <!-- CHI TIẾT SẢN PHẨM ĐÃ BÁN -->
    <div class="report-card">
      <h2 style="margin-bottom:16px;">Chi Tiết Sản Phẩm Đã Bán</h2>

      <form method="get" action="/admin/report" class="filter-bar">
        <select name="month" class="filter-select">
          <option value="">-- Tất cả tháng --</option>
          <c:forEach begin="1" end="12" var="m">
            <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
          </c:forEach>
        </select>
        <select name="year" class="filter-select">
          <option value="">-- Tất cả năm --</option>
          <c:forEach var="y" items="${years}">
            <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
          </c:forEach>
        </select>
        <button type="submit" class="btn-filter">Lọc</button>
        <c:if test="${selectedMonth != null || selectedYear != null}">
          <a href="/admin/report" class="btn-reset">Xóa lọc</a>
          <span class="filter-count">
            <c:if test="${selectedMonth != null}">Tháng ${selectedMonth}</c:if>
            <c:if test="${selectedMonth != null && selectedYear != null}"> / </c:if>
            <c:if test="${selectedYear != null}">Năm ${selectedYear}</c:if>
            — ${allProductsSales.size()} sản phẩm
          </span>
        </c:if>
      </form>

      <table class="admin-table">
        <thead>
        <tr>
          <th>STT</th>
          <th>Tên sản phẩm</th>
          <th>Danh mục</th>
          <th>Số lượng bán</th>
          <th>Doanh thu</th>
          <th>% Đóng góp doanh thu</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
          <c:when test="${empty allProductsSales}">
            <tr><td colspan="6" style="text-align:center;color:var(--ink-3);padding:24px;">Không có sản phẩm nào được bán trong khoảng thời gian này</td></tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="sp" items="${allProductsSales}" varStatus="st">
              <tr>
                <td style="color:var(--ink-3);">${st.index + 1}</td>
                <td style="font-weight:500;">${sp.ten_sp}</td>
                <td>${sp.danh_muc}</td>
                <td style="font-weight:600;">${sp.so_luong_ban}</td>
                <td style="font-weight:600;color:var(--accent);"><fmt:formatNumber value="${sp.doanh_thu}" pattern="#,##0"/>đ</td>
                <td>
                  <div style="display:flex;align-items:center;gap:8px;">
                    <div style="flex:1;height:8px;background:var(--line-soft);overflow:hidden;max-width:100px;">
                      <div style="height:100%;background:var(--accent);width:${sp.phan_tram_doanh_thu}%;"></div>
                    </div>
                    <span style="font-family:var(--font-mono);font-size:12px;font-weight:600;white-space:nowrap;">
                      <fmt:formatNumber value="${sp.phan_tram_doanh_thu}" pattern="0.0"/>%
                    </span>
                  </div>
                </td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
        </tbody>
      </table>
    </div>

    <!-- TOP SẢN PHẨM BÁN CHẠY -->
    <div class="report-card" style="margin-bottom:0;">
      <h2>Top Sản Phẩm Bán Chạy</h2>
      <table class="admin-table">
        <thead>
        <tr>
          <th>Hạng</th>
          <th>Tên sản phẩm</th>
          <th>Số lượng bán</th>
          <th>Doanh thu</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${topProducts}" varStatus="st">
          <tr>
            <td style="font-family:var(--font-mono);font-weight:700;color:var(--accent);">#${st.index + 1}</td>
            <td>${p.ten_sp}</td>
            <td style="font-weight:600;">${p.so_luong_ban}</td>
            <td style="font-weight:600;color:var(--accent);"><fmt:formatNumber value="${p.doanh_thu}" pattern="#,##0"/>đ</td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script>
  var dinhDang = new Intl.NumberFormat('vi-VN');

  /* ===== BIỂU ĐỒ CỘT 12 THÁNG ===== */
  (function () {
    var nhan = [
      <c:forEach var="r" items="${revenueByMonthForYear}" varStatus="st">
      "Th${r.thang}"<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];
    var giaTri = [
      <c:forEach var="r" items="${revenueByMonthForYear}" varStatus="st">
      ${r.doanh_thu}<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];
    var oBars = document.getElementById('rrBars');
    var oLabel = document.getElementById('rrLabels');
    if (!oBars) return;

    var max = giaTri.length ? Math.max.apply(null, giaTri) : 0;
    if (!giaTri.length || max <= 0) {
      oBars.style.display = 'none';
      oLabel.style.display = 'none';
      document.getElementById('rrTrong').style.display = '';
      return;
    }

    giaTri.forEach(function (v, i) {
      var cot = document.createElement('div');
      cot.className = 'rr-bcol';

      var tip = document.createElement('span');
      tip.className = 'rr-tip';
      tip.textContent = nhan[i] + ' · ' + dinhDang.format(v) + 'đ';

      var bar = document.createElement('div');
      bar.className = 'rr-bar';
      /* Tháng không có doanh thu vẫn chừa 1% cho thấy CÓ cột — nhìn vào biết
         tháng đó bán được 0đ, chứ không phải dữ liệu bị thiếu. */
      bar.style.height = (v > 0 ? Math.max(4, (v / max) * 100) : 1) + '%';
      bar.style.animationDelay = (i * 55) + 'ms';

      cot.appendChild(tip);
      cot.appendChild(bar);
      oBars.appendChild(cot);

      var nh = document.createElement('span');
      nh.textContent = nhan[i];
      oLabel.appendChild(nh);
    });
  })();

  /* ===== DONUT DOANH THU THEO DANH MỤC ===== */
  (function () {
    var ten = [
      <c:forEach var="dm" items="${revenueByCategory}" varStatus="st">
      "<c:out value='${dm.danh_muc}'/>"<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];
    var so = [
      <c:forEach var="dm" items="${revenueByCategory}" varStatus="st">
      ${dm.doanh_thu}<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];
    var svg = document.getElementById('rrDonut');
    var oLegend = document.getElementById('rrLegend');
    if (!svg || !ten.length) return;

    /* Bảng màu lấy đúng token của hệ thiết kế, không bịa màu mới */
    var MAU = ['#1B49FF', '#10A268', '#7A5CC7', '#E08600', '#C62828', '#5A6068'];
    var tong = so.reduce(function (a, b) { return a + Number(b); }, 0);
    document.getElementById('rrSoDanhMuc').textContent = ten.length;
    if (!tong) return;

    var R = 58, CHU_VI = 2 * Math.PI * R, daVe = 0, canVe = [];

    ten.forEach(function (t, i) {
      var v = Number(so[i]) || 0;
      var mau = MAU[i % MAU.length];
      var pct = tong ? (v / tong * 100) : 0;

      if (v > 0) {
        var dai = (v / tong) * CHU_VI;
        var c = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        c.setAttribute('cx', 75); c.setAttribute('cy', 75); c.setAttribute('r', R);
        c.setAttribute('fill', 'none');
        c.setAttribute('stroke', mau);
        c.setAttribute('stroke-width', 20);
        c.setAttribute('data-veDan', '1');
        c.setAttribute('stroke-dasharray', '0 ' + CHU_VI);
        c.setAttribute('stroke-dashoffset', -daVe);
        svg.appendChild(c);
        canVe.push({ el: c, dai: dai });
        daVe += dai;
      }

      var d = document.createElement('div');
      d.style.animationDelay = (i * 90 + 100) + 'ms';
      d.innerHTML = '<span class="rr-cham" style="background:' + mau + '"></span>'
                  + '<span style="flex:1;color:var(--ink-2);">' + t + '</span>'
                  + '<span class="tg-mono" style="font-weight:600;">' + pct.toFixed(1) + '%</span>';
      d.title = dinhDang.format(v) + 'đ';
      oLegend.appendChild(d);
    });

    /* Ép tính lại layout để trình duyệt ghi nhận giá trị đầu (cung dài 0) rồi mới
       đặt giá trị cuối -> transition chạy. KHÔNG dùng requestAnimationFrame: tab
       đang ẩn thì rAF không chạy và biểu đồ sẽ trống trơn. */
    void svg.getBoundingClientRect();
    canVe.forEach(function (m, i) {
      m.el.style.transitionDelay = (i * 160) + 'ms';
      m.el.setAttribute('stroke-dasharray', m.dai + ' ' + (CHU_VI - m.dai));
    });
  })();
</script>

</body>
</html>
