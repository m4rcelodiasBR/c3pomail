# 📖 Documentação da API - C3PO Mail

O **C3PO Mail** é um microsserviço centralizado para envio de e-mails, desenvolvido para abstrair a complexidade de comunicação com servidores SMTP. Através de uma API REST simples, rápida e segura, qualquer sistema da infraestrutura pode disparar e-mails sem precisar lidar com configurações complexas de bibliotecas de e-mail.

---

## 🔒 1. Autenticação

Para garantir a segurança, todos os envios devem ser autenticados via **API Key**. 
A chave deve ser enviada no cabeçalho HTTP da requisição (Headers).

*   **Header Name:** X-API-KEY
*   **Header Value:** <SUA_CHAVE_FORNECIDA>

> **Aviso de Segurança (OWASP):** Nunca exponha sua API Key no código fonte do frontend. Requisições para esta API devem partir exclusivamente do backend da sua aplicação.

---

## 🚀 2. Endpoint de Envio

Para disparar um e-mail, envie uma requisição **POST** com os dados no formato JSON.

*   **URL:** https://email.cpo.mb/api/c3pomail/mail *(ou IP/Domínio do servidor)*
*   **Método:** POST
*   **Content-Type:** pplication/json

### Estrutura do Payload (JSON)

| Campo | Tipo | Obrigatório | Descrição |
| :--- | :---: | :---: | :--- |
| destinatarios | Array de Strings | Sim | Lista de e-mails que receberão a mensagem. |
| ssunto | String | Sim | O título (assunto) do e-mail. |
| corpo | String | Sim | O conteúdo da mensagem. Aceita formatação HTML (tags padrão). |
| copias | Array de Strings | Não | Lista de e-mails que receberão em Cópia Oculta (BCC). |

### Exemplo de Corpo da Requisição (Body)
\\\json
{
  "destinatarios": ["joao@exemplo.com", "maria@exemplo.com"],
  "copias": ["auditoria@empresa.com"],
  "assunto": "Relatório Mensal Finalizado",
  "corpo": "<h1>Olá!</h1><p>O relatório <b>Mensal</b> já está disponível.</p>"
}
\\\

---

## 📥 3. Respostas da API

A API foi projetada para retornar respostas precisas utilizando os padrões de **HTTP Status Codes**.

### ✅ Sucesso (HTTP 200 OK)
Retornado quando a mensagem é aceita pelo servidor SMTP e enfileirada para envio.
\\\json
{
  "status": "ENVIADO",
  "protocolo": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "mensagem": "E-mail enviado com sucesso"
}
\\\

### ❌ Erro de Validação (HTTP 400 Bad Request)
Retornado quando algum campo obrigatório está faltando, vazio, ou quando um endereço de e-mail é inválido. A mensagem indica exatamente qual campo falhou.
\\\json
{
  "status": "ERRO",
  "protocolo": "c3f8e562-11a5-4f3b-8212-abcd1234ef56",
  "mensagem": "E-mail de destinatário inválido: joao.exemplo"
}
\\\

### 🚫 Não Autorizado (HTTP 401 Unauthorized)
Retornado quando o cabeçalho X-API-KEY está ausente, vazio ou incorreto.
\\\json
{
  "status": "ERRO",
  "protocolo": "d4a9b234-...",
  "mensagem": "Chave de autenticação ausente ou inválida."
}
\\\

### ⏳ Limite Excedido (HTTP 429 Too Many Requests)
Retornado quando o volume de envios utilizando a mesma API Key ultrapassa o limite permitido (Rate Limiting).
\\\json
{
  "status": "ERRO",
  "protocolo": "e5b1...",
  "mensagem": "Limite de requisições excedido. Tente novamente mais tarde."
}
\\\

### 🔧 Falha no SMTP (HTTP 503 Service Unavailable)
Retornado caso o servidor de e-mail corporativo interno (Zimbra/Exchange) caia ou rejeite a comunicação.
\\\json
{
  "status": "ERRO",
  "protocolo": "f6c2...",
  "mensagem": "Falha de comunicação com o servidor SMTP interno. Tente novamente mais tarde."
}
\\\

---

## 💻 4. Exemplos de Integração

### Exemplo cURL (Terminal / Bash)
\\\ash
curl -X POST https://email.cpo.mb/api/c3pomail/mail \
     -H "Content-Type: application/json" \
     -H "X-API-KEY: sua-chave-aqui-123" \
     -d '{
           "destinatarios": ["cliente@teste.com"],
           "assunto": "Aviso do Sistema",
           "corpo": "<p>Aviso importante do sistema!</p>"
         }'
\\\

### Exemplo PHP (cURL)
\\\php
<?php
\ = curl_init();
\ = json_encode([
    "destinatarios" => ["cliente@teste.com"],
    "assunto" => "Aviso do Sistema",
    "corpo" => "<p>Aviso importante do sistema!</p>"
]);

curl_setopt_array(\, [
    CURLOPT_URL => "https://email.cpo.mb/api/c3pomail/mail",
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_POST => true,
    CURLOPT_POSTFIELDS => \,
    CURLOPT_HTTPHEADER => [
        "Content-Type: application/json",
        "X-API-KEY: sua-chave-aqui-123"
    ],
]);

\ = curl_exec(\);
curl_close(\);
echo \;
?>
\\\

### Exemplo Java (11+ HttpClient)
\\\java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Disparador {
    public static void main(String[] args) throws Exception {
        String json = "{\"destinatarios\":[\"cliente@teste.com\"],\"assunto\":\"Aviso\",\"corpo\":\"<p>Texto</p>\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://email.cpo.mb/api/c3pomail/mail"))
            .header("Content-Type", "application/json")
            .header("X-API-KEY", "sua-chave-aqui-123")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
            
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Corpo: " + response.body());
    }
}
\\\

---

## ⚕️ 5. Monitoramento (Health Check)

Sistemas externos podem verificar a saúde e disponibilidade desta API batendo na rota de Actuator. Útil para Load Balancers e painéis de monitoramento (como Zabbix/Grafana).

*   **URL:** https://email.cpo.mb/actuator/health
*   **Método:** GET

**Resposta:**
\\\json
{
  "status": "UP"
}
\\\
*(Nota: Não requer API Key e o status "DOWN" indica falha na aplicação).*