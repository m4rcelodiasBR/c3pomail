package mb.cpo.sistema.c3pomail.exception;

import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manipulador global de exceções da API.
 * Garante que toda resposta de erro siga o formato padronizado {@link RespostaEmailDTO}
 * com mensagens claras indicando o campo que falhou e como corrigir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata falhas de autenticação (API Key inválida ou ausente).
     * Retorna HTTP 401 Unauthorized.
     */
    @ExceptionHandler(ChaveSegurancaInvalidaException.class)
    public ResponseEntity<RespostaEmailDTO> tratarChaveInvalida(ChaveSegurancaInvalidaException excecao) {
        log.warn("Tentativa de acesso com chave de segurança inválida: {}", excecao.getMessage());
        var resposta = RespostaEmailDTO.falha(
                HttpStatus.UNAUTHORIZED.value(),
                UUID.randomUUID().toString(),
                excecao.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    /**
     * Trata e-mails com formato inválido detectados pela validação manual.
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(EmailInvalidoException.class)
    public ResponseEntity<RespostaEmailDTO> tratarEmailInvalido(EmailInvalidoException excecao) {
        log.warn("E-mail com formato inválido: {}", excecao.getMessage());
        var resposta = RespostaEmailDTO.falha(
                HttpStatus.BAD_REQUEST.value(),
                UUID.randomUUID().toString(),
                excecao.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Trata rate limit excedido (Bucket4j).
     * Retorna HTTP 429 Too Many Requests.
     */
    @ExceptionHandler(LimiteRequisicaoExcedidoException.class)
    public ResponseEntity<RespostaEmailDTO> tratarLimiteExcedido(LimiteRequisicaoExcedidoException excecao) {
        log.warn("Limite de requisições excedido: {}", excecao.getMessage());
        var resposta = RespostaEmailDTO.falha(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                UUID.randomUUID().toString(),
                excecao.getMessage()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resposta);
    }

    /**
     * Trata falhas de Bean Validation (@Valid) no payload da requisição.
     * Coleta todas as mensagens de validação e retorna em uma única resposta.
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaEmailDTO> tratarValidacao(MethodArgumentNotValidException excecao) {
        var mensagensDeErro = excecao.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Falha na validação do payload: {}", mensagensDeErro);

        var resposta = RespostaEmailDTO.falha(
                HttpStatus.BAD_REQUEST.value(),
                UUID.randomUUID().toString(),
                "Erro de validação: " + mensagensDeErro
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Trata qualquer exceção não prevista.
     * Registra o stack trace completo no log e retorna mensagem genérica ao cliente
     * para não expor detalhes internos do sistema.
     * Retorna HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaEmailDTO> tratarExcecaoGenerica(Exception excecao) {
        log.error("Erro interno não previsto na API: {}", excecao.getMessage(), excecao);
        var resposta = RespostaEmailDTO.falha(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                UUID.randomUUID().toString(),
                "Erro interno do servidor. Contate o administrador informando o protocolo retornado."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}
