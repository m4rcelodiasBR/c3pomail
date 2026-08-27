package mb.cpo.sistema.c3pomail.exception;

/**
 * Exceção lançada quando o limite de requisições por minuto (rate limit)
 * configurado via Bucket4j é excedido pelo sistema cliente.
 */
public class LimiteRequisicaoExcedidoException extends RuntimeException {

    public LimiteRequisicaoExcedidoException(String mensagem) {
        super(mensagem);
    }
}
