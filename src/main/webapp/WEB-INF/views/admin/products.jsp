<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quản lý Sản phẩm – TiTanGEAR</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="products"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Sản phẩm"/>
        <c:set var="adminCrumb" value="ADMIN / PRODUCTS"/>
        <c:set var="adminSearchUrl" value="/admin/products"/>
        <c:set var="adminSearchPlaceholder" value="Tìm theo tên sản phẩm…"/>
        <c:set var="adminAction" value="+ Thêm sản phẩm"/>
        <c:set var="adminActionUrl" value="/admin/products/add"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <!-- ===== 4 THẺ CHỈ SỐ ===== -->
        <div class="stat-cards">
            <div class="stat-card tg-rise">
                <div class="ad-bar" style="background:var(--accent);"></div>
                <div class="ad-top">
                    <span class="stat-label">TỔNG SẢN PHẨM</span>
                    <span class="ad-ico" style="background:var(--accent-soft-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 1.8 14 5v6L8 14.2 2 11V5l6-3.2Z"/><path d="M2 5l6 3.2L14 5M8 8.2v6"/></svg>
                    </span>
                </div>
                <div class="stat-value">${tongSanPham}</div>
                <div class="stat-sub">đang bán trong kho</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:80ms;">
                <div class="ad-bar" style="background:var(--ok-2);"></div>
                <div class="ad-top">
                    <span class="stat-label">CÒN HÀNG</span>
                    <span class="ad-ico" style="background:var(--ok-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M5.4 8.2 7.2 10l3.4-3.6"/></svg>
                    </span>
                </div>
                <div class="stat-value">${soConHang}</div>
                <div class="stat-sub">
                    <c:if test="${soSapHet > 0}"><span style="color:var(--warn-deep);font-weight:600;">${soSapHet} sắp hết</span></c:if>
                    <c:if test="${soSapHet == 0}">không có mã nào sắp hết</c:if>
                </div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:160ms;">
                <div class="ad-bar" style="background:var(--err);"></div>
                <div class="ad-top">
                    <span class="stat-label">HẾT HÀNG</span>
                    <span class="ad-ico" style="background:var(--err-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#C62828" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2 1.5 13.5h13L8 2Z"/><path d="M8 6.5v3.2M8 11.6v.4"/></svg>
                    </span>
                </div>
                <div class="stat-value">${soHetHang}</div>
                <div class="stat-sub">cần nhập thêm</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:240ms;">
                <div class="ad-bar" style="background:var(--violet);"></div>
                <div class="ad-top">
                    <span class="stat-label">GIÁ TRỊ TỒN KHO</span>
                    <span class="ad-ico" style="background:var(--violet-bg);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8v6.4M6.3 6.4h3M6.3 9.6h3"/></svg>
                    </span>
                </div>
                <div class="stat-value"><fmt:formatNumber value="${giaTriKho / 1000000}" pattern="#,##0.0"/>M</div>
                <div class="stat-sub">giá bán × số lượng còn</div>
            </div>
        </div>

        <!-- ===== TAB LỌC THEO DANH MỤC ===== -->
        <div class="filter-bar" style="margin-bottom:14px;">
            <a href="/admin/products" class="filter-tab ${empty param.category ? 'active' : ''}">
                Tất cả <span class="tab-count">${tongSanPham}</span>
            </a>
            <c:forEach var="cat" items="${categories}">
                <a href="/admin/products?category=${cat.id}" class="filter-tab ${param.category eq cat.id ? 'active' : ''}">
                    ${cat.name} <span class="tab-count">${empty demDanhMuc[cat.id] ? 0 : demDanhMuc[cat.id]}</span>
                </a>
            </c:forEach>
        </div>

        <!-- FILTER THÁNG / NĂM -->
        <form method="get" action="/admin/products" class="filter-bar">
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
                <a href="/admin/products" class="btn-reset">Xóa lọc</a>
            </c:if>

            <%-- Đếm trên TỔNG kết quả sau khi lọc, không đếm số dòng đang hiện —
                 trang này chỉ hiện tối đa 10 dòng nên đếm dòng sẽ luôn ra 10. --%>
            <span class="filter-count">
                ${tongKetQua} sản phẩm
                <c:if test="${tongTrang > 1}"> · trang ${trang}/${tongTrang}</c:if>
            </span>
        </form>

        <table class="admin-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Tên sản phẩm</th>
                <th>Danh mục</th>
                <th>Giá</th>
                <th>Tồn kho</th>
                <th>Ngày thêm</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${products}">
                <tr>
                    <td>${p.id}</td>
                    <td>${p.name}</td>
                    <td>${p.category.name}</td>
                    <td><fmt:formatNumber value="${p.price}" pattern="#,##0"/> đ</td>
                    <td>
                        <span class="tg-mono" style="font-weight:600;">${p.stockQuantity}</span>
                        <%-- Thanh mini: quy ước 20 cái là "đầy kho" cho dễ nhìn tương đối.
                             Đỏ = hết, cam = ≤5 (sắp hết), xanh = ổn. --%>
                        <c:set var="pct" value="${p.stockQuantity >= 20 ? 100 : p.stockQuantity * 5}"/>
                        <span class="stock-mini">
                            <span style="width:${pct}%;background:${p.stockQuantity == 0 ? 'var(--err)' : (p.stockQuantity <= 5 ? 'var(--warn-2)' : 'var(--ok-2)')};"></span>
                        </span>
                    </td>
                    <td>
                        <c:if test="${p.createdAt != null}">
                            ${p.createdAt.dayOfMonth}/${p.createdAt.monthValue}/${p.createdAt.year}
                        </c:if>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${p.stockQuantity > 0}">
                                <span class="badge-green">Còn hàng</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-red">Hết hàng</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a href="/admin/products/edit/${p.id}" class="btn-edit">Sửa</a>
                        <a href="/admin/products/${p.id}/variants" class="btn-variants">Biến thể</a>
                        <a href="/admin/products/delete/${p.id}" class="btn-delete" data-post
                           data-confirm="Xóa sản phẩm này?">Xóa</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <%-- Thanh phân trang. Mỗi link dựng bằng <c:url> kèm lại đủ tham số tìm
             kiếm và lọc đang bật, chỉ đổi số trang — bấm sang trang 2 không mất bộ lọc. --%>
        <c:if test="${tongTrang > 1}">
            <div class="pagination">
                <c:if test="${trang > 1}">
                    <c:url var="linkTruoc" value="/admin/products">
                        <c:param name="page" value="${trang - 1}"/>
                        <c:if test="${not empty param.q}">       <c:param name="q"        value="${param.q}"/></c:if>
                        <c:if test="${not empty param.category}"><c:param name="category" value="${param.category}"/></c:if>
                        <c:if test="${not empty param.month}">   <c:param name="month"    value="${param.month}"/></c:if>
                        <c:if test="${not empty param.year}">    <c:param name="year"     value="${param.year}"/></c:if>
                    </c:url>
                    <a href="${linkTruoc}" class="page-btn">← Trước</a>
                </c:if>

                <c:forEach var="i" begin="1" end="${tongTrang}">
                    <c:url var="linkTrang" value="/admin/products">
                        <c:param name="page" value="${i}"/>
                        <c:if test="${not empty param.q}">       <c:param name="q"        value="${param.q}"/></c:if>
                        <c:if test="${not empty param.category}"><c:param name="category" value="${param.category}"/></c:if>
                        <c:if test="${not empty param.month}">   <c:param name="month"    value="${param.month}"/></c:if>
                        <c:if test="${not empty param.year}">    <c:param name="year"     value="${param.year}"/></c:if>
                    </c:url>
                    <a href="${linkTrang}" class="page-btn ${i == trang ? 'active' : ''}">${i}</a>
                </c:forEach>

                <c:if test="${trang < tongTrang}">
                    <c:url var="linkSau" value="/admin/products">
                        <c:param name="page" value="${trang + 1}"/>
                        <c:if test="${not empty param.q}">       <c:param name="q"        value="${param.q}"/></c:if>
                        <c:if test="${not empty param.category}"><c:param name="category" value="${param.category}"/></c:if>
                        <c:if test="${not empty param.month}">   <c:param name="month"    value="${param.month}"/></c:if>
                        <c:if test="${not empty param.year}">    <c:param name="year"     value="${param.year}"/></c:if>
                    </c:url>
                    <a href="${linkSau}" class="page-btn">Tiếp →</a>
                </c:if>
            </div>
        </c:if>
    </div>
</div>

</body>
</html>
