(function () {
  const el = document.getElementById('relatorios-data');
  if (!el) return;

  const payload = JSON.parse(el.textContent);

  const resumoStatus = payload.resumoStatus || {};
  const porMes = payload.porMes || {};
  const tendencia = payload.tendencia || {};

  // ===== STATUS (Atrasados x Pendentes x Em dia) =====
  new Chart(document.getElementById('statusChart'), {
    type: 'doughnut',
    data: {
      labels: ['Atrasados', 'Pendentes', 'Em dia'],
      datasets: [{
        data: [
          resumoStatus.atrasados || 0,
          resumoStatus.pendentes || 0,
          resumoStatus.emDia || 0
        ],
        backgroundColor: [
          'rgba(239,68,68,0.85)',   // Atrasados (vermelho)
          'rgba(234,179,8,0.85)',   // Pendentes (amarelo)
          'rgba(34,197,94,0.85)'    // Em dia (verde)
        ],
        borderColor: 'rgba(255,255,255,0.9)',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom' } }
    }
  });

  new Chart(document.getElementById('cadastradosMesChart'), {
    type: 'bar',
    data: {
      labels: Object.keys(porMes),
      datasets: [{
        label: 'Comparecimentos',
        data: Object.values(porMes),
        backgroundColor: 'rgba(31,78,216,0.65)',
        borderRadius: 10
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } }
    }
  });



  // ===== TENDÊNCIA =====
  new Chart(document.getElementById('tendenciaAtrasosChart'), {
    type: 'line',
    data: {
      labels: Object.keys(tendencia),
      datasets: [{
        label: 'Atrasos',
        data: Object.values(tendencia),
        borderColor: 'rgba(27,107,74,0.95)',
        backgroundColor: 'rgba(27,107,74,0.10)',
        fill: true,
        tension: 0.35,
        pointRadius: 3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom' } }
    }
  });
})();
