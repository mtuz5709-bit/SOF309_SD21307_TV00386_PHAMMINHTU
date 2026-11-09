<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Flash Sale – TiTanGEAR Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .fs-type-choice { display:flex; gap:16px; margin-bottom:20px; flex-wrap:wrap; }
        .fs-type-choice label { display:flex; align-items:center; gap:8px; padding:12px 18px; border:1px solid var(--line-new); border-radius:var(--r-md); cursor:pointer; font-size:14px; transition:border-color .2s ease, background .2s ease; }
        .fs-type-choice label:hover { border-color:var(--accent); }
        .fs-type-choice label:has(input:checked) { border-color:var(--accent); background:var(--accent-soft-2); }
        .fs-type-choice input[type=radio] { accent-color:var(--accent); }
        .fs-type-choice input[type=radio]:checked + span { font-weight:600; color:var(--accent); }
        .fs-section { border:1px solid var(--line-new); border-radius:var(--r-lg); padding:18px; margin-bottom:20px; background:var(--surface); }
        .fs-section h3 { font-size:14px; font-weight:700; margin-bottom:12px; color:var(--ink); }
        .product-row { display:flex; gap:10px; margin-bottom:8px; align-items:center; }
        .product-row select, .product-row input { padding:9px 11px; border:1px solid var(--line-new); border-radius:var(--r-md); font-size:13px; font-family:inherit; outline:none; transition:border-color .2s ease, box-shadow .2s ease; }
        .product-row select:focus, .product-row input:focus { border-color:var(--accent); box-shadow:0 0 0 3px rgba(27,73,255,.12); }
        .product-row select.pr-product { flex:2; }
        .product-row select.pr-type { flex:1; }
        .product-row input.pr-value { flex:1; }
        .btn-remove-spec { background:var(--surface); color:var(--err); border:1px solid var(--err-line-2); border-radius:var(--r-md); padding:7px 12px; cursor:pointer; font-size:16px; line-height:1; transition:background .2s ease, color .2s ease; }
        .btn-remove-spec:hover { background:var(--err); border-color:var(--err); color:#fff; }
        .btn-add-spec { background:var(--accent-soft-2); border:1px dashed var(--accent-line); border-radius:var(--r-md); padding:8px 16px; cursor:pointer; font-size:13px; font-weight:600; color:var(--accent); margin-top:4px; transition:background .2s ease, color .2s ease; }
        .btn-add-spec:hover { background:var(--accent); border-style:solid; border-color:var(--accent); color:#fff; }

        /* Form rộng ra và xếp 2 cột: bản cũ là một cột hẹp 640px giữa trang rộng,
           nhìn trống bên phải mà lại phải cuộn dài. */
        .admin-form { max-width:1080px; border-radius:var(--r-xl); box-shadow:var(--sh-card); }
        .fs-grid { display:grid; grid-template-columns:1fr 1fr; gap:0 22px; }
        /* Các khối cần chiếm trọn chiều ngang */
        .fs-grid > .fs-full,
        .fs-grid > .fs-type-choice,
        .fs-grid > .fs-section,
        .fs-grid > .fs-actions { grid-column:1 / -1; }
        @media (max-width: 900px) { .fs-grid { grid-template-columns:1fr; } }

        /* Khối con bên trong section cũng xếp 2 cột cho cân */
        .fs-section .fs-sub { display:grid; grid-template-columns:1fr 1fr; gap:0 22px; }
        @media (max-width: 900px) { .fs-section .fs-sub { grid-template-columns:1fr; } }

        .fs-section { padding:20px 22px; }
        .fs-section h3 { display:flex; align-items:center; gap:8px; padding-bottom:11px;
            margin-bottom:16px; border-bottom:1px solid var(--line-new); font-size:13px;
            letter-spacing:.02em; text-transform:uppercase; color:var(--ink-2); }
        .fs-section h3::before { content:''; width:3px; height:14px; background:var(--accent); border-radius:2px; }

        /* Dòng chọn sản phẩm: bọc thành thẻ riêng cho khỏi trôi lộn xộn */
        .product-row { background:var(--surface-2); border:1px solid var(--line-new);
            border-radius:var(--r-md); padding:10px 12px; margin-bottom:10px; }
        .product-row select, .product-row input { background:var(--surface); }
        .btn-remove-spec { flex:0 0 auto; width:34px; height:34px; display:flex;
            align-items:center; justify-content:center; padding:0; }
        .fs-actions { display:flex; gap:12px; align-items:center; padding-top:6px; }
        .fs-hint { font-size:12.5px; color:var(--ink-3); }
    </style>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="flashsale"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="${flashSale.id == null ? 'Tạo Flash Sale' : 'Sửa Flash Sale'}"/>
        <c:set var="adminCrumb" value="ADMIN / FLASH-SALES / ${flashSale.id == null ? 'NEW' : 'EDIT'}"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/flash-sales" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <c:if test="${not empty error}">
            <div class="admin-alert-err">${error}</div>
        </c:if>

        <%-- Chuẩn bị chuỗi datetime-local prefill từ LocalDateTime khi sửa --%>
        <c:set var="startAtValue" value=""/>
        <c:set var="endAtValue" value=""/>
        <c:if test="${flashSale.startAt != null}">
            <c:set var="startAtValue" value="${flashSale.startAt.year}-${flashSale.startAt.monthValue < 10 ? '0' : ''}${flashSale.startAt.monthValue}-${flashSale.startAt.dayOfMonth < 10 ? '0' : ''}${flashSale.startAt.dayOfMonth}T${flashSale.startAt.hour < 10 ? '0' : ''}${flashSale.startAt.hour}:${flashSale.startAt.minute < 10 ? '0' : ''}${flashSale.startAt.minute}"/>
        </c:if>
        <c:if test="${flashSale.endAt != null}">
            <c:set var="endAtValue" value="${flashSale.endAt.year}-${flashSale.endAt.monthValue < 10 ? '0' : ''}${flashSale.endAt.monthValue}-${flashSale.endAt.dayOfMonth < 10 ? '0' : ''}${flashSale.endAt.dayOfMonth}T${flashSale.endAt.hour < 10 ? '0' : ''}${flashSale.endAt.hour}:${flashSale.endAt.minute < 10 ? '0' : ''}${flashSale.endAt.minute}"/>
        </c:if>

        <div class="admin-form">
            <c:choose>
                <c:when test="${flashSale.id == null}">
                    <form method="post" action="/admin/flash-sales/add" enctype="multipart/form-data">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                </c:when>
                <c:otherwise>
                    <form method="post" action="/admin/flash-sales/edit/${flashSale.id}" enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                </c:otherwise>
            </c:choose>

            <div class="fs-grid">

                <div class="form-group fs-full">
                    <label>Tên chương trình</label>
                    <input type="text" name="name" value="${flashSale.name}" placeholder="VD: Flash Sale Cuối Tuần" required/>
                </div>

                <!-- CHỌN KIỂU HIỂN THỊ -->
                <div class="fs-type-choice">
                    <label>
                        <input type="radio" name="displayType" value="BANNER" id="typeBanner"
                               onclick="toggleType()"
                               ${flashSale.displayType == null || flashSale.displayType == 'BANNER' ? 'checked' : ''}/>
                        <span>Banner (ảnh + slide xoay vòng)</span>
                    </label>
                    <label>
                        <input type="radio" name="displayType" value="TEXT_BAR" id="typeTextBar"
                               onclick="toggleType()"
                               ${flashSale.displayType == 'TEXT_BAR' ? 'checked' : ''}/>
                        <span>Text bar (dòng chữ trên đầu trang)</span>
                    </label>
                </div>

                <div class="form-group">
                    <label>Thời gian bắt đầu</label>
                    <input type="datetime-local" name="startAt" value="${startAtValue}" required/>
                </div>
                <div class="form-group">
                    <label>Thời gian kết thúc</label>
                    <input type="datetime-local" name="endAt" value="${endAtValue}" required/>
                </div>
                <div class="form-group fs-full">
                    <label>Thứ tự hiển thị</label>
                    <input type="number" name="displayOrder" value="${flashSale.displayOrder != null ? flashSale.displayOrder : 0}"/>
                    <small class="fs-hint">Số nhỏ hiện trước, dùng khi có nhiều banner hoặc text bar cùng lúc.</small>
                </div>

                <!-- SECTION BANNER -->
                <div class="fs-section" id="bannerSection">
                    <h3>Nội dung Banner</h3>
                    <div class="fs-sub">
                    <div class="form-group fs-full">
                        <label>Ảnh banner</label>
                        <input type="file" name="bannerImageFile" accept="image/*" onchange="previewImage(this)"/>
                        <c:if test="${flashSale.bannerImageUrl != null && flashSale.bannerImageUrl != ''}">
                            <div style="margin-top:8px;">
                                <img id="imgPreview" src="/static/images/${flashSale.bannerImageUrl}" style="height:80px;object-fit:contain;background:var(--surface-2);border:1px solid var(--line-new);padding:4px;"/>
                                <div style="font-size:12px;color:var(--ink-3);margin-top:4px;">Ảnh hiện tại: ${flashSale.bannerImageUrl}</div>
                            </div>
                        </c:if>
                        <c:if test="${flashSale.bannerImageUrl == null || flashSale.bannerImageUrl == ''}">
                            <img id="imgPreview" style="display:none;height:80px;margin-top:8px;border-radius:4px;"/>
                        </c:if>
                    </div>
                    <div class="form-group">
                        <label>Tiêu đề</label>
                        <input type="text" name="bannerTitle" value="${flashSale.bannerTitle}" placeholder="VD: YOGO 75 Limited-Time Deal"/>
                    </div>
                    <div class="form-group">
                        <label>Phụ đề</label>
                        <input type="text" name="bannerSubtitle" value="${flashSale.bannerSubtitle}" placeholder="VD: Tri-Mode Custom Low-profile Mechanical Keyboard"/>
                    </div>
                    <div class="form-group">
                        <label>Chữ trên nút CTA</label>
                        <input type="text" name="bannerCtaText" value="${flashSale.bannerCtaText}" placeholder="VD: Shop Now"/>
                    </div>
                    <div class="form-group">
                        <label>Link khi bấm nút CTA</label>
                        <input type="text" name="bannerCtaLink" value="${flashSale.bannerCtaLink}" placeholder="VD: /products?category=2"/>
                    </div>
                    </div>
                </div>

                <!-- SECTION TEXT BAR -->
                <div class="fs-section" id="textBarSection" style="display:none;">
                    <h3>Nội dung Text Bar</h3>
                    <div class="form-group">
                        <label>Dòng chữ hiển thị trên đầu trang</label>
                        <input type="text" name="textBarContent" value="${flashSale.textBarContent}"
                               placeholder="VD: FLASH SALE — Giảm đến 30% toàn bộ chuột gaming, chỉ trong hôm nay!"/>
                    </div>
                </div>

                <!-- SẢN PHẨM ÁP DỤNG -->
                <div class="fs-section">
                    <h3>Sản phẩm áp dụng giảm giá</h3>
                    <div id="products-container">
                        <c:choose>
                            <c:when test="${not empty existingItems}">
                                <c:forEach var="item" items="${existingItems}">
                                    <div class="product-row">
                                        <select name="productIds" class="pr-product">
                                            <option value="">-- Chọn sản phẩm --</option>
                                            <c:forEach var="p" items="${products}">
                                                <option value="${p.id}" ${item.product.id == p.id ? 'selected' : ''}>${p.name} (${p.price} đ)</option>
                                            </c:forEach>
                                        </select>
                                        <select name="discountTypes" class="pr-type">
                                            <option value="PERCENT" ${item.discountType == 'PERCENT' ? 'selected' : ''}>Giảm %</option>
                                            <option value="AMOUNT" ${item.discountType == 'AMOUNT' ? 'selected' : ''}>Giảm số tiền (đ)</option>
                                        </select>
                                        <input type="number" name="discountValues" class="pr-value" value="${item.discountValue}" step="0.01" min="0" placeholder="Giá trị"/>
                                        <button type="button" class="btn-remove-spec" onclick="removeProductRow(this)">×</button>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="product-row">
                                    <select name="productIds" class="pr-product">
                                        <option value="">-- Chọn sản phẩm --</option>
                                        <c:forEach var="p" items="${products}">
                                            <option value="${p.id}">${p.name} (${p.price} đ)</option>
                                        </c:forEach>
                                    </select>
                                    <select name="discountTypes" class="pr-type">
                                        <option value="PERCENT">Giảm %</option>
                                        <option value="AMOUNT">Giảm số tiền (đ)</option>
                                    </select>
                                    <input type="number" name="discountValues" class="pr-value" step="0.01" min="0" placeholder="Giá trị"/>
                                    <button type="button" class="btn-remove-spec" onclick="removeProductRow(this)">×</button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <button type="button" class="btn-add-spec" onclick="addProductRow()">+ Thêm sản phẩm</button>
                </div>

                <div class="fs-actions">
                    <button type="submit" class="btn-primary">
                        <c:choose>
                            <c:when test="${flashSale.id == null}">Tạo Flash Sale</c:when>
                            <c:otherwise>Lưu thay đổi</c:otherwise>
                        </c:choose>
                    </button>
                    <a href="/admin/flash-sales" class="btn-edit">Hủy</a>
                </div>

            </div>
            </form>
        </div>
    </div>
</div>

<script>
    const PRODUCT_OPTIONS_HTML = `<option value="">-- Chọn sản phẩm --</option><c:forEach var="p" items="${products}">
<option value="${p.id}">${p.name} (${p.price} đ)</option></c:forEach>`;

    function toggleType() {
        const isBanner = document.getElementById('typeBanner').checked;
        document.getElementById('bannerSection').style.display = isBanner ? 'block' : 'none';
        document.getElementById('textBarSection').style.display = isBanner ? 'none' : 'block';
    }

    function addProductRow() {
        const container = document.getElementById('products-container');
        const row = document.createElement('div');
        row.className = 'product-row';
        row.innerHTML = `
            <select name="productIds" class="pr-product">${PRODUCT_OPTIONS_HTML}</select>
            <select name="discountTypes" class="pr-type">
                <option value="PERCENT">Giảm %</option>
                <option value="AMOUNT">Giảm số tiền (đ)</option>
            </select>
            <input type="number" name="discountValues" class="pr-value" step="0.01" min="0" placeholder="Giá trị"/>
            <button type="button" class="btn-remove-spec" onclick="removeProductRow(this)">×</button>
        `;
        container.appendChild(row);
    }

    function removeProductRow(btn) {
        const container = document.getElementById('products-container');
        if (container.children.length > 1) {
            btn.parentElement.remove();
        }
    }

    function previewImage(input) {
        const preview = document.getElementById('imgPreview');
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = e => {
                preview.src = e.target.result;
                preview.style.display = 'block';
            };
            reader.readAsDataURL(input.files[0]);
        }
    }

    // Set trạng thái hiển thị ban đầu khi load trang (edit mode giữ đúng loại đã chọn)
    toggleType();
</script>

</body>
</html>
