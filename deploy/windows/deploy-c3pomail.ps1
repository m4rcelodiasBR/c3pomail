<#
.SYNOPSIS
Script nativo do Windows para deploy automatizado (Push) da API C3PO Mail no Oracle Linux 9.

.DESCRIPTION
Utiliza o cliente OpenSSH do Windows (SSH/SCP) para transferir o executável JAR,
arquivos de configuração e gerenciar o serviço via Systemd.
#>

# =====================================================================
# CONFIGURAÇÕES DO SERVIDOR
# =====================================================================
$IP_SERVIDOR = "192.168.X.X"         # Mude para o IP do seu Oracle Linux 9
$USUARIO_SSH = "opc"                 # Ex: opc, oracle ou root
$CHAVE_SSH = "C:\Caminho\sua_chave.pem" # Caminho Windows para a sua chave privada

# Caminhos remotos no Linux
$DIR_APP = "/opt/c3pomail"
$DIR_CONF = "/etc/c3pomail"
$DIR_SYSTEMD = "/etc/systemd/system"

# =====================================================================
# EXECUÇÃO DO DEPLOY
# =====================================================================
Write-Host "Iniciando Deploy do C3PO Mail no servidor $IP_SERVIDOR..." -ForegroundColor Cyan

# 1. Parar o serviço atual (se existir) e criar diretórios (via SSH)
Write-Host "1. Preparando servidor (parando serviço antigo e checando diretórios)..." -ForegroundColor Yellow
$cmdPreparacao = "sudo systemctl stop c3pomail 2>/dev/null; sudo mkdir -p $DIR_APP/logs; sudo mkdir -p $DIR_CONF; sudo chmod 755 $DIR_APP"
ssh -i $CHAVE_SSH -o StrictHostKeyChecking=no ${USUARIO_SSH}@${IP_SERVIDOR} $cmdPreparacao

# 2. Transferir o arquivo JAR novo (via SCP)
Write-Host "2. Transferindo o arquivo c3pomail.jar (Isso pode demorar alguns segundos)..." -ForegroundColor Yellow
scp -i $CHAVE_SSH -o StrictHostKeyChecking=no "..\..\target\c3pomail.jar" "${USUARIO_SSH}@${IP_SERVIDOR}:~/c3pomail.jar"
ssh -i $CHAVE_SSH ${USUARIO_SSH}@${IP_SERVIDOR} "sudo mv ~/c3pomail.jar $DIR_APP/c3pomail.jar; sudo chmod 755 $DIR_APP/c3pomail.jar"

# 3. Transferir arquivo Systemd
Write-Host "3. Atualizando o serviço Systemd..." -ForegroundColor Yellow
scp -i $CHAVE_SSH "..\c3pomail.service" "${USUARIO_SSH}@${IP_SERVIDOR}:~/c3pomail.service"
ssh -i $CHAVE_SSH ${USUARIO_SSH}@${IP_SERVIDOR} "sudo mv ~/c3pomail.service $DIR_SYSTEMD/c3pomail.service; sudo chmod 644 $DIR_SYSTEMD/c3pomail.service"

# 4. Transferir variáveis de ambiente (Se não existir no destino)
Write-Host "4. Checando variáveis de ambiente..." -ForegroundColor Yellow
$cmdEnvCheck = "if [ ! -f $DIR_CONF/c3pomail.env ]; then echo 'FALTA_ENV'; fi"
$resultadoEnv = ssh -i $CHAVE_SSH ${USUARIO_SSH}@${IP_SERVIDOR} $cmdEnvCheck

if ($resultadoEnv -match 'FALTA_ENV') {
    Write-Host "   -> Arquivo c3pomail.env não encontrado no servidor. Copiando arquivo de exemplo..." -ForegroundColor DarkYellow
    scp -i $CHAVE_SSH "..\c3pomail.env.exemplo" "${USUARIO_SSH}@${IP_SERVIDOR}:~/c3pomail.env"
    ssh -i $CHAVE_SSH ${USUARIO_SSH}@${IP_SERVIDOR} "sudo mv ~/c3pomail.env $DIR_CONF/c3pomail.env; sudo chmod 600 $DIR_CONF/c3pomail.env"
    Write-Host "   -> [ALERTA] Lembre-se de editar o arquivo c3pomail.env no servidor com a chave API e dados SMTP reais!" -ForegroundColor Red
} else {
    Write-Host "   -> Arquivo c3pomail.env já existe. Protegendo senhas atuais e pulando cópia." -ForegroundColor Green
}

# 5. Reiniciar a aplicação
Write-Host "5. Reiniciando a aplicação Java no Oracle Linux..." -ForegroundColor Yellow
$cmdReiniciar = "sudo systemctl daemon-reload; sudo systemctl enable c3pomail; sudo systemctl start c3pomail"
ssh -i $CHAVE_SSH ${USUARIO_SSH}@${IP_SERVIDOR} $cmdReiniciar

Write-Host "Deploy finalizado com sucesso! A nova versão já está rodando." -ForegroundColor Green
Write-Host "Para acompanhar os logs no servidor, acesse via SSH e digite:" -ForegroundColor Gray
Write-Host "sudo tail -f /opt/c3pomail/logs/c3pomail.log" -ForegroundColor Gray
