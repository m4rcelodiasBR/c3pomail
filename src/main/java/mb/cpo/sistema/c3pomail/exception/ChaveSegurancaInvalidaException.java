package mb.cpo.sistema.c3pomail.exception;

/**
 * Exceção lançada quando a chave de API (X-API-KEY) está ausente,
 * em branco ou não corresponde à chave configurada no servidor.
 */
public class ChaveSegurancaInvalidaException extends RuntimeException {

    public ChaveSegurancaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
