package mb.cpo.sistema.c3pomail.filter;

import tools.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de rate limiting baseado no Bucket4j.
 * Intercepta requisições à API e consome 1 token por chamada.
 *
 * <p>Se o bucket estiver vazio (limite excedido), retorna HTTP 429
 * com uma resposta JSON padronizada informando o cliente para aguardar.</p>
 *
 * <p>Rotas excluídas: {@code /actuator/health}, {@code /} (página de teste),
 * recursos estáticos.</p>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final Bucket bucket;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(Bucket bucket, ObjectMapper objectMapper) {
        this.bucket = bucket;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeiaFiltros) throws ServletException, IOException {

        if (bucket.tryConsume(1)) {
            cadeiaFiltros.doFilter(requisicao, resposta);
        } else {
            log.warn("Rate limit excedido para IP: {}", requisicao.getRemoteAddr());

            resposta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
            resposta.setCharacterEncoding("UTF-8");

            var respostaErro = RespostaEmailDTO.falha(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    UUID.randomUUID().toString(),
                    "Limite de requisições excedido. Aguarde alguns segundos e tente novamente."
            );

            resposta.getWriter().write(objectMapper.writeValueAsString(respostaErro));
        }
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
}
