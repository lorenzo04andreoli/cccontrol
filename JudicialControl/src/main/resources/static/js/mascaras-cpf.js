// máscara CPF
const cpf = document.getElementById('cpf');
if (cpf) {
  cpf.addEventListener('input', () => {
    let v = cpf.value.replace(/\D/g, '').slice(0, 11);
    v = v.replace(/^(\d{3})(\d)/, '$1.$2');
    v = v.replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3');
    v = v.replace(/\.(\d{3})(\d)/, '.$1-$2');
    cpf.value = v;
  });
}