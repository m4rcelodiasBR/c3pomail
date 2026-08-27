package mb.cpo.sistema.c3pomail.controller;

import tools.jackson.databind.ObjectMapper;
import mb.cpo.sistema.c3pomail.dto.RequisicaoEmailDTO;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import mb.cpo.sistema.c3pomail.service.EmailService;
import mb.cpo.sistema.c3pomail.util.ExtratorIpOrigem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para {@link EmailController}.
 * Utiliza MockMvc para simular requisições HTTP completas
 * passando pelo SecurityFilterChain.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ExtratorIpOrigem extratorIpOrigem;

    private static final String URL_API = "/api/c3pomail/mail";
    private static final String CHAVE_TESTE = "chave-teste-segura-256-bits";

    @Test
    @DisplayName("Deve retornar 200 ao enviar e-mail válido com API Key correta")
    void deveRetornar200ComEnvioValido() throws Exception {
        var requisicao = new RequisicaoEmailDTO(
                List.of("teste@dominio.com"),
                "Assunto de Teste",
                "<p>Corpo do e-mail</p>",
                List.of()
        );

        var respostaEsperada = RespostaEmailDTO.sucesso(
                "uuid-teste-123",
                "E-mail enviado com sucesso."
        );

        when(extratorIpOrigem.extrairIp(any())).thenReturn("192.168.1.1");
        when(emailService.enviarEmail(any(), anyString())).thenReturn(respostaEsperada);

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", CHAVE_TESTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"))
                .andExpect(jsonPath("$.protocolo").value("uuid-teste-123"));
    }

    @Test
    @DisplayName("Deve retornar 401 quando API Key está ausente")
    void deveRetornar401SemApiKey() throws Exception {
        var requisicao = new RequisicaoEmailDTO(
                List.of("teste@dominio.com"),
                "Assunto",
                "Corpo",
                List.of()
        );

        mockMvc.perform(post(URL_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FALHA"));
    }

    @Test
    @DisplayName("Deve retornar 401 quando API Key é inválida")
    void deveRetornar401ComApiKeyInvalida() throws Exception {
        var requisicao = new RequisicaoEmailDTO(
                List.of("teste@dominio.com"),
                "Assunto",
                "Corpo",
                List.of()
        );

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", "chave-errada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 quando destinatários estão vazios")
    void deveRetornar400ComDestinatariosVazios() throws Exception {
        var payloadInvalido = """
                {
                    "destinatarios": [],
                    "assunto": "Assunto",
                    "corpo": "Corpo"
                }
                """;

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", CHAVE_TESTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando assunto está em branco")
    void deveRetornar400ComAssuntoVazio() throws Exception {
        var payloadInvalido = """
                {
                    "destinatarios": ["teste@dominio.com"],
                    "assunto": "",
                    "corpo": "Corpo"
                }
                """;

        mockMvc.perform(post(URL_API)
                        .header("X-API-KEY", CHAVE_TESTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest());
    }
}
