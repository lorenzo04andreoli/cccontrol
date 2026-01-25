(function () {
  const input = document.getElementById("inputFoto");
  const img = document.getElementById("imgPerfil");
  const msg = document.getElementById("uploadMsg");

  const btnEscolher = document.getElementById("btnEscolherFoto");
  const avatarClick = document.getElementById("avatarClick");

  if (!input || !img) return;

  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

  const openPicker = () => input.click();

  btnEscolher?.addEventListener("click", (e) => {
    e.preventDefault();
    openPicker();
  });

  avatarClick?.addEventListener("click", (e) => {

    if (e.target?.closest?.("#btnEscolherFoto")) return;
    openPicker();
  });

  avatarClick?.addEventListener("keydown", (e) => {
    if (e.key === "Enter" || e.key === " ") openPicker();
  });

  input.addEventListener("change", async () => {
    msg.textContent = "";

    const file = input.files?.[0];
    if (!file) return;

    const okTypes = ["image/png", "image/jpeg", "image/webp"];
    if (!okTypes.includes(file.type)) {
      msg.textContent = "Formato inválido. Use PNG, JPG ou WEBP.";
      input.value = "";
      return;
    }

    msg.textContent = "Enviando foto...";

    const fd = new FormData();
    fd.append("foto", file);

    try {
      const res = await fetch("/usuario/foto", {
        method: "POST",
        body: fd,
        headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}
      });

      if (!res.ok) {
        const t = await res.text();
        msg.textContent = "Erro ao enviar: " + t;
        return;
      }

      const data = await res.json();
      if (data.fotoUrl) {

        const sep = data.fotoUrl.includes("?") ? "&" : "?";
        img.src = data.fotoUrl + sep + "v=" + Date.now();
      }

      msg.textContent = "Foto atualizada com sucesso.";
      input.value = "";
    } catch (err) {
      msg.textContent = "Falha ao enviar. Tente novamente.";
    }
  });
})();
