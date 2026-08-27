---
trigger: always_on
---

## IMPORTANTE - Limitações de Análises e Desenvolvimento em Diretório

- **Diretório do Projeto c3po-mail:** utilize apenas o diretório deste projeto para baixar bibliotecas e criar os arquivos. Não analise, veja ou altere diretórios vizinhos, apenas deste projeto e seus subdiretórios.
- **Projeto Independente:** este projeto não depende dos outros que estão neste mesmo diretório.

## 1. Stack Tecnológica e Arquitetura Base

- **Linguagem:** Java 21+ (Uso intensivo de `Records`, `var`, e _Pattern Matching_).
- **Alta Concorrência:** Obrigatório habilitar **Virtual Threads** (`spring.threads.virtual.enabled=true`) para otimizar chamadas de I/O de rede no disparo SMTP.
- **Framework:** Spring Boot 4.x ou superior (Tomcat embutido operando na porta 8080).
- **Build e Dependências:** Maven (`pom.xml`).
- **Bibliotecas Base:** Utilizar estritamente o namespace `jakarta.*`.
- **Infraestrutura Alvo:** Oracle Linux 9 protegido por proxy reverso Apache.
- **Deploy:** O artefato final será um `.jar` executável via serviço `systemd`. As credenciais (SMTP e API Key) NUNCA devem estar no código, mas injetadas via variáveis de ambiente (`EnvironmentFile`).
- **Reconhecimento de Proxy:** O projeto deve conter `server.forward-headers-strategy=native` no arquivo `application.properties` para garantir que o Spring Boot interprete corretamente os cabeçalhos de repasse do Apache.

## 2. Requisitos de Negócio e Segurança (Blindagem)

- **Tratamento Estrito de Rede (HTTPS OBRIGATÓRIO):** O sistema operará 100% sob tráfego criptografado (TLS 1.2 ou superior). O proxy reverso Apache deve bloquear requisições na porta 80 e o Spring Boot deve injetar o cabeçalho HSTS (`Strict-Transport-Security`) em todas as respostas. Nenhuma requisição HTTP não criptografada será aceita.
- **Remetente Fixo:** O remetente (`from`) é global e definido exclusivamente na API. A API deve ignorar ou rejeitar qualquer tentativa do sistema cliente de alterar o remetente.
- **Autenticação:** Exigência do cabeçalho HTTP `X-API-KEY`. A validação deve ser feita em tempo constante.
- **Tratamento de IP:** A API está atrás de um Apache. O IP real do cliente deve ser extraído do cabeçalho `X-Forwarded-For`. O fallback é `request.getRemoteAddr()`.

## 3. Auditoria e Rastreabilidade (Padrão Enterprise)

- **Protocolo Único:** Todo envio (sucesso ou falha) DEVE gerar um UUID (`String protocolo`).
- **Responsabilidade Compartilhada:** O Controlador deve retornar um JSON contendo o `status` e o `protocolo` gerado, para que o sistema cliente persista a prova do envio.
- **Log Físico e Rotativo:** Uso obrigatório de `logback-spring.xml` gravando arquivos diários (rotação de 90 dias) no diretório `/opt/api-email/logs/`.
- **Conteúdo do Log:** Deve registrar: Data/Hora, Nível, IP de Origem, UUID do Protocolo, E-mail Destinatário, Assunto e Status. É ESTRITAMENTE PROIBIDO gravar o corpo (body) do e-mail no log para evitar vazamento de dados sensíveis.
