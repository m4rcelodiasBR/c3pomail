package mb.cpo.sistema.c3pomail.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para {@link RateLimitFilter}.
 * Verifica que o rate limiting bloqueia requisições excedentes.
 */
@SpringBootTest(properties = {
        "api.ratelimit.requisicoes-por-minuto=3"
})
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String URL_API = "/api/c3pomail/mail";
    private static final String CHAVE_TESTE = "chave-teste-segura-256-bits";

    @Test
    @DisplayName("Deve retornar 429 após exceder o limite de requisições por minuto")
    void deveRetornar429AposExcederLimite() throws Exception {
        var payload = """
                {
                    "destinatarios": ["teste@dominio.com"],
                    "assunto": "Teste Rate Limit",
                    "corpo": "Corpo"
                }
                """;

        // Envia 3 requisições (dentro do limite definido no @SpringBootTest)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post(URL_API)
                    .header("X-API-KEY", CHAVE_TESTE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload));
        }

        // A 4ª requisição deve ser bloqueada com 429
        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", CHAVE_TESTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("FALHA"))
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
