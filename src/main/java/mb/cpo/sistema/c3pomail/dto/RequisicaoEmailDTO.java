package mb.cpo.sistema.c3pomail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO de entrada para requisição de envio de e-mail.
 * Utiliza Bean Validation para garantir integridade antes de chegar ao serviço.
 *
 * @param destinatarios lista de e-mails destinatários (mínimo 1)
 * @param assunto       assunto do e-mail (obrigatório)
 * @param corpo         corpo do e-mail em HTML (será sanitizado pelo OWASP)
 * @param copias        lista de e-mails em cópia (opcional)
 */
public record RequisicaoEmailDTO(

        @NotEmpty(message = "A lista de destinatários não pode estar vazia.")
        List<@NotBlank(message = "O endereço de e-mail do destinatário não pode estar em branco.")
             @Email(message = "Formato de e-mail do destinatário é inválido.")
             String> destinatarios,

        @NotBlank(message = "O assunto do e-mail é obrigatório.")
        String assunto,

        @NotBlank(message = "O corpo do e-mail é obrigatório.")
        String corpo,

        List<@Email(message = "Formato de e-mail de cópia é inválido.")
             String> copias

) {
    /**
     * Construtor compacto que inicializa a lista de cópias como vazia
     * caso o cliente não envie o campo, evitando NullPointerException.
     */
    public RequisicaoEmailDTO {
        if (copias == null) {
            copias = List.of();
        }
    }
}
