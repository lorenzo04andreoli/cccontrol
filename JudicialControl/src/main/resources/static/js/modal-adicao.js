document.addEventListener('DOMContentLoaded', () => {
  const modal = document.getElementById('modalAdicionar');
  const form = document.getElementById('formAdicionarReeducando');
  const btnAbrir = document.getElementById('btnAbrirAdicionar');
  const btnFechar = document.getElementById('btnFecharModalAdicionar');

  if (!modal || !form) return;

  const abrir = () => modal.classList.add('show');
  const fechar = () => {
    modal.classList.remove('show');
    form.reset();
  };

  btnAbrir?.addEventListener('click', abrir);
  btnFechar?.addEventListener('click', (e) => {
    e.preventDefault();
    fechar();
  });

  modal.addEventListener('click', (e) => {
    if (e.target === modal) fechar();
  });

  form.addEventListener('submit', (e) => {
    e.preventDefault();

    const payload = {
      nome: document.getElementById("novoNome")?.value,
      autos: document.getElementById("novoAutos")?.value,
      cpf: document.getElementById("novoCpf")?.value,
      telefone: document.getElementById("novoTelefone")?.value,
      dia: document.getElementById("novoDia")?.value,
      frequencia: document.getElementById("novaFrequencia")?.value
    };

    apiFetch('/reeducandos', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', ...csrfHeader() },
      body: JSON.stringify(payload)
    }).then(async (res) => {
      if (res.ok) {
        fechar();
        window.location.reload();
      } else {
        alert(await res.text());
      }
    });
  });
});
