
const botaoLer = document.getElementById("lerPagina");
const status = document.getElementById("status");

botaoLer.addEventListener("click", async () => {

    status.textContent = "Analisando página...";

    try {
        const [aba] = await chrome.tabs.query({
            active: true,
            currentWindow: true
        });

        const resultado = await chrome.scripting.executeScript({
            target: { tabId: aba.id },
            func: () => document.body.innerText
        });

        const conteudo = resultado[0].result;

        const resposta = await fetch(
            "http://localhost:8080/questoes/pagina",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    conteudo: conteudo
                })
            }
        );

        if (!resposta.ok) {
            throw new Error("Erro HTTP: " + resposta.status);
        }

        const mensagem = await resposta.text();

        status.textContent = mensagem;

    } catch (erro) {
        console.error(erro);
        status.textContent = "Erro: " + erro.message;
    }
});
