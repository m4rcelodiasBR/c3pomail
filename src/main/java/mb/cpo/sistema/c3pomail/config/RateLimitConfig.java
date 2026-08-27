package mb.cpo.sistema.c3pomail.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuração do Bucket4j para rate limiting in-memory.
 * Cria um bucket global que limita o número de requisições por minuto.
 *
 * <p>O limite é configurável via {@code api.ratelimit.requisicoes-por-minuto}
 * no {@code application.properties}.</p>
 */
@Configuration
public class RateLimitConfig {

    @Value("${api.ratelimit.requisicoes-por-minuto:30}")
    private int requisicoesPorMinuto;

    /**
     * Cria o bucket global de rate limiting.
     * Capacidade = requisicoesPorMinuto, recarga completa a cada 1 minuto.
     *
     * @return o bucket configurado
     */
    @Bean
    public Bucket bucketRateLimit() {
        var limite = Bandwidth.builder()
                .capacity(requisicoesPorMinuto)
                .refillGreedy(requisicoesPorMinuto, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }
}
