package mb.cpo.sistema.c3pomail.exception;

/**
 * Exceção lançada quando o formato do endereço de e-mail fornecido
 * não passa na validação Regex RFC 5322 simplificada.
 */
public class EmailInvalidoException extends RuntimeException {

    public EmailInvalidoException(String mensagem) {
        super(mensagem);
    }
}
