package mb.cpo.sistema.c3pomail.dto;

/**
 * DTO de resposta padronizado para todas as operações da API.
 * Garante consistência no formato JSON retornado ao sistema cliente.
 *
 * @param codigoHttp código HTTP da resposta (200, 400, 401, 429, 500)
 * @param status     status textual da operação ("ENVIADO" ou "FALHA")
 * @param protocolo  UUID de rastreamento gerado para cada operação
 * @param mensagem   mensagem descritiva do resultado ou instrução de correção
 */
public record RespostaEmailDTO(
        int codigoHttp,
        String status,
        String protocolo,
        String mensagem
) {
    /** Status de sucesso. */
    public static final String STATUS_ENVIADO = "ENVIADO";

    /** Status de falha. */
    public static final String STATUS_FALHA = "FALHA";

    /**
     * Cria uma resposta de sucesso com protocolo e mensagem descritiva.
     */
    public static RespostaEmailDTO sucesso(String protocolo, String mensagem) {
        return new RespostaEmailDTO(200, STATUS_ENVIADO, protocolo, mensagem);
    }

    /**
     * Cria uma resposta de falha com código HTTP, protocolo e mensagem descritiva.
     */
    public static RespostaEmailDTO falha(int codigoHttp, String protocolo, String mensagem) {
        return new RespostaEmailDTO(codigoHttp, STATUS_FALHA, protocolo, mensagem);
    }
}
