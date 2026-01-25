const boxes = document.querySelectorAll('.otpBox');
    const hiddenInput = document.getElementById('otpValue');
    const form = document.querySelector('form.form');

    function updateHidden(){
      hiddenInput.value = Array.from(boxes).map(b => b.value).join('');
    }

    boxes.forEach((box, index) => {
      box.addEventListener('input', () => {
        box.value = box.value.replace(/\D/g, '').slice(0, 1);
        if (box.value && index < boxes.length - 1) boxes[index + 1].focus();
        updateHidden();
      });

      box.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !box.value && index > 0) boxes[index - 1].focus();
      });

      box.addEventListener('paste', (e) => {
        const text = (e.clipboardData || window.clipboardData).getData('text');
        const digits = (text || '').replace(/\D/g, '').slice(0, boxes.length);
        if (!digits) return;

        e.preventDefault();
        digits.split('').forEach((d, i) => { if (boxes[i]) boxes[i].value = d; });
        updateHidden();
        boxes[Math.min(digits.length, boxes.length) - 1].focus();
      });
    });

    // garante que o hidden vai com 6 dígitos
    if (form) {
      form.addEventListener('submit', (e) => {
        updateHidden();
        if (!hiddenInput.value || hiddenInput.value.length !== 6) {
          e.preventDefault();
          alert("Digite os 6 dígitos do código.");
        }
      });
    }