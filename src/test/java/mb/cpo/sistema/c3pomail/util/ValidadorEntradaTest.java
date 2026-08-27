package mb.cpo.sistema.c3pomail.util;

import mb.cpo.sistema.c3pomail.exception.EmailInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ValidadorEntrada}.
 */
class ValidadorEntradaTest {

    private ValidadorEntrada validador;

    @BeforeEach
    void configurar() {
        validador = new ValidadorEntrada();
    }

    // ========== Testes de E-mail Válido ==========

    @ParameterizedTest
    @ValueSource(strings = {
            "usuario@dominio.com",
            "usuario.nome@subdominio.dominio.com.br",
            "usuario+tag@dominio.com",
            "teste123@empresa.org",
            "a@b.co"
    })
    @DisplayName("Deve aceitar e-mails com formato válido")
    void deveAceitarEmailsValidos(String email) {
        assertDoesNotThrow(() -> validador.validarFormatoEmail(email));
    }

    // ========== Testes de E-mail Inválido ==========

    @ParameterizedTest
    @ValueSource(strings = {
            "sem-arroba.com",
            "@sem-usuario.com",
            "usuario@",
            "usuario@.com",
            "usuario @dominio.com",
            "usuario@dominio",
            "",
            "   "
    })
    @DisplayName("Deve rejeitar e-mails com formato inválido")
    void deveRejeitarEmailsInvalidos(String email) {
        assertThrows(EmailInvalidoException.class, () -> validador.validarFormatoEmail(email));
    }

    @Test
    @DisplayName("Deve rejeitar e-mail nulo")
    void deveRejeitarEmailNulo() {
        assertThrows(EmailInvalidoException.class, () -> validador.validarFormatoEmail(null));
    }

    // ========== Testes de Lista de E-mails ==========

    @Test
    @DisplayName("Deve aceitar lista vazia ou nula sem lançar exceção")
    void deveAceitarListaVaziaOuNula() {
        assertDoesNotThrow(() -> validador.validarListaEmails(null));
        assertDoesNotThrow(() -> validador.validarListaEmails(List.of()));
    }

    @Test
    @DisplayName("Deve rejeitar se qualquer e-mail da lista for inválido")
    void deveRejeitarListaComEmailInvalido() {
        var lista = List.of("valido@dominio.com", "invalido", "outro@dominio.com");
        assertThrows(EmailInvalidoException.class, () -> validador.validarListaEmails(lista));
    }

    // ========== Testes de Tamanho ==========

    @Test
    @DisplayName("Deve rejeitar assunto que excede 255 caracteres")
    void deveRejeitarAssuntoGrande() {
        var assuntoGrande = "A".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> validador.validarAssunto(assuntoGrande));
    }

    @Test
    @DisplayName("Deve aceitar assunto dentro do limite")
    void deveAceitarAssuntoDentroDoLimite() {
        assertDoesNotThrow(() -> validador.validarAssunto("A".repeat(255)));
    }

    @Test
    @DisplayName("Deve rejeitar corpo que excede 50.000 caracteres")
    void deveRejeitarCorpoGrande() {
        var corpoGrande = "X".repeat(50_001);
        assertThrows(IllegalArgumentException.class, () -> validador.validarCorpo(corpoGrande));
    }

    // ========== Testes de Sanitização para Log ==========

    @Test
    @DisplayName("Deve remover caracteres de controle na sanitização para log")
    void deveRemoverCaracteresDeControle() {
        var comControle = "texto\u0000com\u0007controle\nnova-linha";
        var resultado = validador.sanitizarParaLog(comControle);
        assertFalse(resultado.contains("\u0000"));
        assertFalse(resultado.contains("\u0007"));
        assertFalse(resultado.contains("\n"));
    }

    @Test
    @DisplayName("Deve truncar strings maiores que 200 caracteres")
    void deveTruncarStringsLongas() {
        var textoLongo = "A".repeat(300);
        var resultado = validador.sanitizarParaLog(textoLongo);
        assertTrue(resultado.endsWith("...(truncado)"));
        assertTrue(resultado.length() < 300);
    }

    @Test
    @DisplayName("Deve retornar N/A para entrada nula")
    void deveRetornarNaParaNulo() {
        assertEquals("N/A", validador.sanitizarParaLog(null));
    }
}
