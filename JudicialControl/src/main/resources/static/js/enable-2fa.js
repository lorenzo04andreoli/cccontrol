    const boxes = document.querySelectorAll('.otpBox');
    const hiddenInput = document.getElementById('otpValue');

    function updateHidden(){
      hiddenInput.value = Array.from(boxes)
        .map(b => b.value)
        .join('');
    }

    boxes.forEach((box, index) => {
      box.addEventListener('input', () => {
        box.value = box.value.replace(/\D/g, '');

        if (box.value && index < boxes.length - 1) {
          boxes[index + 1].focus();
        }

        updateHidden();
      });

      box.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !box.value && index > 0) {
          boxes[index - 1].focus();
        }
      });

      box.addEventListener('paste', (e) => {
        const text = (e.clipboardData || window.clipboardData).getData('text');
        const digits = (text || '').replace(/\D/g, '').slice(0, boxes.length);
        if (!digits) return;

        e.preventDefault();
        digits.split('').forEach((d, i) => {
          boxes[i].value = d;
        });
        boxes[Math.min(digits.length, boxes.length) - 1].focus();
        updateHidden();
      });
    });