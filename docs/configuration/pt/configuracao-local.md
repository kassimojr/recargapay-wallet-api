# 🚀 Configuração de Desenvolvimento Local

Este guia ajuda você a configurar a Digital Wallet API para desenvolvimento local com configuração de ambiente adequada e práticas de segurança.

## 📋 Visão Geral

O projeto usa variáveis de ambiente para gerenciamento de configuração, eliminando credenciais hardcoded e tornando o desenvolvimento local mais seguro e flexível.

## ⚡ Setup Rápido

### 1. Configuração Automática do Ambiente

O arquivo `.env` é **gerado automaticamente** quando você executa o script de inicialização:

```bash
./wallet-api-startup.sh
```

O script automaticamente:
- Gera `.env` baseado no `src/main/resources/templates/.env.template`
- Aplica valores padrão seguros para desenvolvimento
- Cria backup se um `.env` existente for encontrado

O arquivo `.env` gerado inclui:

```bash
# Configuração do Banco de Dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=walletdb
DB_USERNAME=admin
DB_PASSWORD=admin

# Configuração JWT (mínimo 32 caracteres)
JWT_SECRET=my-super-secure-jwt-secret-key-for-development-at-least-32-characters-long

# Configuração do Usuário da Aplicação
USER_NAME=admin
USER_PASSWORD=admin

# Configuração do Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Configuração do Cache
APP_CACHE_VERSION=v1
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Configuração da Aplicação
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Configuração de Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG

# Configuração CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:8080

# Configuração SonarQube
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=admin123
```

### 2. Corrigir Permissões de Scripts (Se Necessário)

Se você encontrar erros de permissão ao executar scripts, corrija todas as permissões de uma vez:

```bash
# Dar permissão de execução para todos os scripts .sh
find . -name "*.sh" -type f -exec chmod +x {} \;
```

**Métodos alternativos:**
```bash
# Usando xargs (mais legível)
find . -name "*.sh" -type f | xargs chmod +x

# Para scripts específicos apenas
chmod +x *.sh scripts/*.sh scripts/utils/*.sh
```

**Dica Pro:** Adicione este alias ao seu `~/.bashrc` ou `~/.zshrc`:
```bash
alias fix-scripts="find . -name '*.sh' -type f -exec chmod +x {} \;"
```

### 3. Iniciar Serviços

Inicie todos os serviços necessários usando Docker Compose:

```bash
docker-compose up -d
```

Isso iniciará:
- **PostgreSQL** banco de dados na porta 5432
- **Redis** cache na porta 6379
- **Grafana** monitoramento na porta 3000
- **Loki** agregação de logs
- **Promtail** coleta de logs

### 4. Iniciar a Aplicação

```bash
./mvnw spring-boot:run
```

A aplicação carregará automaticamente as variáveis de ambiente do arquivo `.env`.

### 5. Verificar Setup

Verifique se tudo está funcionando:

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health

# Testar autenticação
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

## 🔧 Métodos Alternativos de Configuração

### Opção 1: Variáveis de Ambiente do Sistema

Defina variáveis de ambiente diretamente no seu sistema:

```bash
export DB_USERNAME=seu_usuario_banco
export DB_PASSWORD=sua_senha_banco
export JWT_SECRET=sua_chave_jwt_secreta_com_pelo_menos_32_caracteres
export ADMIN_USERNAME=seu_usuario_admin
export ADMIN_PASSWORD=sua_senha_admin
```

### Opção 2: Configuração da IDE

Configure variáveis de ambiente na sua IDE:

#### IntelliJ IDEA
1. Vá para **Run/Debug Configurations**
2. Selecione sua configuração Spring Boot
3. Adicione variáveis de ambiente na seção **Environment Variables**

#### VS Code
1. Crie `.vscode/launch.json`
2. Adicione variáveis de ambiente na configuração:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot-WalletApiApplication",
      "request": "launch",
      "mainClass": "com.digital.wallet.WalletApiApplication",
      "env": {
        "DB_USERNAME": "seu_usuario_banco",
        "DB_PASSWORD": "sua_senha_banco",
        "JWT_SECRET": "sua_chave_jwt_secreta_com_pelo_menos_32_caracteres",
        "ADMIN_USERNAME": "seu_usuario_admin",
        "ADMIN_PASSWORD": "sua_senha_admin"
      }
    }
  ]
}
```

## 🐳 Desenvolvimento com Docker

### Setup Completo com Docker

Execute toda a stack da aplicação com Docker:

```bash
# Construir a aplicação
./mvnw clean package -DskipTests

# Iniciar todos os serviços incluindo a aplicação
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

### Apenas Banco de Dados

Se preferir executar apenas o banco de dados no Docker:

```bash
docker-compose up -d postgres redis
```

Então execute a aplicação localmente:

```bash
./mvnw spring-boot:run
```

## 🔍 Solução de Problemas

### Problemas Comuns

#### 1. Aplicação Não Inicia

**Problema**: Variáveis de ambiente ausentes
```bash
# Verificar se todas as variáveis necessárias estão definidas
env | grep -E "(DB_|JWT_|ADMIN_|REDIS_|CACHE_)"
```

**Solução**: Garantir que todas as variáveis necessárias estejam definidas no `.env` ou ambiente do sistema

#### 2. Problemas de Conexão com Banco

**Problema**: Não consegue conectar ao PostgreSQL
```bash
# Testar conexão com banco
psql -h localhost -p 5432 -U seu_usuario -d digital_wallet
```

**Soluções**:
- Garantir que PostgreSQL esteja rodando: `docker-compose up -d postgres`
- Verificar credenciais do banco no `.env`
- Verificar nome do banco e porta

#### 3. Problemas de Conexão com Redis

**Problema**: Não consegue conectar ao Redis
```bash
# Testar conexão Redis
redis-cli -h localhost -p 6379 -a sua_senha_redis ping
```

**Soluções**:
- Garantir que Redis esteja rodando: `docker-compose up -d redis`
- Verificar senha do Redis no `.env`
- Verificar host e porta do Redis

#### 4. Problemas de Autenticação

**Problema**: Login falha
```bash
# Verificar comprimento do segredo JWT
echo -n "$JWT_SECRET" | wc -c  # Deve ser >= 32
```

**Soluções**:
- Garantir que segredo JWT tenha pelo menos 32 caracteres
- Verificar credenciais de admin no `.env`
- Verificar se endpoint de autenticação está acessível

## 📊 Ferramentas de Desenvolvimento

### Testes de API

Importe as coleções fornecidas:
- **Postman**: [Coleção](../../api/postman/)
- **Insomnia**: [Coleção](../../api/insomnia/)

### Acesso ao Banco de Dados

Conecte ao PostgreSQL:
```bash
# Linha de comando
psql -h localhost -p 5432 -U seu_usuario -d digital_wallet

# Ou use uma ferramenta GUI como pgAdmin, DBeaver, etc.
```

### Monitoramento do Cache

Monitore o cache Redis:
```bash
# Redis CLI
redis-cli -h localhost -p 6379 -a sua_senha_redis

# Monitorar operações do cache
redis-cli -h localhost -p 6379 -a sua_senha_redis MONITOR
```

### Observabilidade

Acesse ferramentas de monitoramento:
- **Grafana**: http://localhost:3000 (admin/admin)
- **Logs da Aplicação**: Verificar saída do console ou `logs/wallet-api.json`

## 🔒 Notas de Segurança

### Segurança do Arquivo de Ambiente

- ✅ **Nunca commitar arquivos `.env`** no controle de versão
- ✅ **Usar senhas fortes** (mínimo 12 caracteres)
- ✅ **Usar segredos JWT únicos** (mínimo 32 caracteres)
- ✅ **Rotacionar credenciais regularmente**

### Desenvolvimento vs Produção

- **Desenvolvimento**: Usa arquivo `.env` por conveniência
- **Produção**: Usa variáveis de ambiente do sistema
- **Segredos diferentes**: Cada ambiente deve ter credenciais únicas

## 📚 Próximos Passos

Após setup local bem-sucedido:

1. **Explorar a API**: [Documentação da API](../../../README.md#api-reference)
2. **Entender Arquitetura**: [Guia de Arquitetura](../../../README.md#architecture)
3. **Executar Testes**: `./mvnw test`
4. **Verificar Cobertura**: `./mvnw jacoco:report`
5. **Onboarding do Time**: [Checklist Completo](../../onboarding/pt/integracao-time.md)

## 🔗 Documentação Relacionada

- **🏠 Documentação Principal**: [README do Projeto](../../README-PT.md)
- **⚙️ Setup de Ambiente**: [Guia Completo de Configuração](configuracao-local.md)
- **🔒 Segurança**: [Configuração de Segurança](../../security/pt/configuracao-seguranca.md)
- **🚀 Onboarding do Time**: [Guia de Integração](../../onboarding/pt/)

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Local Setup in English](../en/local-setup.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
