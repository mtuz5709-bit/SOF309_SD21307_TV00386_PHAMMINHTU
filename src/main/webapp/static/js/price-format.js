/**
 * Định dạng số nhập vào có dấu cách mỗi 3 chữ số (vd: 1 450 000) để dễ đọc,
 * nhưng vẫn gửi lên server đúng số thô không dấu cách (vd: 1450000).
 *
 * Cách dùng trong JSP — đặt 2 input cạnh nhau, input hiển thị PHẢI đứng ngay
 * trước input ẩn giữ giá trị thật:
 *
 *   <input type="text" class="js-money-display" inputmode="numeric" placeholder="VD: 1 450 000"/>
 *   <input type="hidden" name="price" class="js-money-raw" value="${product.price}"/>
 *
 * Tự động chạy khi trang load (DOMContentLoaded). Nếu thêm input mới bằng
 * JavaScript (vd: thêm dòng động), gọi lại window.scanMoneyInputs() sau khi
 * chèn xong DOM mới để áp dụng cho các input vừa thêm.
 */
(function () {
    function formatWithSpaces(digitsOnly) {
        return digitsOnly.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    }

    function findRawInput(display) {
        // Ưu tiên tìm input.js-money-raw trong cùng cha, nếu không có thì lấy phần tử liền kề
        var parent = display.parentElement;
        var raw = parent ? parent.querySelector('.js-money-raw') : null;
        return raw || display.nextElementSibling;
    }

    function initField(display) {
        if (display.dataset.moneyInit === '1') return;
        display.dataset.moneyInit = '1';

        var raw = findRawInput(display);
        if (!raw) return;

        if (raw.value) {
            var initialDigits = raw.value.toString().split('.')[0].replace(/\D/g, '');
            display.value = formatWithSpaces(initialDigits);
        }

        display.addEventListener('input', function () {
            var digitsOnly = display.value.replace(/\D/g, '');
            raw.value = digitsOnly;
            display.value = formatWithSpaces(digitsOnly);
        });
    }

    function scan(root) {
        (root || document).querySelectorAll('.js-money-display').forEach(initField);
    }

    // Đồng bộ lần cuối trước khi submit — phòng trường hợp autofill/paste
    // không bắn sự kiện "input"
    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!form.querySelectorAll) return;
        form.querySelectorAll('.js-money-display').forEach(function (display) {
            var raw = findRawInput(display);
            if (raw) raw.value = display.value.replace(/\D/g, '');
        });
    }, true);

    document.addEventListener('DOMContentLoaded', function () { scan(document); });

    window.scanMoneyInputs = scan;
})();
