const btn = document.getElementById('togglePass');
const senhaInput = document.getElementById('senha');

if (btn && senhaInput) {
  btn.addEventListener('click', () => {
    const show = senhaInput.type === 'password';
    senhaInput.type = show ? 'text' : 'password';
    btn.classList.toggle('is-on', show);
  });
}

const form = document.querySelector('form.form');
const cpfInput = document.getElementById('cpf');

if (cpfInput) {
  // Máscara enquanto digita
  cpfInput.addEventListener('input', () => {
    let value = cpfInput.value.replace(/\D/g, '').slice(0, 11);

    value = value.replace(/(\d{3})(\d)/, '$1.$2');
    value = value.replace(/(\d{3})\.(\d{3})(\d)/, '$1.$2.$3');
    value = value.replace(/(\d{3})\.(\d{3})\.(\d{3})(\d{1,2})/, '$1.$2.$3-$4');

    cpfInput.value = value;
  });
}

if (form && cpfInput) {
  // Remove máscara antes de enviar
  form.addEventListener('submit', () => {
    cpfInput.value = cpfInput.value.replace(/\D/g, '');
  });
}

// ===== AUTO-DISMISS DOS ALERTAS =====
window.addEventListener('DOMContentLoaded', () => {
  const alerts = document.querySelectorAll('.alerts .alert');
  if (!alerts.length) return;

  // tempos (ms)
  const DEFAULT_TTL = 4500; // 4.5s
  const TTL_BY_TYPE = {
    'alert-success': 3500,
    'alert-warning': 5000,
    'alert-danger': 6500
  };

  alerts.forEach((el) => {
    // define TTL pelo tipo
    let ttl = DEFAULT_TTL;
    Object.entries(TTL_BY_TYPE).forEach(([cls, t]) => {
      if (el.classList.contains(cls)) ttl = t;
    });

    // agenda sumir
    setTimeout(() => {
      el.classList.add('is-hiding');

      // remove do DOM após a animação
      const removeAfter = 220;
      setTimeout(() => {
        el.remove();

        // se não tiver mais alertas, remove o container
        const container = document.querySelector('.alerts');
        if (container && container.children.length === 0) container.remove();
      }, removeAfter);
    }, ttl);
  });
});