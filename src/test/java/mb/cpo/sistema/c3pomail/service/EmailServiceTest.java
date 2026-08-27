package mb.cpo.sistema.c3pomail.service;

import mb.cpo.sistema.c3pomail.dto.RequisicaoEmailDTO;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import mb.cpo.sistema.c3pomail.util.ValidadorEntrada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link EmailService}.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SanitizadorHtmlService sanitizadorHtmlService;

    @Mock
    private ValidadorEntrada validadorEntrada;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(emailService, "remetenteFixo", "naoresponda@teste.com");
    }

    @Test
    @DisplayName("Deve retornar sucesso ao enviar e-mail válido")
    void deveRetornarSucessoComEnvioValido() {
        var requisicao = new RequisicaoEmailDTO(
                List.of("destinatario@teste.com"),
                "Assunto de Teste",
                "<p>Corpo HTML</p>",
                List.of()
        );

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(sanitizadorHtmlService.sanitizar(anyString())).thenReturn("<p>Corpo HTML</p>");

        var resultado = emailService.enviarEmail(requisicao, "192.168.1.1");

        assertEquals(RespostaEmailDTO.STATUS_ENVIADO, resultado.status());
        assertNotNull(resultado.protocolo());
        assertFalse(resultado.protocolo().isEmpty());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve retornar falha quando SMTP lança exceção")
    void deveRetornarFalhaComErroSmtp() {
        var requisicao = new RequisicaoEmailDTO(
                List.of("destinatario@teste.com"),
                "Assunto",
                "Corpo",
                List.of()
        );

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(sanitizadorHtmlService.sanitizar(anyString())).thenReturn("Corpo");
        doThrow(new org.springframework.mail.MailSendException("Erro SMTP simulado"))
                .when(javaMailSender).send(any(MimeMessage.class));

        var resultado = emailService.enviarEmail(requisicao, "192.168.1.1");

        assertEquals(RespostaEmailDTO.STATUS_FALHA, resultado.status());
        assertEquals(500, resultado.codigoHttp());
        assertNotNull(resultado.protocolo());
    }

    @Test
    @DisplayName("Deve gerar protocolo UUID mesmo em caso de falha")
    void deveGerarProtocoloMesmoEmFalha() {
        var requisicao = new RequisicaoEmailDTO(
                List.of("destinatario@teste.com"),
                "Assunto",
                "Corpo",
                List.of()
        );

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(sanitizadorHtmlService.sanitizar(anyString())).thenReturn("Corpo");
        doThrow(new org.springframework.mail.MailSendException("Falha"))
                .when(javaMailSender).send(any(MimeMessage.class));

        var resultado = emailService.enviarEmail(requisicao, "10.0.0.1");

        assertNotNull(resultado.protocolo());
        assertFalse(resultado.protocolo().isEmpty());
    }
}
