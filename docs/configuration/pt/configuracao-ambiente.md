# Guia de Configuração de Ambiente

Este documento fornece instruções abrangentes de configuração para todos os ambientes da RecargaPay Wallet API.

## Índice

- [Visão Geral](#visão-geral)
- [Perfis de Ambiente](#perfis-de-ambiente)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Configurações Específicas por Perfil](#configurações-específicas-por-perfil)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
- [Configuração do Cache Redis](#configuração-do-cache-redis)
- [Configuração de Segurança](#configuração-de-segurança)
- [Configuração de Logging](#configuração-de-logging)
- [Monitoramento e Actuator](#monitoramento-e-actuator)
- [Diretrizes de Deploy](#diretrizes-de-deploy)
- [Solução de Problemas](#solução-de-problemas)
- [OpenTelemetry e Rastreamento Distribuído](#opentelemetry-e-rastreamento-distribuído)

## Visão Geral

A RecargaPay Wallet API suporta múltiplos ambientes com configurações específicas:

- **Desenvolvimento** (`dev`) - Desenvolvimento local com debug completo
- **Teste** (`test`) - Testes automatizados com banco em memória
- **Homologação** (`hml`) - Ambiente de testes pré-produção
- **Produção** (`prod`) - Ambiente de produção

## Perfis de Ambiente

### Perfis Disponíveis

| Perfil | Arquivo | Propósito | Nível de Segurança |
|--------|---------|-----------|-------------------|
| `dev` | `application-dev.yml` | Desenvolvimento local | Baixo |
| `test` | `application-test.yml` | Testes unitários/integração | Baixo |
| `hml` | `application-hml.yml` | Testes pré-produção | Médio |
| `prod` | `application-prod.yml` | Deploy de produção | Alto |

### Ativação de Perfil

```bash
# Via variável de ambiente
export SPRING_PROFILES_ACTIVE=dev

# Via argumento JVM
java -Dspring.profiles.active=prod -jar wallet-api.jar

# Via application.properties
spring.profiles.active=hml
```

## Variáveis de Ambiente

### Configuração Automática do Ambiente

O arquivo `.env` é **gerado automaticamente** quando você executa o script de inicialização:

```bash
./wallet-api-startup.sh
```

O script automaticamente:
1. Verifica se o `.env` existe e é válido
2. Se não existir, gera baseado no `src/main/resources/templates/.env.template`
3. Aplica valores padrão seguros para desenvolvimento
4. Cria backup se um `.env` existente for encontrado

### Variáveis de Ambiente Geradas

O arquivo `.env` gerado automaticamente inclui:

```bash
# Configuração do Banco de Dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=walletdb
DB_USERNAME=admin
DB_PASSWORD=admin

# Configuração de Segurança JWT
JWT_SECRET=my-super-secure-jwt-secret-key-for-development-at-least-32-characters-long

# Configuração do Cache Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Configuração do Cache
APP_CACHE_VERSION=v1

# Configuração TTL do Cache (Padrões da Indústria Financeira)
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Configuração do Usuário da Aplicação
USER_NAME=admin
USER_PASSWORD=admin

# Configuração da Aplicação
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Configuração de Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG

# Configuração CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:8080
APP_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
APP_CORS_ALLOWED_HEADERS=*
APP_CORS_ALLOW_CREDENTIALS=true
APP_CORS_MAX_AGE=3600

# Configuração SonarQube
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=admin123
```

### Variáveis Específicas por Ambiente

#### Desenvolvimento
```bash
SPRING_PROFILES_ACTIVE=dev
LOGGING_LEVEL_ROOT=DEBUG
LOGGING_LEVEL_APP=DEBUG
```

#### Teste
```bash
SPRING_PROFILES_ACTIVE=test
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_APP=INFO
```

#### Homologação
```bash
SPRING_PROFILES_ACTIVE=hml
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=INFO
```

#### Produção
```bash
SPRING_PROFILES_ACTIVE=prod
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_APP=ERROR
```

## Configurações Específicas por Perfil

### Perfil de Desenvolvimento (`application-dev.yml`)

```yaml
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
      format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

**Características:**
- Log SQL completo habilitado
- Atualizações automáticas do schema
- Todos os endpoints do actuator expostos
- Informações detalhadas de saúde

### Perfil de Teste (`application-test.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop

app:
  cache:
    version: v1
    ttl:
      default: 1  # Testes mais rápidos
```

**Características:**
- Banco H2 em memória
- Schema recriado para cada teste
- TTLs de cache reduzidos para testes mais rápidos
- Versão do cache fixa

### Perfil de Homologação (`application-hml.yml`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**Características:**
- Apenas validação do schema (sem atualizações automáticas)
- Endpoints do actuator restritos
- Detalhes de saúde apenas para autorizados
- Configurações de cache similares à produção

### Perfil de Produção (`application-prod.yml`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: never
  server:
    port: 9090
```

**Características:**
- Hibernate otimizado para performance
- Endpoints do actuator mínimos
- Sem exposição de detalhes de saúde
- Porta separada para gerenciamento

## Configuração do Banco de Dados

### Setup PostgreSQL

#### Desenvolvimento/Homologação/Produção
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

#### Ambiente de Teste
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
```

### Migração Flyway

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    enabled: true
    validate-on-migrate: true
    locations: classpath:db/migration
```

## Configuração do Cache Redis

### Configurações de Conexão

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Configuração do Cache

```yaml
app:
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      default: ${CACHE_TTL_DEFAULT_MINUTES}
      wallet-list: ${CACHE_TTL_WALLET_LIST_MINUTES}
      wallet-single: ${CACHE_TTL_WALLET_SINGLE_MINUTES}
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS}
      wallet-transactions: ${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES}
      user-profile: ${CACHE_TTL_USER_PROFILE_MINUTES}
```

### Regiões de Cache e TTLs

| Região do Cache | TTL | Caso de Uso |
|-----------------|-----|-------------|
| `wallet-list` | 3 minutos | Coleção de carteiras |
| `wallet-single` | 1 minuto | Dados de carteira individual |
| `wallet-balance` | 30 segundos | Dados financeiros críticos |
| `wallet-transactions` | 10 minutos | Dados históricos de transações |
| `user-profile` | 15 minutos | Informações do perfil do usuário |

## Configuração de Segurança

### Configuração JWT

```yaml
# Segredo JWT deve ter pelo menos 256 bits (32 caracteres)
JWT_SECRET=your_super_secure_jwt_secret_key_here_minimum_256_bits
```

### Usuário Admin

```yaml
app:
  user:
    username: ${ADMIN_USERNAME}
    password: ${ADMIN_PASSWORD}
```

### Headers de Segurança

A aplicação configura automaticamente headers de segurança abrangentes:

#### Configuração CORS
```yaml
# Origens permitidas pelo CORS (específico por ambiente)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

**Headers Implementados:**
- **Access-Control-Allow-Origin**: Configurado por ambiente
- **Access-Control-Allow-Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Access-Control-Allow-Headers**: Authorization, Content-Type, X-Requested-With
- **Access-Control-Allow-Credentials**: true

#### Headers de Segurança
**Configurados automaticamente via SecurityConfig:**
- **X-Frame-Options**: DENY (previne clickjacking)
- **X-Content-Type-Options**: nosniff (previne MIME sniffing)
- **X-XSS-Protection**: 1; mode=block (proteção XSS)
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains (HSTS)
- **Content-Security-Policy**: default-src 'self' (proteção CSP)
- **Referrer-Policy**: strict-origin-when-cross-origin

#### Validação Bean Validation
**Validação sistemática implementada:**
- **@Valid**: Aplicado a todos os endpoints de controller
- **Anotações JSR-303**: @NotNull, @NotBlank, @Size, @Email, @Positive
- **Validadores Customizados**: Validação de regras de negócio
- **Tratamento de Erros**: Respostas de erro compatíveis com RFC 7807

#### Autenticação JWT
- Gerenciamento de segredo JWT via variáveis de ambiente
- Tratamento de expiração e renovação de tokens
- Anotações de segurança a nível de método
- Proteção dos endpoints do actuator

## Configuração de Logging

### Níveis de Log por Ambiente

| Ambiente | Nível Root | Nível App | Nível Framework |
|----------|------------|-----------|-----------------|
| Desenvolvimento | DEBUG | DEBUG | INFO |
| Teste | WARN | INFO | WARN |
| Homologação | INFO | INFO | WARN |
| Produção | WARN | ERROR | ERROR |

### Logging Estruturado

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
```

**Características:**
- Logging estruturado em JSON
- Suporte a rastreamento distribuído (traceId/spanId)
- Nome da aplicação nos logs
- Informações de thread

## Monitoramento e Actuator

### Endpoints do Actuator por Ambiente

#### Desenvolvimento
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

#### Teste
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Homologação
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### Produção
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: never
  server:
    port: 9090
```

### Health Checks

A aplicação inclui indicadores de saúde customizados:
- Conectividade do banco de dados
- Conectividade do Redis
- Verificações específicas da aplicação

### Métricas

Métricas disponíveis:
- Métricas de requisições HTTP
- Pool de conexões do banco
- Taxas de hit/miss do cache
- Métricas customizadas de negócio

## OpenTelemetry e Rastreamento Distribuído

**Detalhes da Implementação:**
- **Configuração Manual do SDK**: Setup programático do OpenTelemetry
- **Contexto de Trace W3C**: Propagação de trace padrão (W3CTraceContextPropagator, W3CBaggagePropagator)
- **Filtros Customizados**: TraceContextFilter garante traceId/spanId em todos os logs
- **Integração MDC**: Contexto de trace automaticamente adicionado ao MDC dos logs
- **Correlação**: Rastreamento completo de requisições através de fronteiras de serviço

**Configuração:**
```yaml
# Tracing é configurado automaticamente via OpenTelemetryConfig
# Nenhuma variável de ambiente adicional necessária para setup básico
```

**Formato de Log com Tracing:**
```
2024-01-15 10:30:45.123 [wallet-api] [a1b2c3d4e5f6,1a2b3c4d] [http-nio-8080-exec-1] INFO  c.r.w.api.controller.WalletController - Processing deposit request
```

**Recursos:**
- TraceId único por requisição HTTP
- SpanId único por operação
- Propagação automática nos logs
- Correlação através de operações distribuídas

## Diretrizes de Deploy

### Checklist de Setup do Ambiente

#### Pré-deploy
- [ ] Executar `./wallet-api-startup.sh` (gera `.env` automaticamente se necessário)
- [ ] Verificar conectividade do banco de dados
- [ ] Verificar conectividade do Redis
- [ ] Testar força do segredo JWT
- [ ] Validar credenciais de admin

#### Desenvolvimento
- [ ] Definir `SPRING_PROFILES_ACTIVE=dev`
- [ ] Habilitar logging de debug
- [ ] Expor todos os endpoints do actuator
- [ ] Usar banco de dados local

#### Teste
- [ ] Definir `SPRING_PROFILES_ACTIVE=test`
- [ ] Usar banco H2 em memória
- [ ] Reduzir TTLs do cache
- [ ] Habilitar configurações específicas de teste

#### Homologação
- [ ] Definir `SPRING_PROFILES_ACTIVE=hml`
- [ ] Usar banco similar à produção
- [ ] Restringir endpoints do actuator
- [ ] Habilitar detalhes de saúde para autorizados

#### Produção
- [ ] Definir `SPRING_PROFILES_ACTIVE=prod`
- [ ] Usar banco de produção
- [ ] Minimizar endpoints do actuator
- [ ] Desabilitar detalhes de saúde
- [ ] Usar porta separada para gerenciamento
- [ ] Habilitar otimizações de performance

## Solução de Problemas

### Problemas Comuns

#### Aplicação Não Inicia

1. **Verificar variáveis de ambiente**
   ```bash
   # Verificar se todas as variáveis necessárias estão definidas
   env | grep -E "(DB_|REDIS_|JWT_|ADMIN_|CACHE_)"
   ```

2. **Verificar ativação do perfil**
   ```bash
   # Verificar se o perfil correto está ativo
   echo $SPRING_PROFILES_ACTIVE
   ```

3. **Verificar conectividade do banco**
   ```bash
   # Testar conexão PostgreSQL
   psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
   ```

4. **Verificar conectividade do Redis**
   ```bash
   # Testar conexão Redis
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping
   ```

#### Problemas de Cache

1. **Verificar conexão Redis**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Verificar chaves do cache**
   ```bash
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD KEYS "*"
   ```

3. **Monitorar operações do cache**
   ```bash
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD MONITOR
   ```

#### Problemas de Autenticação

1. **Verificar tamanho do segredo JWT**
   ```bash
   # Segredo JWT deve ter pelo menos 32 caracteres
   echo -n "$JWT_SECRET" | wc -c
   ```

2. **Verificar credenciais de admin**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"'$ADMIN_USERNAME'","password":"'$ADMIN_PASSWORD'"}'
   ```

### Melhores Práticas

#### Segurança
- Nunca commitar arquivos `.env` no controle de versão
- Usar segredos JWT fortes (mínimo 256 bits)
- Rotacionar credenciais regularmente
- Restringir endpoints do actuator em produção
- Usar portas separadas para gerenciamento em produção

#### Performance
- Usar pool de conexões para banco e Redis
- Configurar TTLs apropriados para cache
- Habilitar batch processing do Hibernate em produção
- Monitorar e ajustar configurações da JVM

#### Monitoramento
- Configurar health checks para todas as dependências
- Monitorar taxas de hit do cache
- Acompanhar métricas da aplicação
- Configurar alertas para problemas críticos

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Environment Setup in English](../en/environment-setup.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
