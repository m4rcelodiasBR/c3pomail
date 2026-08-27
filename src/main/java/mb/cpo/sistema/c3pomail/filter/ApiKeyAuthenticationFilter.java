package mb.cpo.sistema.c3pomail.filter;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Filtro de autenticação via cabeçalho HTTP {@code X-API-KEY}.
 *
 * <p>A comparação da chave é feita em <strong>tempo constante</strong>
 * utilizando {@link MessageDigest#isEqual(byte[], byte[])} para prevenir
 * ataques de Timing Attack (CWE-208).</p>
 *
 * <p>Rotas excluídas: {@code /actuator/health}, {@code /} (página de teste),
 * recursos estáticos.</p>
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String CABECALHO_API_KEY = "X-API-KEY";

    private final String chaveConfigurada;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(String chaveConfigurada, ObjectMapper objectMapper) {
        this.chaveConfigurada = chaveConfigurada;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeiaFiltros) throws ServletException, IOException {

        var chaveRecebida = requisicao.getHeader(CABECALHO_API_KEY);

        if (chaveRecebida == null || chaveRecebida.isBlank()) {
            log.warn("Requisição sem cabeçalho X-API-KEY recebida de IP: {}", requisicao.getRemoteAddr());
            responderNaoAutorizado(resposta, "Cabeçalho X-API-KEY ausente. Informe a chave de autenticação.");
            return;
        }

        // Comparação em tempo constante para prevenir Timing Attack
        var bytesChaveRecebida = chaveRecebida.getBytes(StandardCharsets.UTF_8);
        var bytesChaveConfigurada = chaveConfigurada.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(bytesChaveRecebida, bytesChaveConfigurada)) {
            log.warn("Tentativa de acesso com chave inválida de IP: {}", requisicao.getRemoteAddr());
            responderNaoAutorizado(resposta, "Chave de API inválida. Verifique o valor do cabeçalho X-API-KEY.");
            return;
        }

        // Informa ao Spring Security que a requisição está autenticada
        var autenticacao = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "cliente-api", null, java.util.Collections.emptyList());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(autenticacao);

        cadeiaFiltros.doFilter(requisicao, resposta);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        var caminho = requisicao.getRequestURI();
        return caminho.startsWith("/actuator")
                || caminho.equals("/")
                || caminho.startsWith("/lib/")
                || caminho.startsWith("/css/")
                || caminho.startsWith("/js/")
                || caminho.endsWith(".html")
                || caminho.endsWith(".css")
                || caminho.endsWith(".js")
                || caminho.endsWith(".ico");
    }

    /**
     * Escreve a resposta HTTP 401 diretamente no response com JSON padronizado.
     */
    private void responderNaoAutorizado(HttpServletResponse resposta, String mensagem) throws IOException {
        resposta.setStatus(HttpStatus.UNAUTHORIZED.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");

        var respostaErro = RespostaEmailDTO.falha(
                HttpStatus.UNAUTHORIZED.value(),
                UUID.randomUUID().toString(),
                mensagem
        );

        resposta.getWriter().write(objectMapper.writeValueAsString(respostaErro));
    }
}
