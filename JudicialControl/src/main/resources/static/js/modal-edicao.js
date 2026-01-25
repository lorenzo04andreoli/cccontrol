document.addEventListener('DOMContentLoaded', () => {
  const modal = document.getElementById('modal');
  const btnFechar = document.getElementById('btnFecharModalEdicao');
  const btnExcluir = document.getElementById('btnExcluirReeducando');
  const form = document.getElementById('formReeducando');

  if (!modal || !form) return;

  const abrir = () => modal.classList.add('show');
  const fechar = () => modal.classList.remove('show');

  btnFechar?.addEventListener('click', fechar);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) fechar();
  });

  document.addEventListener('click', (e) => {
    const row = e.target.closest('.row-reeducando');
    if (!row) return;

    const id = row.getAttribute('data-id');
    if (!id) return;

    fetch('/reeducandos/' + id, { credentials: 'same-origin' })
      .then(res => res.json())
      .then(data => {
        document.getElementById('id').value = data.id;
        const autos = (data.autos ?? '').trim();
        document.getElementById('autosText').textContent = autos || '—';
        document.getElementById('nome').value = data.nome ?? '';
        document.getElementById('cpf').value = data.cpf ?? '';
        document.getElementById('telefone').value = data.telefone ?? '';
        document.getElementById('dia').value = data.dia ?? '';
        abrir();
      });
  });

  btnExcluir?.addEventListener('click', async () => {
    if (!confirm('Tem certeza que deseja excluir?')) return;

    const id = document.getElementById('id').value;

    const res = await apiFetch('/reeducandos/' + id, {
      method: 'DELETE',
      headers: { ...csrfHeader() }
    });

    if (res.ok || res.status === 204) {
      window.location.reload();
    } else {
      alert(await res.text());
    }
  });

  form.addEventListener('submit', (e) => {
    e.preventDefault();

    const id = document.getElementById('id').value;
    const payload = {
      telefone: document.getElementById('telefone').value,
      dia: document.getElementById('dia').value
    };

    apiFetch('/reeducandos/' + id, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...csrfHeader() },
      body: JSON.stringify(payload)
    }).then(() => {
      fechar();
      window.location.reload();
    });
  });

  const btnCopiarAutos = document.getElementById('btnCopiarAutos');

  btnCopiarAutos?.addEventListener('click', async () => {
    const valor = document.getElementById('autosText')?.textContent || '';
    if (!valor.trim() || valor.trim() === '—') return;

    try {
      await navigator.clipboard.writeText(valor.trim());
      btnCopiarAutos.innerHTML = '<i class="fas fa-check"></i>';
      setTimeout(() => btnCopiarAutos.innerHTML = '<i class="fas fa-copy"></i>', 900);
    } catch (e) {
      // fallback
      const range = document.createRange();
      range.selectNode(document.getElementById('autosText'));
      const sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(range);
      document.execCommand('copy');
      sel.removeAllRanges();
    }
  });


    const btnArquivar = document.getElementById('btnArquivarReeducando');
    const modalConfirm = document.getElementById('modalConfirmArquivar');
    const modalObs = document.getElementById('modalObsArquivar');

    const btnSim = document.getElementById('btnConfirmArquivarSim');
    const btnNao = document.getElementById('btnConfirmArquivarNao');

    const btnSalvar = document.getElementById('btnSalvarArquivamento');
    const btnCancelarObs = document.getElementById('btnCancelarObsArquivar');

    const obsInput = document.getElementById('obsArquivamento');

    const abrirMini = (m) => m.classList.add('show');
    const fecharMini = (m) => m.classList.remove('show');

    if (btnArquivar && modalConfirm && modalObs) {
      btnArquivar.addEventListener('click', () => abrirMini(modalConfirm));

      btnNao?.addEventListener('click', () => fecharMini(modalConfirm));

      btnSim?.addEventListener('click', () => {
        fecharMini(modalConfirm);
        if (obsInput) obsInput.value = '';
        abrirMini(modalObs);
      });

      btnCancelarObs?.addEventListener('click', () => fecharMini(modalObs));

      btnSalvar?.addEventListener('click', async () => {
        const id = document.getElementById('id')?.value;
        const observacao = obsInput?.value?.trim();

        if (!id) return alert('ID inválido.');
        if (!observacao) return alert('Observação é obrigatória.');

        const res = await apiFetch(`/reeducandos/${id}/arquivar`, {
          method: 'POST',
          credentials: 'same-origin',
          headers: { 'Content-Type': 'application/json', ...csrfHeader() },
          body: JSON.stringify({ observacao })
        });

        if (res.ok) {
          fecharMini(modalObs);
          window.location.reload();
        } else {
          alert(await res.text());
        }
      });
    }


});


