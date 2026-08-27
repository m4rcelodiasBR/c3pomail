package mb.cpo.sistema.c3pomail.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários para {@link ExtratorIpOrigem}.
 */
class ExtratorIpOrigemTest {

    private ExtratorIpOrigem extrator;

    @BeforeEach
    void configurar() {
        extrator = new ExtratorIpOrigem();
    }

    @Test
    @DisplayName("Deve extrair o primeiro IP do cabeçalho X-Forwarded-For")
    void deveExtrairPrimeiroIpDoXForwardedFor() {
        var requisicao = new MockHttpServletRequest();
        requisicao.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1, 172.16.0.1");
        requisicao.setRemoteAddr("127.0.0.1");

        assertEquals("192.168.1.100", extrator.extrairIp(requisicao));
    }

    @Test
    @DisplayName("Deve extrair IP único do cabeçalho X-Forwarded-For")
    void deveExtrairIpUnicoDoXForwardedFor() {
        var requisicao = new MockHttpServletRequest();
        requisicao.addHeader("X-Forwarded-For", "10.20.30.40");
        requisicao.setRemoteAddr("127.0.0.1");

        assertEquals("10.20.30.40", extrator.extrairIp(requisicao));
    }

    @Test
    @DisplayName("Deve usar getRemoteAddr como fallback quando X-Forwarded-For está ausente")
    void deveUsarFallbackQuandoCabecalhoAusente() {
        var requisicao = new MockHttpServletRequest();
        requisicao.setRemoteAddr("192.168.0.50");

        assertEquals("192.168.0.50", extrator.extrairIp(requisicao));
    }

    @Test
    @DisplayName("Deve usar getRemoteAddr quando X-Forwarded-For está vazio")
    void deveUsarFallbackQuandoCabecalhoVazio() {
        var requisicao = new MockHttpServletRequest();
        requisicao.addHeader("X-Forwarded-For", "   ");
        requisicao.setRemoteAddr("192.168.0.51");

        assertEquals("192.168.0.51", extrator.extrairIp(requisicao));
    }
}
