<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Sản phẩm – TiTanGEAR Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <%-- Kiểu dáng của .specs-section / .spec-row / .btn-add-spec / .btn-remove-spec
         lấy hết từ admin.css, trang này không khai lại. Khai lại trong thẻ <style>
         sẽ đè ngược admin.css, vì <style> nạp sau <link>. --%>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="products"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="${product.id == null ? 'Thêm sản phẩm' : 'Sửa sản phẩm'}"/>
        <c:set var="adminCrumb" value="ADMIN / PRODUCTS / ${product.id == null ? 'NEW' : 'EDIT'}"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/products" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <%-- Khung bao ngoài của form sản phẩm. Nền thẻ trắng và bố cục 2 cột
             nằm ở .pf-main (cột trái) và .pf-side (cột phải), không đặt ở đây —
             lớp .admin-form khoá bề ngang 640px nên không dùng cho trang này. --%>
        <div>
            <c:if test="${not empty validationErrors}">
                <div style="background:var(--err-bg-2);border:1px solid var(--err-line-2);color:var(--err);padding:12px 16px;margin-bottom:18px;font-size:14px;">
                    <strong>Vui lòng sửa các lỗi sau:</strong>
                    <ul style="margin:6px 0 0 18px;">
                        <c:forEach var="err" items="${validationErrors}"><li>${err}</li></c:forEach>
                    </ul>
                </div>
            </c:if>
            <c:choose>
            <c:when test="${product.id == null}">
            <form method="post" action="/admin/products/add" enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <div class="pf-grid"><div class="pf-main">
                </c:when>
                <c:otherwise>
                <form method="post" action="/admin/products/edit/${product.id}" enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <div class="pf-grid"><div class="pf-main">
                    </c:otherwise>
                    </c:choose>

                    <div class="form-group">
                        <label>Tên sản phẩm</label>
                        <input type="text" name="name" value="${product.name}" required/>
                    </div>

                    <div class="pf-2col">
                        <div class="form-group">
                            <label>Danh mục *</label>
                            <%-- Nút chọn thay cho <select>: nhìn thấy hết lựa chọn ngay,
                                 không phải bấm mở. Radio thật vẫn nằm bên trong nên
                                 dữ liệu gửi lên server không đổi (name="category.id"). --%>
                            <div class="pf-pills">
                                <c:forEach var="cat" items="${categories}">
                                    <label class="pf-pill ${product.category != null && product.category.id == cat.id ? 'is-on' : ''}">
                                        <input type="radio" name="category.id" value="${cat.id}" required
                                               ${product.category != null && product.category.id == cat.id ? 'checked' : ''}/>
                                        ${cat.name}
                                    </label>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Giá bán (đ) *</label>
                            <input type="text" class="js-money-display" inputmode="numeric" placeholder="VD: 1 450 000" required/>
                            <input type="hidden" name="price" class="js-money-raw" value="${product.price}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Dòng sản phẩm <span style="font-weight:400;color:var(--ink-3);">(không bắt buộc)</span></label>
                        <div style="display:flex;gap:10px;flex-wrap:wrap;">
                            <select name="lineId" id="pfLineSelect" style="flex:1;min-width:200px;">
                                <option value="">— Không thuộc dòng nào —</option>
                                <c:forEach var="d" items="${productLines}">
                                    <option value="${d.id}" ${product.line != null && product.line.id == d.id ? 'selected' : ''}>${d.name}</option>
                                </c:forEach>
                            </select>
                            <input type="text" name="lineNameMoi" id="pfLineNew" placeholder="hoặc gõ tên dòng mới…"
                                   style="flex:1;min-width:200px;"/>
                        </div>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:6px;">
                            Các phiên bản cùng dòng (VD <strong>F1 Ultimate · F1 Pro · F1 Extreme</strong>) sẽ hiện
                            thành hàng nút chuyển nhanh ở trang chi tiết. Gõ trùng tên dòng đã có thì dùng lại,
                            không tạo thêm.
                        </div>
                    </div>

                    <%-- Thuộc tính dùng cho BỘ LỌC phía khách. Để trống nếu không
                         áp dụng (vd tai nghe không có loại switch). --%>
                    <div class="form-group">
                        <label>Loại switch <span style="font-weight:400;color:var(--ink-3);">(bàn phím)</span></label>
                        <select name="switchType">
                            <option value="">— Không áp dụng —</option>
                            <option value="LINEAR"   ${product.switchType == 'LINEAR'   ? 'selected' : ''}>Linear</option>
                            <option value="TACTILE"  ${product.switchType == 'TACTILE'  ? 'selected' : ''}>Tactile</option>
                            <option value="CLICKY"   ${product.switchType == 'CLICKY'   ? 'selected' : ''}>Clicky</option>
                            <option value="MAGNETIC" ${product.switchType == 'MAGNETIC' ? 'selected' : ''}>Magnetic (Hall)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Nhóm trọng lượng <span style="font-weight:400;color:var(--ink-3);">(chuột)</span></label>
                        <select name="weightGroup">
                            <option value="">— Không áp dụng —</option>
                            <option value="ULTRA" ${product.weightGroup == 'ULTRA' ? 'selected' : ''}>Siêu nhẹ &lt; 60g</option>
                            <option value="MID"   ${product.weightGroup == 'MID'   ? 'selected' : ''}>60 – 70g</option>
                            <option value="HEAVY" ${product.weightGroup == 'HEAVY' ? 'selected' : ''}>Trên 70g</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Kết nối</label>
                        <select name="connectivity">
                            <option value="">— Không áp dụng —</option>
                            <option value="WIRED"    ${product.connectivity == 'WIRED'    ? 'selected' : ''}>Có dây</option>
                            <option value="WIRELESS" ${product.connectivity == 'WIRELESS' ? 'selected' : ''}>Không dây</option>
                            <option value="DUAL"     ${product.connectivity == 'DUAL'     ? 'selected' : ''}>Dual</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Bảo hành (tháng)</label>
                        <input type="number" name="warrantyMonths" value="${product.warrantyMonths}" min="0" step="1"
                               placeholder="VD: 12 — bỏ trống nếu không bảo hành"/>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:4px;">
                            Khi đơn chuyển <strong>Hoàn thành</strong>, hệ thống tự phát phiếu bảo hành
                            (mỗi chiếc 1 mã <em>SR000001</em>…) tính từ ngày giao.
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Mô tả</label>
                        <textarea name="description">${product.description}</textarea>
                    </div>

                    <div class="form-group">
                        <label>Số lượng tồn kho (chỉ đọc)</label>
                        <%-- Ô chỉ đọc: tồn kho do trigger trong DB tự tính từ các lô nhập,
                             sửa tay ở đây không có tác dụng. Muốn tăng tồn thì vào
                             Quản lý kho rồi nhập kho. --%>
                        <input type="text" value="${product.stockQuantity != null ? product.stockQuantity : 0}" readonly
                               style="background:var(--surface-2); color:var(--ink-3); cursor:not-allowed;"
                               title="Tồn kho tự tính từ các lô nhập kho — muốn thay đổi hãy dùng chức năng Nhập kho"/>
                    </div>

                    <!-- THÔNG SỐ KỸ THUẬT -->
                    <div class="specs-section">
                        <h3>Thông số kỹ thuật</h3>
                        <div id="specs-container">
                            <c:choose>
                                <c:when test="${not empty product.specs}">
                                    <c:forEach var="spec" items="${product.specs}" varStatus="i">
                                        <div class="spec-row">
                                            <input type="text" name="specNames" value="${spec.specName}" placeholder="Tên thông số (vd: Trọng lượng)"/>
                                            <input type="text" name="specValues" value="${spec.specValue}" placeholder="Giá trị (vd: 38g)"/>
                                            <button type="button" class="btn-remove-spec" onclick="removeSpec(this)">×</button>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div class="spec-row">
                                        <input type="text" name="specNames" placeholder="Tên thông số (vd: Trọng lượng)"/>
                                        <input type="text" name="specValues" placeholder="Giá trị (vd: 38g)"/>
                                        <button type="button" class="btn-remove-spec" onclick="removeSpec(this)">×</button>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <button type="button" class="btn-add-spec" onclick="addSpec()">+ Thêm dòng</button>
                    </div>

                    <button type="submit" class="btn-primary pf-submit">
                        ${product.id == null ? 'Thêm sản phẩm' : 'Lưu thay đổi'}
                    </button>
                </div><!-- /cột trái -->

                <!-- ===== CỘT PHẢI: ẢNH SẢN PHẨM ===== -->
                <aside class="pf-side">
                    <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:14px;">ẢNH SẢN PHẨM</div>

                    <label class="pf-drop" for="pfImgInput">
                        <c:choose>
                            <c:when test="${not empty product.imageUrl}">
                                <img id="imgPreview" src="/static/images/${product.imageUrl}"
                                     onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                            </c:when>
                            <c:otherwise>
                                <img id="imgPreview" style="display:none;"/>
                            </c:otherwise>
                        </c:choose>
                        <span class="pf-drop-empty" ${not empty product.imageUrl ? 'style="display:none;"' : ''}>
                            <span class="pf-drop-ico">
                                <svg width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 11V3.5M5 6.5 8 3.5l3 3"/><path d="M2.5 11v1.5a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1V11"/></svg>
                            </span>
                            <span class="pf-drop-t">Tải ảnh lên</span>
                            <span class="pf-drop-s">PNG, JPG · nền trong suốt là tốt nhất</span>
                        </span>
                    </label>
                    <input type="file" name="imageFile" id="pfImgInput" accept="image/*"
                           onchange="previewImage(this)" style="display:none;"/>

                    <c:if test="${not empty product.imageUrl}">
                        <div class="tg-mono" style="font-size:11px;color:var(--ink-4);margin-top:10px;word-break:break-all;">
                            Ảnh hiện tại: ${product.imageUrl}
                        </div>
                    </c:if>
                    <p style="font-size:12px;color:var(--ink-3);line-height:1.6;margin:14px 0 0;">
                        Ảnh chính hiển thị ở lưới sản phẩm và trang chi tiết.
                        Biến thể có thể dùng ảnh riêng.
                    </p>
                </aside>
            </div><!-- /.pf-grid -->
                </form>
        </div>
    </div>
</div>

<script>
    function addSpec() {
        const container = document.getElementById('specs-container');
        const row = document.createElement('div');
        row.className = 'spec-row';
        row.innerHTML = `
            <input type="text" name="specNames" placeholder="Tên thông số (vd: Trọng lượng)"/>
            <input type="text" name="specValues" placeholder="Giá trị (vd: 38g)"/>
            <button type="button" class="btn-remove-spec" onclick="removeSpec(this)">×</button>
        `;
        container.appendChild(row);
    }

    function removeSpec(btn) {
        const container = document.getElementById('specs-container');
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
                // Ẩn phần chữ "Tải ảnh lên" đi, nếu không ảnh và chữ chồng lên nhau
                const rong = document.querySelector('.pf-drop-empty');
                if (rong) rong.style.display = 'none';
            };
            reader.readAsDataURL(input.files[0]);
        }
    }
</script>
<script src="/static/js/price-format.js"></script>

</body>
</html>
