# Guia de Configuração de Senhas SonarQube

## 🔐 Visão Geral

Este guia explica como configurar senhas do SonarQube para diferentes ambientes e cenários, garantindo automação robusta mesmo quando as senhas mudam.

## 🎯 Cenários Suportados

### ✅ Cenários de Mudança de Senha Cobertos:
1. **Mudanças Manuais de Senha** - Admin altera senha via interface
2. **Políticas de Segurança Corporativa** - Rotação forçada de senhas
3. **Diferenças de Ambiente** - Senhas diferentes por ambiente (dev/staging/prod)
4. **Automação CI/CD** - Integração com gerenciamento de segredos
5. **Configuração Inicial** - Tratamento automatizado de reset forçado de senha

## ⚙️ Métodos de Configuração

### 1. Variáveis de Ambiente (Maior Prioridade)
```bash
export SONAR_USER="admin"
export SONAR_PASS="sua_senha_atual"
export SONAR_NEW_PASS="sua_nova_senha"

# Convenção alternativa de nomenclatura
export SONARQUBE_USER="admin"
export SONARQUBE_PASSWORD="sua_senha_atual"
export SONARQUBE_NEW_PASSWORD="sua_nova_senha"
```

### 2. Configuração via Arquivo .env
Crie um arquivo `.env` na raiz do seu projeto:
```bash
# Configuração de Autenticação SonarQube
SONAR_USER=admin
SONAR_PASS=sua_senha_atual
SONAR_NEW_PASS=sua_nova_senha

# Nomenclatura alternativa (ambas suportadas)
SONARQUBE_USER=admin
SONARQUBE_PASSWORD=sua_senha_atual
SONARQUBE_NEW_PASSWORD=sua_nova_senha
```

### 3. Candidatos de Senha Fallback
O script automaticamente tenta essas senhas em ordem:
1. Senha primária do env/config
2. `admin` (padrão)
3. `admin123` (senha alterada comum)
4. Nova senha configurada
5. `sonar` (padrão alternativo)
6. `password` (fallback comum)

## 🚀 Exemplos de Uso

### Ambiente de Desenvolvimento
```bash
# arquivo .env
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=dev123
```

### Ambiente de Staging
```bash
# Variáveis de ambiente
export SONAR_USER="admin"
export SONAR_PASS="senha_segura_staging"
export SONAR_NEW_PASS="nova_senha_staging"
```

### Ambiente de Produção (CI/CD)
```bash
# Usando gerenciamento de segredos
export SONAR_USER="${VAULT_SONAR_USER}"
export SONAR_PASS="${VAULT_SONAR_PASSWORD}"
export SONAR_NEW_PASS="${VAULT_SONAR_NEW_PASSWORD}"
```

## 🔧 Configuração Avançada

### Candidatos de Senha Personalizados
Você pode modificar o script para incluir senhas comuns da sua organização:

```bash
# Em setup-sonarqube-automation.sh
SONAR_PASSWORD_CANDIDATES=(
    "$SONAR_PASS"           # Primária do env/config
    "admin"                 # Padrão
    "admin123"              # Senha alterada comum
    "$SONAR_NEW_PASS"       # Nova senha configurada
    "padrao_sua_org"        # Adicione padrões da sua organização
    "sua_senha_comum"       # Adicione senhas comuns
)
```

## 🛡️ Melhores Práticas de Segurança

### ✅ Práticas Recomendadas:
- **Use variáveis de ambiente** para produção
- **Nunca commite senhas** no controle de versão
- **Use gerenciamento de segredos** em pipelines CI/CD
- **Rotacione senhas regularmente**
- **Use senhas fortes** (evite padrões)

### ❌ Evite:
- Codificar senhas diretamente em scripts
- Usar senhas padrão em produção
- Commitar arquivos `.env` com senhas reais
- Compartilhar senhas em texto simples

## 🔍 Troubleshooting

### Falha na Autenticação de Senha
1. **Verifique senha atual**: Confirme se a senha está correta
2. **Teste login manual**: Teste credenciais na interface SonarQube
3. **Verifique variáveis de ambiente**: Garanta que variáveis estão definidas corretamente
4. **Revise logs**: Verifique saída do script para tentativas de autenticação

### Script Não Consegue Encontrar Senha
1. **Verifique arquivo .env**: Garanta que arquivo existe e tem formato correto
2. **Verifique nomes de variáveis**: Use nomes de variáveis suportados
3. **Teste variáveis de ambiente**: `echo $SONAR_PASS`
4. **Revise candidatos fallback**: Adicione suas senhas à lista de candidatos

### Problemas com Múltiplos Ambientes
1. **Use arquivos .env diferentes**: `.env.dev`, `.env.staging`, `.env.prod`
2. **Variáveis específicas do ambiente**: Use variáveis de ambiente CI/CD
3. **Configuração condicional**: Script pode carregar configs diferentes por ambiente

## 📋 Checklist de Configuração

- [ ] Escolher método de configuração (variáveis env ou arquivo .env)
- [ ] Definir SONAR_USER (padrão: admin)
- [ ] Definir SONAR_PASS (senha atual)
- [ ] Definir SONAR_NEW_PASS (senha para alterar)
- [ ] Testar autenticação com `./scripts/setup-sonarqube-automation.sh`
- [ ] Verificar automação funciona com `./wallet-api-startup.sh`
- [ ] Documentar senhas no seu sistema de gerenciamento de segredos

## 🔄 Fluxo de Rotação de Senhas

### Quando Senha Muda:
1. **Atualizar configuração** (variáveis env ou .env)
2. **Testar autenticação** com nova senha
3. **Atualizar segredos CI/CD** se aplicável
4. **Executar script de automação** para verificar
5. **Atualizar documentação** para equipe

### Rotação Automatizada:
```bash
# Exemplo de script de rotação
OLD_PASS="$SONAR_PASS"
NEW_PASS="$(generate_secure_password)"

export SONAR_PASS="$OLD_PASS"
export SONAR_NEW_PASS="$NEW_PASS"

# Executar automação
./scripts/setup-sonarqube-automation.sh

# Atualizar gerenciamento de segredos
update_vault_secret "SONAR_PASS" "$NEW_PASS"
```

## 🎯 Exemplos de Integração

### GitHub Actions
```yaml
env:
  SONAR_USER: ${{ secrets.SONAR_USER }}
  SONAR_PASS: ${{ secrets.SONAR_PASSWORD }}
  SONAR_NEW_PASS: ${{ secrets.SONAR_NEW_PASSWORD }}
```

### GitLab CI
```yaml
variables:
  SONAR_USER: $SONAR_USER
  SONAR_PASS: $SONAR_PASSWORD
  SONAR_NEW_PASS: $SONAR_NEW_PASSWORD
```

### Jenkins
```groovy
environment {
    SONAR_USER = credentials('sonar-user')
    SONAR_PASS = credentials('sonar-password')
    SONAR_NEW_PASS = credentials('sonar-new-password')
}
```

---

## 🔗 Documentação Relacionada

- [Automação SonarQube](sonarqube-automation.md)
- [Configuração de Ambiente](configuracao-ambiente.md)
- [Versões Docker](docker-versions.md)

---

## 📞 Suporte

Se você encontrar problemas com configuração de senhas:
1. Consulte esta documentação
2. Revise logs do script
3. Teste autenticação manual
4. Entre em contato com sua equipe DevOps para ajuda com gerenciamento de segredos

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [SonarQube Password Configuration](../en/sonarqube-password-config.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../docs/README-PT.md).*
