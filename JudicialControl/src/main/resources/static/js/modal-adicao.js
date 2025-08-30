function abrirModalAdicionar() {
    document.getElementById("modalAdicionar").classList.add("show");
}

function fecharModalAdicionar() {
    document.getElementById("modalAdicionar").classList.remove("show");
    document.getElementById("formAdicionarReeducando").reset();
}

document.getElementById('formAdicionarReeducando').addEventListener('submit', function(e) {
    e.preventDefault();

    const payload = {
        nome: document.getElementById("novoNome").value,
        cpf: document.getElementById("novoCpf").value,
        telefone: document.getElementById("novoTelefone").value,
        dia: document.getElementById("novoDia").value,
        frequencia: document.getElementById("novaFrequencia").value
    };

    fetch('/reeducandos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (response.ok) {
            alert("Reeducando adicionado com sucesso!");
            fecharModalAdicionar();
            window.location.reload();
        } else {
            return response.text().then(texto => {
                alert("Erro ao adicionar reeducando:\n" + texto);
            });
        }
    })
    .catch(error => {
        console.error("Erro inesperado:", error);
        alert("Erro inesperado: " + error.message);
    });

});