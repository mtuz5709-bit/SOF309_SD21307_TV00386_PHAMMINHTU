/* Chặn gửi trùng form: lần bấm đầu cho qua, các lần sau hủy. Nút bị khóa và đổi
   chữ để người dùng biết hệ thống đang xử lý chứ không phải bị đơ.
   Nạp một lần ở admin-topbar.jsp và footer.jsp nên áp cho mọi form của cả site.
   Không có nó thì bấm "Lưu" hai lần sẽ tạo hai bản ghi trùng nhau mà không báo gì. */
(function () {
    'use strict';

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!(form instanceof HTMLFormElement)) return;

        // Form tìm kiếm / lọc (GET) thì không chặn: bấm lại nhiều lần là bình thường
        // và cũng không tạo thêm dữ liệu gì.
        if ((form.method || 'get').toLowerCase() !== 'post') return;
        if (form.hasAttribute('data-cho-gui-lai')) return;

        if (form.dataset.dangGui === '1') {
            e.preventDefault();
            e.stopImmediatePropagation();
            return;
        }
        form.dataset.dangGui = '1';

        var nut = form.querySelector('button[type="submit"], input[type="submit"]');
        if (nut) {
            var chuCu = nut.tagName === 'INPUT' ? nut.value : nut.innerHTML;
            nut.disabled = true;
            if (nut.tagName === 'INPUT') nut.value = 'Đang xử lý…';
            else nut.innerHTML = 'Đang xử lý…';

            /* Mở khoá lại sau 8 giây: nếu server trả về trang lỗi ngay trên cùng
               URL (không tải lại trang) thì người dùng vẫn bấm gửi lại được,
               không bị kẹt cứng với cái nút chết. */
            setTimeout(function () {
                form.dataset.dangGui = '';
                nut.disabled = false;
                if (nut.tagName === 'INPUT') nut.value = chuCu;
                else nut.innerHTML = chuCu;
            }, 8000);
        }
    }, true);
})();
