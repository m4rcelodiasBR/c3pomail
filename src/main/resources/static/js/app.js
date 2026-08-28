const URL_API = '/api/c3pomail/mail';
const URL_HEALTH = '/actuator/health';
const INTERVALO_HEALTH_MS = 30000;

let quill;

document.addEventListener('DOMContentLoaded', () => {
    verificarStatusApi();
    setInterval(verificarStatusApi, INTERVALO_HEALTH_MS);
    document.getElementById('campoApiKey').focus();

    const toolbarOptions = [
        ['bold', 'italic', 'underline', 'strike'],        
        [{ 'color': [] }, { 'background': [] }],          
        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        ['clean']                                         
    ];

    quill = new Quill('#editor-container', {
        theme: 'snow',
        placeholder: 'Escreva a mensagem do e-mail aqui...',
        modules: {
            toolbar: toolbarOptions
        }
    });

    // Validacao em tempo real (on input/blur)
    configurarValidacaoTempoReal();
});

function configurarValidacaoTempoReal() {
    const validarListaEmails = (input, idErro) => {
        input.classList.remove('is-invalid');
        document.getElementById(idErro).style.display = 'none';
        
        const texto = input.value.trim();
        if (texto) {
            const emails = texto.split(',');
            for (let i = 0; i < emails.length; i++) {
                if (!validarEmail(emails[i])) {
                    exibirErro(input.id, idErro, 'E-mail inválido: ' + sanitizarTexto(emails[i]));
                    return;
                }
            }
        }
    };

    document.getElementById('campoDestinatarios').addEventListener('input', function() {
        validarListaEmails(this, 'erroDestinatarios');
    });

    document.getElementById('campoCopias').addEventListener('input', function() {
        validarListaEmails(this, 'erroCopias');
    });

    // Remove erro ao digitar para inputs normais
    const camposObrigatorios = ['campoApiKey', 'campoAssunto'];
    camposObrigatorios.forEach(id => {
        document.getElementById(id).addEventListener('input', function() {
            if (this.value.trim().length > 0) {
                this.classList.remove('is-invalid');
                document.getElementById('erro' + id.replace('campo', '')).style.display = 'none';
            }
        });
    });

    // Remove erro do quill ao digitar
    quill.on('text-change', function() {
        const corpoText = quill.getText().trim();
        if (corpoText.length > 0) {
            document.getElementById('wrapperCorpo').classList.remove('has-error');
            document.getElementById('erroCorpo').style.display = 'none';
        }
    });
}

function verificarStatusApi() {
    const textoStatus = document.getElementById('textoStatus');
    const statusContainer = document.getElementById('statusIndicator');

    fetch(URL_HEALTH)
        .then(res => res.json())
        .then(dados => {
            if (dados.status === 'UP') {
                statusContainer.classList.remove('status-offline');
                statusContainer.classList.add('status-online');
                textoStatus.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Servidor Online';
            } else {
                statusContainer.classList.remove('status-online');
                statusContainer.classList.add('status-offline');
                textoStatus.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>Falha no SMTP';
            }
        })
        .catch(() => {
            statusContainer.classList.remove('status-online');
            statusContainer.classList.add('status-offline');
            textoStatus.innerHTML = '<i class="bi bi-x-circle-fill me-2"></i>API Indisponível';
        });
}

function validarEmail(email) {
    const padrao = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
    return padrao.test(email.trim());
}

function sanitizarTexto(texto) {
    if (!texto) return '';
    const div = document.createElement('div');
    div.textContent = texto;
    return div.innerHTML;
}

function exibirErro(idCampo, idErro, mensagem) {
    const campo = document.getElementById(idCampo);
    if(campo) campo.classList.add('is-invalid');
    const erro = document.getElementById(idErro);
    if(erro) {
        erro.textContent = mensagem;
        erro.style.display = 'block';
    }
}

function exibirErroQuill(mensagem) {
    document.getElementById('wrapperCorpo').classList.add('has-error');
    const erro = document.getElementById('erroCorpo');
    erro.textContent = mensagem;
    erro.style.display = 'block';
}

function limparErros() {
    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.getElementById('wrapperCorpo').classList.remove('has-error');
    document.querySelectorAll('.invalid-feedback, #erroCorpo').forEach(el => el.style.display = '');
}

function validarFormulario() {
    let valido = true;
    limparErros();

    const apiKey = document.getElementById('campoApiKey').value.trim();
    if (!apiKey) {
        exibirErro('campoApiKey', 'erroApiKey', 'Informe a chave de autenticação.');
        valido = false;
    }

    const destTexto = document.getElementById('campoDestinatarios').value.trim();
    if (!destTexto) {
        exibirErro('campoDestinatarios', 'erroDestinatarios', 'Informe ao menos um destinatário.');
        valido = false;
    } else {
        destTexto.split(',').forEach(email => {
            if (!validarEmail(email)) {
                exibirErro('campoDestinatarios', 'erroDestinatarios', 'E-mail inválido: ' + sanitizarTexto(email));
                valido = false;
            }
        });
    }

    const assunto = document.getElementById('campoAssunto').value.trim();
    if (!assunto) {
        exibirErro('campoAssunto', 'erroAssunto', 'Informe o assunto.');
        valido = false;
    }

    const corpo = quill.root.innerHTML;
    const corpoText = quill.getText().trim();
    if (!corpoText && corpo === '<p><br></p>') {
        exibirErroQuill('Informe o corpo do e-mail.');
        valido = false;
    }

    const copiasTexto = document.getElementById('campoCopias').value.trim();
    if (copiasTexto) {
        copiasTexto.split(',').forEach(email => {
            if (!validarEmail(email)) {
                exibirErro('campoCopias', 'erroCopias', 'E-mail cópia inválido: ' + sanitizarTexto(email));
                valido = false;
            }
        });
    }

    return valido;
}

function enviarFormulario() {
    if (!validarFormulario()) return;

    const botao = document.getElementById('botaoEnviar');
    const loader = document.getElementById('loaderEnviar');
    const painelResposta = document.getElementById('painelResposta');
    const alertaResposta = document.getElementById('alertaResposta');
    const msgResposta = document.getElementById('msgResposta');
    const protocoloResposta = document.getElementById('protocoloResposta');

    botao.disabled = true;
    loader.classList.remove('d-none');
    painelResposta.classList.add('d-none');

    const payload = {
        destinatarios: document.getElementById('campoDestinatarios').value.trim().split(',').map(e => e.trim()),
        assunto: document.getElementById('campoAssunto').value.trim(),
        corpo: quill.root.innerHTML,
        copias: document.getElementById('campoCopias').value.trim() ? document.getElementById('campoCopias').value.trim().split(',').map(e => e.trim()) : []
    };

    fetch(URL_API, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-API-KEY': document.getElementById('campoApiKey').value.trim()
        },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(dados => {
        painelResposta.classList.remove('d-none');
        if (dados.status === 'ENVIADO') {
            alertaResposta.className = 'alert alert-success';
            msgResposta.innerHTML = '<strong><i class="bi bi-check-circle-fill me-2"></i> ' + sanitizarTexto(dados.mensagem) + '</strong>';
            
            // Limpa o form após sucesso
            document.getElementById('formularioEmail').reset();
            quill.root.innerHTML = '';
        } else {
            alertaResposta.className = 'alert alert-danger';
            msgResposta.innerHTML = '<strong><i class="bi bi-x-circle-fill me-2"></i> ' + sanitizarTexto(dados.mensagem) + '</strong>';
        }
        protocoloResposta.textContent = 'Protocolo: ' + dados.protocolo;
    })
    .catch(() => {
        painelResposta.classList.remove('d-none');
        alertaResposta.className = 'alert alert-danger';
        msgResposta.innerHTML = '<strong><i class="bi bi-wifi-off me-2"></i> Erro de conexão com a API.</strong>';
        protocoloResposta.textContent = '';
    })
    .finally(() => {
        botao.disabled = false;
        loader.classList.add('d-none');
    });
}