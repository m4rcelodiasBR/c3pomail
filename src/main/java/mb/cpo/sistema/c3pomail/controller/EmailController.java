package mb.cpo.sistema.c3pomail.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mb.cpo.sistema.c3pomail.dto.RequisicaoEmailDTO;
import mb.cpo.sistema.c3pomail.dto.RespostaEmailDTO;
import mb.cpo.sistema.c3pomail.service.EmailService;
import mb.cpo.sistema.c3pomail.util.ExtratorIpOrigem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operações de envio de e-mail.
 *
 * <p>Endpoint único: {@code POST /api/c3pomail/mail}</p>
 *
 * <p>Recebe a requisição validada, extrai o IP de origem
 * e delega o envio para o {@link EmailService}.</p>
 */
@RestController
@RequestMapping("/api/c3pomail")
public class EmailController {

    private final EmailService emailService;
    private final ExtratorIpOrigem extratorIpOrigem;

    public EmailController(EmailService emailService, ExtratorIpOrigem extratorIpOrigem) {
        this.emailService = emailService;
        this.extratorIpOrigem = extratorIpOrigem;
    }

    /**
     * Envia um e-mail para os destinatários informados.
     *
     * @param requisicao dados do e-mail validados via Bean Validation
     * @param request    requisição HTTP para extração do IP de origem
     * @return resposta padronizada com protocolo UUID e status da operação
     */
    @PostMapping("/mail")
    public ResponseEntity<RespostaEmailDTO> enviarEmail(
            @Valid @RequestBody RequisicaoEmailDTO requisicao,
            HttpServletRequest request) {

        var ipOrigem = extratorIpOrigem.extrairIp(request);
        var resposta = emailService.enviarEmail(requisicao, ipOrigem);

        return ResponseEntity.status(resposta.codigoHttp()).body(resposta);
    }
}
