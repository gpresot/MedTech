/* ==========================================================
   Funções compartilhadas pelas páginas de gestão do MedTech.
   Todo texto vindo da API entra na tela com textContent
   (nunca innerHTML), para não executar HTML de terceiros.
========================================================== */

/**
 * Chama a API e devolve o JSON (ou null em respostas 204).
 * Em caso de erro lança Error com a mensagem do servidor.
 */
async function api(url, opcoes = {}) {
    const config = { method: opcoes.method || "GET", headers: {} };

    if (opcoes.body !== undefined) {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(opcoes.body);
    }

    let resposta;

    try {
        resposta = await fetch(url, config);
    } catch (erro) {
        throw new Error("Erro ao conectar com o servidor.");
    }

    // Sessão expirada: o servidor redireciona para a tela de login.
    if (resposta.redirected && resposta.url.includes("/login")) {
        window.location.href = "/login";
        throw new Error("Sessão expirada. Faça login novamente.");
    }

    if (resposta.status === 403) {
        throw new Error("Você não tem permissão para esta ação.");
    }

    if (!resposta.ok) {
        throw new Error(await lerErro(resposta));
    }

    if (resposta.status === 204) {
        return null;
    }

    return resposta.json();
}

async function lerErro(resposta) {
    const texto = await resposta.text();

    try {
        const json = JSON.parse(texto);

        // Erros de validação: { campo: "mensagem" }
        if (json && typeof json === "object") {
            return Object.values(json).join(" ");
        }
    } catch (e) {
        // Não é JSON: usa o texto puro.
    }

    return texto || "Não foi possível concluir a operação.";
}

/** Cria um elemento: el("td", { class: "x", onclick: fn }, "texto", outroNo). */
function el(tag, props = {}, ...filhos) {
    const no = document.createElement(tag);

    for (const [chave, valor] of Object.entries(props || {})) {
        if (valor === null || valor === undefined || valor === false) {
            continue;
        }

        if (chave === "class") {
            no.className = valor;
        } else if (chave.startsWith("on")) {
            no.addEventListener(chave.slice(2), valor);
        } else {
            no.setAttribute(chave, valor);
        }
    }

    adicionar(no, filhos);
    return no;
}

function adicionar(no, filhos) {
    for (const filho of filhos.flat()) {
        if (filho === null || filho === undefined || filho === false) {
            continue;
        }

        no.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
    }
}

/** Monta uma tabela a partir de cabeçalhos e linhas (arrays de células). */
function tabela(cabecalhos, linhas) {
    return el("div", { class: "tabela-container" },
        el("table", {},
            el("thead", {}, el("tr", {}, ...cabecalhos.map(c => el("th", {}, c)))),
            el("tbody", {}, ...linhas.map(l => el("tr", {}, ...l.map(c => el("td", {}, c)))))
        )
    );
}

/** Troca o conteúdo de um elemento por uma tabela ou pela mensagem de lista vazia. */
function desenharLista(alvo, vazio, cabecalhos, linhas) {
    alvo.replaceChildren(
        linhas.length ? tabela(cabecalhos, linhas) : el("p", { class: "vazio" }, vazio)
    );
}

/** Mostra uma mensagem de sucesso ou erro em um elemento .mensagem. */
function mostrarMensagem(alvo, texto, tipo) {
    alvo.textContent = texto;
    alvo.className = "mensagem" + (tipo ? " " + tipo : "");
}

/* ---------- FORMATAÇÃO ---------- */

function formatarData(iso) {
    return iso ? iso.split("-").reverse().join("/") : "—";
}

function formatarHora(hora) {
    return hora ? hora.slice(0, 5) : "—";
}

function formatarDataHora(iso) {
    if (!iso) {
        return "—";
    }

    const [data, hora] = iso.split("T");
    return formatarData(data) + " " + formatarHora(hora);
}

function formatarCpf(cpf) {
    return cpf && cpf.length === 11
        ? `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`
        : (cpf || "—");
}

const ROTULOS = {
    // Status de atendimento (consulta, emergência e procedimento cirúrgico)
    AGENDADO: "Agendado",
    CONFIRMADO: "Confirmado",
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDO: "Concluído",
    CANCELADO: "Cancelado",
    NAO_COMPARECEU: "Não compareceu",
    AGENDADA: "Agendada",
    CONFIRMADA: "Confirmada",
    EM_ATENDIMENTO: "Em atendimento",
    CONCLUIDA: "Concluída",
    CANCELADA: "Cancelada",
    // Tipo de cirurgia
    ELETIVA: "Eletiva",
    URGENCIA: "Urgência",
    EMERGENCIA: "Emergência",
    // Tipo de quarto
    ENFERMARIA: "Enfermaria",
    APARTAMENTO: "Apartamento",
    UTI: "UTI",
    // Quarto (situação)
    DISPONIVEL: "Disponível",
    OCUPADO: "Lotado",
    MANUTENCAO: "Em manutenção"
};

/** EM_ANDAMENTO -> "Em andamento" (usa o dicionário; senão formata o texto) */
function rotulo(enumValor) {
    if (!enumValor) {
        return "—";
    }

    if (ROTULOS[enumValor]) {
        return ROTULOS[enumValor];
    }

    const texto = enumValor.toLowerCase().replaceAll("_", " ");
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** Selo colorido de situação. */
function selo(texto, tipo) {
    return el("span", { class: "status" + (tipo ? " status-" + tipo : "") }, texto);
}

function soNumeros(valor) {
    return valor.replace(/\D/g, "");
}

function hoje() {
    const d = new Date();
    const mes = String(d.getMonth() + 1).padStart(2, "0");
    const dia = String(d.getDate()).padStart(2, "0");
    return `${d.getFullYear()}-${mes}-${dia}`;
}

/* ---------- PAINEL DE AÇÃO (pede uma data ou um texto) ---------- */

/**
 * Usa a <section id="painelAcao"> da página. Opções:
 * { titulo, rotulo, tipo: "date" | "textarea", valor, confirmar, onConfirmar(valor) }
 */
function abrirPainel(opcoes) {
    const painel = document.getElementById("painelAcao");
    const msg = el("p", { class: "mensagem" });
    const campo = opcoes.tipo === "textarea"
        ? el("textarea", { id: "painelCampo", required: "required" })
        : el("input", { id: "painelCampo", type: "date", required: "required" });

    campo.value = opcoes.valor || "";

    const confirmar = el("button", { type: "button", class: "botao" }, opcoes.confirmar || "Confirmar");
    const cancelar = el("button", { type: "button", class: "botao botao-secundario" }, "Cancelar");

    cancelar.addEventListener("click", () => painel.classList.add("oculto"));

    confirmar.addEventListener("click", async () => {
        if (!campo.value.trim()) {
            mostrarMensagem(msg, "Preencha o campo.", "erro");
            return;
        }

        try {
            await opcoes.onConfirmar(campo.value.trim());
            painel.classList.add("oculto");
        } catch (erro) {
            mostrarMensagem(msg, erro.message, "erro");
        }
    });

    painel.replaceChildren(
        el("h2", {}, opcoes.titulo),
        el("div", { class: "campo" }, el("label", { for: "painelCampo" }, opcoes.rotulo), campo),
        el("div", { class: "campo-acoes", style: "margin-top:16px" }, confirmar, cancelar),
        msg
    );

    painel.classList.remove("oculto");
    painel.scrollIntoView({ behavior: "smooth" });
    campo.focus();
}
