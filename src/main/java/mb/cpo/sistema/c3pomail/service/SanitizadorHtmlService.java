package mb.cpo.sistema.c3pomail.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

/**
 * Serviço de sanitização HTML utilizando a biblioteca OWASP Java HTML Sanitizer.
 * Aplica uma whitelist rigorosa de tags e atributos permitidos,
 * bloqueando qualquer conteúdo potencialmente malicioso (XSS, injeção de script, etc).
 *
 * <p>Tags bloqueadas: {@code <script>}, {@code <iframe>}, {@code <object>},
 * {@code <embed>}, {@code <form>}, e qualquer evento JavaScript (onclick, onerror, etc).</p>
 */
@Service
public class SanitizadorHtmlService {

    private final PolicyFactory politicaSanitizacao;

    public SanitizadorHtmlService() {
        this.politicaSanitizacao = new HtmlPolicyBuilder()
                // Tags de estrutura de texto
                .allowElements(
                        "p", "br", "hr",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "div", "span", "blockquote", "pre", "code"
                )
                // Tags de formatação
                .allowElements(
                        "b", "i", "u", "strong", "em", "s",
                        "sub", "sup", "small"
                )
                // Tags de lista
                .allowElements("ul", "ol", "li")
                // Tags de tabela
                .allowElements(
                        "table", "thead", "tbody", "tfoot",
                        "tr", "td", "th", "caption"
                )
                // Tags de link (com restrição de protocolo)
                .allowElements("a")
                .allowUrlProtocols("https", "mailto")
                .allowAttributes("href").onElements("a")
                .allowAttributes("target").onElements("a")
                .requireRelNofollowOnLinks()
                // Tags de imagem (com restrição de protocolo)
                .allowElements("img")
                .allowUrlProtocols("https", "data")
                .allowAttributes("src", "alt", "width", "height").onElements("img")
                // Atributos globais de estilo (restritos)
                .allowAttributes("style").onElements(
                        "p", "div", "span", "td", "th", "table",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "b", "i", "u", "strong", "em"
                )
                .allowAttributes("class").globally()
                .allowAttributes("align").onElements("p", "div", "td", "th", "table")
                .allowAttributes("colspan", "rowspan").onElements("td", "th")
                .toFactory();
    }

    /**
     * Sanitiza o conteúdo HTML removendo tags e atributos perigosos.
     * Qualquer tag não presente na whitelist é removida silenciosamente.
     *
     * @param htmlBruto o conteúdo HTML enviado pelo sistema cliente
     * @return o conteúdo HTML sanitizado e seguro para envio por e-mail
     */
    public String sanitizar(String htmlBruto) {
        if (htmlBruto == null || htmlBruto.isBlank()) {
            return "";
        }
        return politicaSanitizacao.sanitize(htmlBruto);
    }
}
