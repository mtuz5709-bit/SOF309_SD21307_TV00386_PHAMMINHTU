<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Biến thể – ${product.name}</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .variant-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; margin-bottom: 32px; }
        .variant-card {
            background: var(--surface); border: 1px solid var(--line-new); border-radius: var(--r-xl);
            box-shadow: var(--sh-card); padding: 16px;
            transition: transform .3s var(--ease), box-shadow .3s ease, border-color .3s ease;
        }
        .variant-card:hover { transform: translateY(-4px); box-shadow: var(--sh-hover); border-color: #D6DEF9; }
        .variant-card img { width: 100%; height: 130px; object-fit: contain; border-radius: var(--r-lg); background: var(--surface-2); margin-bottom: 12px; mix-blend-mode: multiply; }
        .variant-card .no-img { width: 100%; height: 130px; background: var(--surface-2); border-radius: var(--r-lg); display: flex; align-items: center; justify-content: center; color: var(--ink-4); font-family: var(--font-mono); font-size: 11px; margin-bottom: 12px; }
        .variant-name { font-weight: 700; font-size: 14px; margin-bottom: 4px; color: var(--ink); }
        .variant-color { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ink-2); margin-bottom: 4px; }
        .color-dot { width: 20px; height: 20px; border-radius: 50%; border: 1px solid var(--line-new); flex-shrink: 0; }
        .variant-price { font-family: var(--font-mono); font-weight: 700; font-size: 15px; color: var(--accent); margin: 4px 0; }
        .variant-stock { font-size: 13px; color: var(--ink-3); margin-bottom: 10px; }
        .variant-actions { display: flex; gap: 8px; }
        .btn-edit-variant { padding: 6px 14px; background: var(--accent); color: #fff; border: 1px solid var(--accent); border-radius: var(--r-md); font-size: 12px; font-weight: 600; cursor: pointer; transition: box-shadow .22s ease; }
        .btn-edit-variant:hover { box-shadow: var(--sh-blue); }
        .btn-del-variant { padding: 6px 14px; background: var(--surface); color: var(--err); border: 1px solid var(--err-line-2); border-radius: var(--r-md); font-size: 12px; font-weight: 600; cursor: pointer; text-decoration: none; transition: background .2s ease, color .2s ease; }
        .btn-del-variant:hover { background: var(--err); border-color: var(--err); color: #fff; }

        /* Edit form inline */
        .edit-form { display: none; margin-top: 14px; border-top: 1px solid var(--line-soft); padding-top: 14px; }
        .edit-form.open { display: block; }
        .ef-row { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px; }
        .ef-label { font-size: 11px; color: var(--ink-4); margin-bottom: 3px; display: block; font-family: var(--font-mono); letter-spacing: .06em; }
        .ef-input { width: 100%; padding: 8px 10px; border: 1px solid var(--line-new); border-radius: var(--r-md); font-size: 13px; box-sizing: border-box; font-family: inherit; outline: none; transition: border-color .2s ease, box-shadow .2s ease; }
        .ef-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(27,73,255,.12); }
        .ef-save { width: 100%; padding: 9px; background: var(--accent); color: #fff; border: 1px solid var(--accent); border-radius: var(--r-md); font-size: 13px; font-weight: 600; cursor: pointer; margin-top: 4px; transition: box-shadow .22s ease; }
        .ef-save:hover { box-shadow: var(--sh-blue); }

        /* Add form */
        .add-form { background: var(--surface); border: 1px solid var(--line-new); border-radius: var(--r-xl); box-shadow: var(--sh-card); padding: 24px; }
        .add-form h3 { font-size: 15px; font-weight: 700; margin-bottom: 18px; color: var(--ink); }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; }
        .form-row label { font-size: 13px; color: var(--ink-2); margin-bottom: 4px; display: block; }
        .form-row input { width: 100%; padding: 9px 11px; border: 1px solid var(--line-new); border-radius: var(--r-md); font-size: 13px; box-sizing: border-box; font-family: inherit; outline: none; transition: border-color .2s ease, box-shadow .2s ease; }
        .form-row input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(27,73,255,.12); }
        .color-preview { width: 32px; height: 32px; border-radius: 50%; border: 1px solid var(--line-new); display: inline-block; vertical-align: middle; margin-left: 8px; }
        .alert { padding: 10px 16px; border-radius: var(--r-md); font-size: 13px; margin-bottom: 16px; }
        .alert-success { background: var(--ok-bg-2); border: 1px solid var(--ok-line-2); color: var(--ok-deep); }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="products"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Biến thể: ${product.name}"/>
        <c:set var="adminCrumb" value="ADMIN / PRODUCTS / VARIANTS"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/products" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <!-- THÔNG BÁO -->
        <c:if test="${param.added == 'true'}">
            <div class="alert alert-success">Thêm biến thể thành công!</div>
        </c:if>
        <c:if test="${param.updated == 'true'}">
            <div class="alert alert-success">Cập nhật biến thể thành công!</div>
        </c:if>
        <c:if test="${param.deleted == 'true'}">
            <div class="alert alert-success">Đã ẩn biến thể (ngừng bán) — lịch sử đơn hàng vẫn được giữ.</div>
        </c:if>
        <c:if test="${param.assigned == 'true'}">
            <div class="alert alert-success">Đã gán lô tồn vào biến thể — tồn kho đã được tính lại.</div>
        </c:if>
        <c:if test="${param.error == 'assign'}">
            <div class="alert" style="background:var(--err-bg-2);border:1px solid var(--err-line-2);color:var(--err);">
                Không gán được lô: biến thể không thuộc sản phẩm này hoặc lô đã được gán.
            </div>
        </c:if>
        <c:if test="${param.error == 'hasStock'}">
            <div class="alert" style="background:var(--err-bg-2);border:1px solid var(--err-line-2);color:var(--err);">
                Không thể ẩn biến thể vì còn <strong>${param.stock}</strong> tồn kho.
                Hãy bán hết số này (hoặc nhập/xuất cho về 0) trước khi ẩn — tránh làm tồn kho bị "kẹt".
            </div>
        </c:if>

        <!-- TỔNG QUAN TỒN KHO (Model 2: tồn đến từ nhập kho theo biến thể, trigger tự cộng) -->
        <div style="background:var(--surface);border:1px solid var(--line-new);padding:18px 22px;margin-bottom:20px;
                    display:flex;gap:32px;align-items:center;flex-wrap:wrap;">
            <div>
                <div style="font-family:var(--font-mono);font-size:11px;letter-spacing:0.08em;color:var(--ink-3);text-transform:uppercase;">Tổng tồn sản phẩm</div>
                <div style="font-size:22px;font-weight:700;color:var(--ink);">${product.stockQuantity}</div>
            </div>
            <div>
                <div style="font-family:var(--font-mono);font-size:11px;letter-spacing:0.08em;color:var(--ink-3);text-transform:uppercase;">Tổng tồn các biến thể</div>
                <div style="font-size:22px;font-weight:700;color:var(--ink);">${totalStock}</div>
            </div>
            <div style="flex:1;min-width:220px;font-size:13px;color:var(--ink-2);line-height:1.5;">
                Tồn kho đến từ <strong>Nhập kho theo biến thể</strong> (Kho hàng) và tự cộng lên tồn sản phẩm —
                không còn phân bổ tay. Hai số trên khớp nhau (trừ khi còn hàng chưa gắn biến thể bên dưới).
            </div>
        </div>

        <!-- HƯỚNG A: HÀNG BỊ KẸT (nhập kho TRƯỚC khi tạo biến thể) -->
        <c:if test="${not empty strandedLots}">
            <div class="alert" style="background:var(--warn-bg-2);border:1px solid var(--warn-line-2);color:var(--warn-deep);">
                <div style="font-weight:700;margin-bottom:4px;">⚠ Có ${strandedTotal} sản phẩm tồn kho chưa gắn biến thể</div>
                <div style="font-size:13px;margin-bottom:12px;color:var(--ink-2);">
                    Số này nhập <strong>trước khi tạo biến thể</strong> nên đang là "hàng chung", chưa bán được qua biến thể.
                    Gán từng lô vào đúng biến thể để hệ thống tính lại tồn:
                </div>
                <c:forEach var="lot" items="${strandedLots}">
                    <form method="post" action="/admin/products/${product.id}/variants/assign-stock"
                          style="display:flex;align-items:center;gap:10px;margin-bottom:8px;flex-wrap:wrap;">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <input type="hidden" name="lotId" value="${lot.id}"/>
                        <span style="font-size:13px;color:var(--ink);min-width:230px;">
                            Lô #${lot.id} — còn <strong>${lot.remainingQuantity}</strong>
                            <c:if test="${not empty lot.importedAt}"> (nhập ${lot.importedAt.toString().substring(0,10)})</c:if>
                        </span>
                        <select name="variantId" required class="filter-select" style="padding:6px 28px 6px 10px;">
                            <option value="">— Chọn biến thể —</option>
                            <c:forEach var="v" items="${variants}">
                                <option value="${v.id}">${v.name}<c:if test="${not empty v.color}"> - ${v.color}</c:if></option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn-primary" style="padding:6px 16px;">Gán</button>
                    </form>
                </c:forEach>
            </div>
        </c:if>

        <!-- DANH SÁCH BIẾN THỂ -->
        <c:choose>
            <c:when test="${empty variants}">
                <p style="color:var(--ink-3);margin-bottom:24px;">Chưa có biến thể nào. Thêm bên dưới.</p>
            </c:when>
            <c:otherwise>
                <div class="variant-grid">
                    <c:forEach var="v" items="${variants}">
                        <div class="variant-card">
                            <!-- ẢNH -->
                            <c:choose>
                                <c:when test="${not empty v.imageUrl}">
                                    <img src="/static/images/${v.imageUrl}" alt="${v.name}"/>
                                </c:when>
                                <c:otherwise>
                                    <div class="no-img">Chưa có ảnh</div>
                                </c:otherwise>
                            </c:choose>

                            <!-- THÔNG TIN -->
                            <div class="variant-name">${v.name}</div>
                            <c:if test="${not empty v.color}">
                                <div class="variant-color">
                                    <c:if test="${not empty v.colorCode}">
                                        <span class="color-dot" style="background:${v.colorCode};"></span>
                                    </c:if>
                                        ${v.color}
                                </div>
                            </c:if>
                            <div class="variant-price"><fmt:formatNumber value="${v.price}" pattern="#,##0"/>đ</div>
                            <c:set var="vReserved" value="${empty reservedMap[v.id] ? 0 : reservedMap[v.id]}"/>
                            <div class="variant-stock">
                                Tồn: <strong>${v.stock}</strong> ·
                                Đang giữ: <strong>${vReserved}</strong> ·
                                Bán được: <strong style="color:var(--ok-deep);">${v.stock - vReserved}</strong>
                            </div>

                            <!-- NÚT -->
                            <div class="variant-actions">
                                <button class="btn-edit-variant" onclick="toggleEdit(${v.id})">Sửa</button>
                                <a href="/warehouse/stock" class="btn-edit" style="padding:6px 14px;font-size:12px;">Nhập kho</a>
                                <a href="/admin/products/${product.id}/variants/delete/${v.id}"
                                   class="btn-del-variant" data-post
                                   data-confirm="Ẩn (ngừng bán) biến thể này? Lịch sử đơn vẫn được giữ.">Ẩn</a>
                            </div>

                            <!-- FORM SỬA INLINE -->
                            <div class="edit-form" id="edit-${v.id}">
                                <form method="post" action="/admin/products/${product.id}/variants/edit/${v.id}"
                                      enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                    <div class="ef-row">
                                        <div>
                                            <label class="ef-label">Tên dòng *</label>
                                            <input type="text" name="name" class="ef-input" value="${v.name}" placeholder="(không bắt buộc)"/>
                                        </div>
                                        <div>
                                            <label class="ef-label">Màu sắc</label>
                                            <input type="text" name="color" class="ef-input" value="${v.color}"/>
                                        </div>
                                    </div>
                                    <div class="ef-row">
                                        <div>
                                            <label class="ef-label">Mã màu (hex)</label>
                                            <input type="text" name="colorCode" class="ef-input" value="${v.colorCode}" placeholder="#000000"/>
                                        </div>
                                        <%-- Biến thể chỉ khai tên màu và mã màu, không có ô nhập giá:
                                             mọi màu dùng chung giá của sản phẩm. Muốn bán khác giá
                                             thì tạo một sản phẩm riêng trong cùng dòng. --%>
                                    </div>
                                    <div class="ef-row">
                                        <div>
                                            <label class="ef-label">Tên ảnh có sẵn</label>
                                            <input type="text" name="existingImage" class="ef-input" placeholder="VD: atk_f1_ultimate.png" value="${v.imageUrl}"/>
                                        </div>
                                        <div>
                                            <label class="ef-label">Hoặc upload ảnh mới</label>
                                            <input type="file" name="imageFile" class="ef-input" accept="image/*" style="padding:4px;"/>
                                        </div>
                                    </div>
                                    <button type="submit" class="ef-save">Lưu thay đổi</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- FORM THÊM MỚI -->
        <div class="add-form">
            <h3>+ Thêm biến thể mới</h3>
            <form method="post" action="/admin/products/${product.id}/variants/add" enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="form-row">
                    <div>
                        <label>Tên gọi thêm <span style="font-weight:400;color:var(--ink-3);">(không bắt buộc)</span></label>
                        <input type="text" name="name" placeholder="Để trống nếu chỉ khác màu"/>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:4px;">
                            Phiên bản có <strong>thông số kỹ thuật khác nhau</strong> (VD: F1 Ultimate vs F1 Extreme)
                            thì tạo <strong>sản phẩm riêng</strong>, đừng tạo biến thể — vì thông số gắn theo sản phẩm.
                            Biến thể chỉ dùng cho khác biệt <strong>không đổi thông số</strong>, chủ yếu là màu.
                        </div>
                    </div>
                    <div>
                        <label>Màu sắc</label>
                        <input type="text" name="color" placeholder="VD: Đen, Trắng, Đỏ"/>
                    </div>
                </div>
                <div class="form-row">
                    <div>
                        <label>Mã màu (hex)</label>
                        <div style="display:flex;align-items:center;">
                            <input type="text" name="colorCode" id="colorCodeInput" placeholder="#000000" value="#000000"
                                   oninput="document.getElementById('colorPreview').style.background=this.value"/>
                            <span class="color-preview" id="colorPreview" style="background:#000000;"></span>
                        </div>
                    </div>
                    <div>
                        <label>Tên ảnh có sẵn (nếu đã có trong images/)</label>
                        <input type="text" name="existingImage" placeholder="VD: atk_f1_ultimate.png"/>
                        <label style="margin-top:6px;">Hoặc upload ảnh mới</label>
                        <input type="file" name="imageFile" accept="image/*"/>
                    </div>
                </div>
                <div class="form-row">
                    <%-- Hàng này chỉ in dòng nhắc, không có ô nhập giá: biến thể
                         chỉ khác màu nên giá luôn lấy theo sản phẩm. --%>
                    <div style="display:flex;align-items:flex-end;">
                        <span style="font-size:12px;color:var(--ink-3);line-height:1.5;">
                            Giá bán lấy theo sản phẩm — mọi màu chung một giá.<br/>
                            Tồn kho bắt đầu = 0. Thêm hàng qua <strong>Kho hàng → Nhập kho theo biến thể</strong>.
                        </span>
                    </div>
                </div>
                <button type="submit" class="btn-primary" style="padding:10px 28px;">Thêm biến thể</button>
            </form>
        </div>
    </div>
</div>

<script>
    function toggleEdit(id) {
        var form = document.getElementById('edit-' + id);
        form.classList.toggle('open');
    }
</script>
<script src="/static/js/price-format.js"></script>
</body>
</html>
