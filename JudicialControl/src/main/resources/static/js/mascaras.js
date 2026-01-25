function aplicarMascaraCPF(campo) {
    campo.value = campo.value
        .replace(/\D/g, '') // remove não dígitos
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
}

function aplicarMascaraTelefone(campo) {
    campo.value = campo.value
        .replace(/\D/g, '')
        .replace(/^(\d{2})(\d)/g, '($1) $2')
        .replace(/(\d{5})(\d{4})$/, '$1-$2');
}


document.getElementById("novoCpf").addEventListener("input", function () {
    aplicarMascaraCPF(this);
});

document.getElementById("novoTelefone").addEventListener("input", function () {
    aplicarMascaraTelefone(this);
});


document.getElementById("cpf").addEventListener("input", function () {
    aplicarMascaraCPF(this);
});

document.getElementById("telefone").addEventListener("input", function () {
    aplicarMascaraTelefone(this);
});