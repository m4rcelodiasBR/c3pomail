# 🚀 Guia de Deploy com Ansible via Oracle Linux 9 (WSL)

Como você possui o **Oracle Linux 9 rodando no seu WSL** (Windows Subsystem for Linux), você tem em mãos a ferramenta perfeita para atuar como o **Nó Controlador do Ansible**! 

Com isso, você pode disparar os deploys direto do seu computador local (pelo terminal do WSL) para o seu servidor remoto de Produção, aproveitando a segurança e idempotência nativas da ferramenta.

---

## 🛠️ Passo 1: Instalar o Ansible no WSL

Abra o seu terminal do Oracle Linux 9 (WSL) e atualize os pacotes. Como o Ansible fica no repositório `EPEL`, precisamos ativá-lo primeiro:

```bash
# 1. Habilitar o repositório EPEL
sudo dnf install epel-release -y

# 2. Instalar o Ansible
sudo dnf install ansible -y

# 3. Verificar se instalou corretamente
ansible --version
```

---

## 📂 Passo 2: Navegar até o Projeto

O WSL mapeia os seus discos do Windows dentro do Linux. Seu disco `D:` fica em `/mnt/d/`.

No terminal do WSL, vá até a pasta onde criamos o playbook:

```bash
cd "/mnt/d/10_Dev/Projetos/Projeto Java/c3po-mail/deploy/ansible"
```
*(Dica: Se der erro de caminho por causa do espaço no nome da pasta, utilize sempre as aspas duplas, como acima).*

---

## ⚙️ Passo 3: Configurar o Alvo no Inventário

Pelo VSCode no Windows mesmo, você pode editar o arquivo [`hosts.ini`](hosts.ini) para apontar para o seu servidor de produção.

Exemplo de como deve ficar:
```ini
[c3po_servers]
servidor_prod ansible_host=192.168.10.50 ansible_user=opc ansible_ssh_private_key_file=/mnt/c/Caminho/sua_chave.pem
```

> [!WARNING]
> **Atenção à Chave SSH:** Como você está no WSL (Linux), o caminho da sua chave `.pem` deve usar o padrão Linux (ex: `/mnt/c/...` em vez de `C:\...`). Além disso, o SSH no Linux exige que a chave privada seja secreta. Garanta que ela tenha a permissão correta rodando no WSL:
> `chmod 600 /mnt/c/Caminho/sua_chave.pem`

---

## 🚀 Passo 4: Disparar o Deploy

Com tudo configurado, é só rodar o comando abaixo de dentro da pasta `deploy/ansible/` no seu WSL:

```bash
ansible-playbook -i hosts.ini deploy-c3pomail.yml
```

### O que o Ansible vai fazer?
1. **Conectar** de forma invisível via SSH ao seu servidor Oracle de Produção.
2. **Transferir** o arquivo `.jar` mais recente (que está na pasta `target/`).
3. **Checar** e criar pastas.
4. **Desligar** a versão antiga, registrar o `.service` e **Ligar** a versão nova.

Tudo isso será exibido na sua tela (verde para OK, amarelo para Modificado e vermelho para Erro).

---

## 💡 Dica Extra: Testando no próprio WSL?
Se a sua intenção era usar o próprio WSL como o **"Servidor de Testes Alvo"** (fazer o deploy dentro do próprio WSL), lembre-se que o WSL, por padrão, não inicia o gerenciador de serviços `systemd`. 
Para testar aplicações Systemd dentro do WSL, você precisará habilitar o suporte ao Systemd no arquivo `/etc/wsl.conf` do seu WSL (Windows 11 suporta isso nativamente agora). Caso contrário, o Ansible falhará na etapa de reiniciar o serviço.
