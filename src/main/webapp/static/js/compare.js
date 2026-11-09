/* ============================================================
   SO SÁNH SẢN PHẨM — lưu ở TRÌNH DUYỆT, không đụng CSDL.

   Lý do không lưu server: đây là lựa chọn tạm của một phiên xem hàng, khách
   chưa đăng nhập cũng phải dùng được. Lưu vào CSDL thì phải thêm bảng, thêm
   dọn rác, mà chẳng ai cần xem lại danh sách so sánh của tháng trước.
   ============================================================ */
window.TGCompare = (function () {
    var KHOA   = 'tg_compare';
    var TOI_DA = 4;   // quá 4 cột thì bảng tràn ngang, đọc không nổi

    function lay() {
        try {
            var v = JSON.parse(localStorage.getItem(KHOA) || '[]');
            return Array.isArray(v) ? v.filter(function (x) { return typeof x === 'number'; }) : [];
        } catch (e) {
            return [];   // localStorage hỏng/bị chặn -> coi như chưa chọn gì
        }
    }

    function dat(ds) {
        try { localStorage.setItem(KHOA, JSON.stringify(ds.slice(0, TOI_DA))); } catch (e) { /* bỏ qua */ }
        ve();
        return lay();
    }

    function co(id)  { return lay().indexOf(id) !== -1; }
    function bo(id)  { return dat(lay().filter(function (x) { return x !== id; })); }

    function bat(id) {
        var ds = lay();
        if (ds.indexOf(id) !== -1) return bo(id);
        if (ds.length >= TOI_DA) {
            alert('Chỉ so sánh được tối đa ' + TOI_DA + ' sản phẩm cùng lúc.');
            return ds;
        }
        ds.push(id);
        return dat(ds);
    }

    /* ---- Thanh nổi ở đáy trang ---- */
    function ve() {
        var ds  = lay();
        var oId = 'tgCmpBar';
        var cu  = document.getElementById(oId);

        if (!ds.length) { if (cu) cu.remove(); danhDauThe(); return; }

        var thanh = cu;
        if (!thanh) {
            thanh = document.createElement('div');
            thanh.id = oId;
            thanh.className = 'tg-cmp-bar';
            document.body.appendChild(thanh);
        }

        var anh = ds.map(function (id) {
            var el  = document.querySelector('[data-cmp-id="' + id + '"]');
            var src = el ? el.getAttribute('data-cmp-img') : '';
            var ten = el ? el.getAttribute('data-cmp-ten') : '';
            return '<span class="tg-cmp-item" title="' + ten + '">'
                 + (src ? '<img src="' + src + '" alt="">' : '<span class="tg-cmp-no">' + id + '</span>')
                 + '<button type="button" class="tg-cmp-x" onclick="TGCompare.bo(' + id + ')">×</button>'
                 + '</span>';
        }).join('');

        thanh.innerHTML =
            '<span class="tg-cmp-label">ĐANG SO SÁNH</span>' +
            '<span class="tg-cmp-list">' + anh + '</span>' +
            '<a class="tg-btn tg-primary tg-cmp-go" href="/so-sanh?ids=' + ds.join(',') + '">So sánh ngay →</a>' +
            '<button type="button" class="tg-cmp-clear" onclick="TGCompare.dat([])">Xóa</button>';

        danhDauThe();
    }

    /* Tô sáng nút so sánh trên đúng những thẻ đang được chọn */
    function danhDauThe() {
        var ds = lay();
        document.querySelectorAll('[data-cmp-id]').forEach(function (el) {
            var id  = Number(el.getAttribute('data-cmp-id'));
            var nut = el.querySelector('.tg-cmp-btn');
            if (nut) nut.classList.toggle('is-on', ds.indexOf(id) !== -1);
        });
    }

    document.addEventListener('DOMContentLoaded', ve);

    return { lay: lay, dat: dat, bat: bat, bo: bo, co: co, ve: ve };
})();
