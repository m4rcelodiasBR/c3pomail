package mb.cpo.sistema.c3pomail.util;

import mb.cpo.sistema.c3pomail.exception.EmailInvalidoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitário para validação e sanitização de entradas.
 * Previne injeção de dados maliciosos (OWASP A03:2021-Injection).
 *
 * <p>A validação de e-mail utiliza um padrão Regex baseado na RFC 5322 simplificada.
 * A sanitização de strings remove caracteres de controle e sequências perigosas.</p>
 */
@Component
public class ValidadorEntrada {

    /**
     * Regex RFC 5322 simplificado para validação de e-mail.
     * Aceita: usuario@dominio.com, usuario.nome@sub.dominio.com.br
     * Rejeita: endereços sem @, sem domínio, com espaços, com caracteres especiais perigosos.
     */
    private static final Pattern PADRAO_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    /** Tamanho máximo permitido para o campo assunto. */
    private static final int TAMANHO_MAXIMO_ASSUNTO = 255;

    /** Tamanho máximo permitido para o corpo do e-mail. */
    private static final int TAMANHO_MAXIMO_CORPO = 50_000;

    /**
     * Valida o formato de um endereço de e-mail contra a Regex RFC 5322 simplificada.
     *
     * @param email o endereço de e-mail a validar
     * @throws EmailInvalidoException se o formato for inválido
     */
    public void validarFormatoEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new EmailInvalidoException(
                    "O endereço de e-mail não pode estar vazio. Informe um e-mail válido (ex: usuario@dominio.com)."
            );
        }

        if (!PADRAO_EMAIL.matcher(email.trim()).matches()) {
            throw new EmailInvalidoException(
                    "O endereço de e-mail '" + sanitizarParaLog(email) + "' possui formato inválido. "
                            + "Use o formato: usuario@dominio.com"
            );
        }
    }

    /**
     * Valida uma lista de endereços de e-mail.
     *
     * @param emails a lista de endereços a validar
     * @throws EmailInvalidoException se qualquer endereço for inválido
     */
    public void validarListaEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }
        for (var email : emails) {
            validarFormatoEmail(email);
        }
    }

    /**
     * Valida o tamanho do assunto do e-mail.
     *
     * @param assunto o assunto a validar
     * @throws IllegalArgumentException se exceder o tamanho máximo
     */
    public void validarAssunto(String assunto) {
        if (assunto != null && assunto.length() > TAMANHO_MAXIMO_ASSUNTO) {
            throw new IllegalArgumentException(
                    "O assunto excede o tamanho máximo permitido de " + TAMANHO_MAXIMO_ASSUNTO + " caracteres."
            );
        }
    }

    /**
     * Valida o tamanho do corpo do e-mail.
     *
     * @param corpo o corpo a validar
     * @throws IllegalArgumentException se exceder o tamanho máximo
     */
    public void validarCorpo(String corpo) {
        if (corpo != null && corpo.length() > TAMANHO_MAXIMO_CORPO) {
            throw new IllegalArgumentException(
                    "O corpo do e-mail excede o tamanho máximo permitido de " + TAMANHO_MAXIMO_CORPO + " caracteres."
            );
        }
    }

    /**
     * Sanitiza uma string para uso seguro em logs.
     * Remove caracteres de controle e limita o tamanho para evitar
     * injeção de log (CWE-117) e consumo excessivo de disco.
     *
     * @param entrada a string a sanitizar
     * @return a string sanitizada e segura para log
     */
    public String sanitizarParaLog(String entrada) {
        if (entrada == null) {
            return "N/A";
        }
        // Remove caracteres de controle (exceto espaço) e quebras de linha
        var sanitizado = entrada.replaceAll("[\\p{Cntrl}&&[^ ]]", "").replaceAll("[\\r\\n]", " ");
        // Limita a 200 caracteres para não inflar os logs
        if (sanitizado.length() > 200) {
            sanitizado = sanitizado.substring(0, 200) + "...(truncado)";
        }
        return sanitizado;
    }
}
