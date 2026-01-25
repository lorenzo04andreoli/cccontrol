document.addEventListener('DOMContentLoaded', () => {
  const sel = document.getElementById('tabelaSelect');
  if (!sel) return;

  sel.addEventListener('change', () => {
    const url = new URL(window.location.href);
    url.searchParams.set('tabela', sel.value);
    url.searchParams.set('page', '0'); // reset pagina
    window.location.href = url.toString();
  });
});
