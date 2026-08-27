package mb.cpo.sistema.c3pomail.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para {@link ApiKeyAuthenticationFilter}.
 * Verifica o comportamento do filtro de autenticação dentro do
 * SecurityFilterChain completo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String URL_API = "/api/c3pomail/mail";

    @Test
    @DisplayName("Deve retornar 401 quando cabeçalho X-API-KEY está ausente")
    void deveRetornar401SemCabecalhoApiKey() throws Exception {
        var payload = """
                {
                    "destinatarios": ["teste@dominio.com"],
                    "assunto": "Teste",
                    "corpo": "Corpo"
                }
                """;

        mockMvc.perform(post(URL_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FALHA"))
                .andExpect(jsonPath("$.mensagem").value("Cabeçalho X-API-KEY ausente. Informe a chave de autenticação."));
    }

    @Test
    @DisplayName("Deve retornar 401 quando cabeçalho X-API-KEY está em branco")
    void deveRetornar401ComApiKeyEmBranco() throws Exception {
        var payload = """
                {
                    "destinatarios": ["teste@dominio.com"],
                    "assunto": "Teste",
                    "corpo": "Corpo"
                }
                """;

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 quando API Key não corresponde à configurada")
    void deveRetornar401ComChaveErrada() throws Exception {
        var payload = """
                {
                    "destinatarios": ["teste@dominio.com"],
                    "assunto": "Teste",
                    "corpo": "Corpo"
                }
                """;

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", "chave-completamente-errada-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FALHA"));
    }

    @Test
    @DisplayName("Deve permitir acesso ao health check sem autenticação")
    void devePermitirHealthSemAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve permitir acesso à página de teste sem autenticação")
    void devePermitirPaginaTesteSemAutenticacao() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
