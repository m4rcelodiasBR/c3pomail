package mb.cpo.sistema.c3pomail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import mb.cpo.sistema.c3pomail.dto.RequisicaoEmailDTO;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import mb.cpo.sistema.c3pomail.util.ValidadorEntrada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Serviço responsável pelo envio de e-mails via SMTP.
 *
 * <p>Gera um UUID de protocolo para cada operação (sucesso ou falha),
 * registra informações de auditoria no MDC do Logback e utiliza
 * remetente fixo injetado via variável de ambiente.</p>
 *
 * <p><strong>IMPORTANTE:</strong> O corpo do e-mail NUNCA é registrado nos logs
 * para evitar vazamento de dados sensíveis.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final SanitizadorHtmlService sanitizadorHtmlService;
    private final ValidadorEntrada validadorEntrada;

    @Value("${api.email.remetente}")
    private String remetenteFixo;

    public EmailService(JavaMailSender javaMailSender,
                        SanitizadorHtmlService sanitizadorHtmlService,
                        ValidadorEntrada validadorEntrada) {
        this.javaMailSender = javaMailSender;
        this.sanitizadorHtmlService = sanitizadorHtmlService;
        this.validadorEntrada = validadorEntrada;
    }

    /**
     * Envia um e-mail com base na requisição recebida.
     *
     * @param requisicao os dados do e-mail (destinatários, assunto, corpo, cópias)
     * @param ipOrigem   o IP real do cliente (extraído de X-Forwarded-For)
     * @return resposta contendo o protocolo UUID e o status da operação
     */
    public RespostaEmailDTO enviarEmail(RequisicaoEmailDTO requisicao, String ipOrigem) {
        var protocolo = UUID.randomUUID().toString();
        var destinatariosTexto = String.join(", ", requisicao.destinatarios());

        // Registra informações de auditoria no MDC para o Logback
        configurarMdc(protocolo, destinatariosTexto, requisicao.assunto(), ipOrigem);

        try {
            // Validação adicional via Regex (camada extra além do Bean Validation)
            validadorEntrada.validarListaEmails(requisicao.destinatarios());
            validadorEntrada.validarListaEmails(requisicao.copias());
            validadorEntrada.validarAssunto(requisicao.assunto());
            validadorEntrada.validarCorpo(requisicao.corpo());

            // Sanitiza o corpo HTML contra XSS
            var corpoSanitizado = sanitizadorHtmlService.sanitizar(requisicao.corpo());

            // Monta e envia o e-mail
            var mensagemMime = javaMailSender.createMimeMessage();
            var auxiliarMensagem = new MimeMessageHelper(mensagemMime, false, "UTF-8");

            auxiliarMensagem.setFrom(new InternetAddress(remetenteFixo, "C3PO Mail", "UTF-8"));
            auxiliarMensagem.setTo(requisicao.destinatarios().toArray(String[]::new));
            auxiliarMensagem.setSubject(requisicao.assunto());
            auxiliarMensagem.setText(corpoSanitizado, true); // true = conteúdo HTML

            // Adiciona cópias (CC) se presentes
            if (!requisicao.copias().isEmpty()) {
                auxiliarMensagem.setCc(requisicao.copias().toArray(String[]::new));
            }

            javaMailSender.send(mensagemMime);

            log.info("E-mail enviado com sucesso.");
            return RespostaEmailDTO.sucesso(protocolo, "E-mail enviado com sucesso para " + destinatariosTexto + ".");

        } catch (MessagingException | UnsupportedEncodingException | org.springframework.mail.MailException excecao) {
            log.error("Falha no envio do e-mail via SMTP: {}", excecao.getMessage(), excecao);
            return RespostaEmailDTO.falha(
                    500,
                    protocolo,
                    "Falha no envio do e-mail. Verifique a conexão SMTP e tente novamente. Protocolo: " + protocolo
            );
        } finally {
            limparMdc();
        }
    }

    /**
     * Configura as variáveis de contexto do MDC para rastreabilidade nos logs.
     * IMPORTANTE: O corpo do e-mail NÃO é inserido no MDC.
     */
    private void configurarMdc(String protocolo, String destinatario, String assunto, String ipOrigem) {
        MDC.put("protocolo", protocolo);
        MDC.put("destinatario", validadorEntrada.sanitizarParaLog(destinatario));
        MDC.put("assunto", validadorEntrada.sanitizarParaLog(assunto));
        MDC.put("ipOrigem", ipOrigem);
    }

    /**
     * Remove as variáveis de contexto do MDC após o processamento
     * para evitar vazamento entre requisições em Virtual Threads.
     */
    private void limparMdc() {
        MDC.remove("protocolo");
        MDC.remove("destinatario");
        MDC.remove("assunto");
        MDC.remove("ipOrigem");
    }
}
