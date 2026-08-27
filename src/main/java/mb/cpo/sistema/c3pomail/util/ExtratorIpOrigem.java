package mb.cpo.sistema.c3pomail.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utilitário para extrair o endereço IP real do cliente.
 * Considera que a API opera atrás de um proxy reverso Apache
 * que injeta o cabeçalho {@code X-Forwarded-For}.
 *
 * <p>O primeiro IP da lista em {@code X-Forwarded-For} é o IP original do cliente.
 * Se o cabeçalho não existir, utiliza {@code request.getRemoteAddr()} como fallback.</p>
 */
@Component
public class ExtratorIpOrigem {

    private static final String CABECALHO_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Extrai o IP de origem da requisição HTTP.
     *
     * @param requisicao a requisição HTTP recebida
     * @return o endereço IP real do cliente
     */
    public String extrairIp(HttpServletRequest requisicao) {
        var valorCabecalho = requisicao.getHeader(CABECALHO_X_FORWARDED_FOR);

        if (valorCabecalho != null && !valorCabecalho.isBlank()) {
            // X-Forwarded-For pode conter múltiplos IPs separados por vírgula.
            // O primeiro é o IP original do cliente.
            var primeiroIp = valorCabecalho.split(",")[0].trim();
            if (!primeiroIp.isEmpty()) {
                return primeiroIp;
            }
        }

        return requisicao.getRemoteAddr();
    }
}
