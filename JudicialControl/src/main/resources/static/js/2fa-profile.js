    // OTP 6 inputs
    const boxes = document.querySelectorAll('.otpBox');
    const hidden = document.getElementById('otpValue');
    const form = document.getElementById('twofaEnableForm');

    function updateHidden(){
      if (!hidden) return;
      hidden.value = Array.from(boxes).map(b => b.value).join('');
    }

    boxes.forEach((box, idx) => {
      box.addEventListener('input', () => {
        box.value = box.value.replace(/\D/g, '').slice(0, 1);
        if (box.value && idx < boxes.length - 1) boxes[idx + 1].focus();
        updateHidden();
      });

      box.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !box.value && idx > 0) boxes[idx - 1].focus();
      });

      box.addEventListener('paste', (e) => {
        const text = (e.clipboardData || window.clipboardData).getData('text');
        const digits = (text || '').replace(/\D/g, '').slice(0, 6);
        if (!digits) return;

        e.preventDefault();
        digits.split('').forEach((d, i) => { if (boxes[i]) boxes[i].value = d; });
        boxes[Math.min(digits.length, 6) - 1]?.focus();
        updateHidden();
      });
    });

    // Garante que só envia com 6 dígitos
    if (form) {
      form.addEventListener('submit', (e) => {
        updateHidden();
        if (!hidden || hidden.value.length !== 6) {
          e.preventDefault();
          alert('Digite os 6 dígitos do código.');
        }
      });
    }

    // Copiar secret
    const btnCopy = document.getElementById('copySecret');
    if (btnCopy) {
      btnCopy.addEventListener('click', async () => {
        const secretEl = document.querySelector('.secretValue');
        const secret = secretEl ? secretEl.textContent.trim() : '';
        if (!secret) return;
        try{
          await navigator.clipboard.writeText(secret);
          btnCopy.innerHTML = '<i class="fas fa-check"></i> Copiado';
          setTimeout(() => btnCopy.innerHTML = '<i class="fas fa-copy"></i> Copiar', 1400);
        }catch(e){}
      });
    }