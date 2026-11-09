/* Biến các link hành động (xóa, hủy, bật/tắt) thành request POST kèm token CSRF.
 *
 * Cách dùng trong JSP: giữ nguyên thẻ <a> và class CSS cũ, chỉ thêm data-post
 *     <a href="/admin/users/${u.id}/delete" data-post
 *        data-confirm="Xóa người dùng này?" class="btn-delete">Xóa</a>
 *
 * Mọi thao tác đổi dữ liệu phải là POST vì Spring Security chỉ kiểm token CSRF
 * trên POST/PUT/PATCH/DELETE, còn GET được miễn hoàn toàn — để GET thì thẻ
 * <img src="..."> trên một trang web khác cũng xóa được dữ liệu.
 *
 * Trình duyệt tắt JS thì link chạy GET và nhận 405 Method Not Allowed, tức là
 * không làm gì cả — an toàn hơn là âm thầm xóa. */
(function () {
  function the(name) {
    var m = document.querySelector('meta[name="' + name + '"]');
    return m ? m.getAttribute('content') : null;
  }

  document.addEventListener('click', function (e) {
    var a = e.target.closest ? e.target.closest('a[data-post]') : null;
    if (!a) return;

    e.preventDefault();

    var loiNhac = a.getAttribute('data-confirm');
    if (loiNhac && !window.confirm(loiNhac)) return;

    var form = document.createElement('form');
    form.method = 'POST';
    form.action = a.getAttribute('href');
    form.style.display = 'none';

    var ten = the('_csrf_param');
    var token = the('_csrf');
    if (ten && token) {
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = ten;
      input.value = token;
      form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
  });
})();
