function abrirModal(id) {
    fetch('/reeducandos/' + id)
        .then(res => res.json())
        .then(data => {
            document.getElementById('id').value = data.id;
            document.getElementById('nome').value = data.nome;
            document.getElementById('cpf').value = data.cpf;
            document.getElementById('telefone').value = data.telefone;
            document.getElementById('dia').value = data.dia || '';
            document.getElementById('modal').classList.add('show');
        });
}

function fecharModal() {
    document.getElementById('modal').classList.remove('show');
}

function confirmarExclusao() {
    if (confirm("Tem certeza que deseja excluir?")) {
        const id = document.getElementById("id").value;
        fetch('/reeducandos/' + id, { method: 'DELETE' })
            .then(() => window.location.reload());
    }
}

document.getElementById('formReeducando').addEventListener('submit', function (e) {
    e.preventDefault();
    const id = document.getElementById("id").value;
    const payload = {
        telefone: document.getElementById("telefone").value,
        dia: document.getElementById("dia").value
    };

    fetch('/reeducandos/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(() => {
        fecharModal();
        window.location.reload();
    });
});