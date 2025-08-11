# Guia de Referência de Configurações

## Visão Geral

Este documento fornece uma referência abrangente para todas as propriedades de configuração utilizadas no projeto RecargaPay Wallet API. Cada propriedade é documentada com seu propósito, impacto e relacionamento com outras configurações.

## Índice

- [Configuração da Aplicação (application.yml)](#configuração-da-aplicação-applicationyml)
- [Configurações do Stack de Monitoramento](#configurações-do-stack-de-monitoramento)
- [Dependências de Configuração](#dependências-de-configuração)
- [Configurações Específicas por Ambiente](#configurações-específicas-por-ambiente)

---

## Configuração da Aplicação (application.yml)

### Núcleo do Spring Framework

#### spring.application.name
- **Tipo**: string
- **Valor Atual**: `recargapay-wallet-api`
- **Descrição**: Define o nome da aplicação usado em componentes do Spring Boot
- **Finalidade**: Usado em padrões de logging, labels de métricas e tracing distribuído para identificar este serviço
- **Impacto**: Se removido, a identificação padrão do serviço será perdida em logs e monitoramento
- **Relacionamentos**: Referenciado em logging.pattern.console e otel.service.name

#### spring.datasource.url
- **Tipo**: string
- **Valor Atual**: `jdbc:postgresql://localhost:5432/walletdb`
- **Descrição**: URL JDBC para conexão com banco PostgreSQL
- **Finalidade**: Conexão primária com banco de dados para persistência de carteiras e transações
- **Impacto**: Aplicação não pode iniciar sem conexão válida com banco
- **Relacionamentos**: Deve corresponder à configuração do serviço PostgreSQL no docker-compose.yml

#### spring.datasource.username/password
- **Tipo**: string
- **Valor Atual**: `admin`/`admin`
- **Descrição**: Credenciais do banco de dados para autenticação
- **Finalidade**: Autentica com o banco PostgreSQL
- **Impacto**: Conexão com banco falhará com credenciais incorretas
- **Ambiente**: Desenvolvimento (hardcoded), Produção (deve usar secrets)

### Configuração JPA/Hibernate

#### spring.jpa.hibernate.ddl-auto
- **Tipo**: string
- **Valor Atual**: `validate`
- **Descrição**: Estratégia de gerenciamento de schema do Hibernate
- **Finalidade**: Valida schema do banco contra mapeamentos de entidades sem modificá-lo
- **Impacto**: Previne mudanças acidentais no schema, garante que Flyway gerencie todas as migrações
- **Crítico**: Deve permanecer 'validate' para funcionar com Flyway

#### spring.jpa.show-sql
- **Tipo**: boolean
- **Valor Atual**: `true`
- **Descrição**: Habilita logging de comandos SQL
- **Finalidade**: Ajuda a debugar consultas de banco durante desenvolvimento
- **Ambiente**: Desenvolvimento (true), Produção (deve ser false)

### Migração Flyway

#### spring.flyway.enabled
- **Tipo**: boolean
- **Valor Atual**: `true`
- **Descrição**: Habilita migração de banco Flyway
- **Finalidade**: Gerencia evolução do schema do banco
- **Impacto**: Schema do banco não será gerenciado se desabilitado
- **Crítico**: Necessário para gerenciamento de schema

#### spring.flyway.locations
- **Tipo**: array
- **Valor Atual**: `classpath:db/migration`
- **Descrição**: Localização dos scripts de migração
- **Finalidade**: Aponta para arquivos SQL de migração em src/main/resources/db/migration
- **Impacto**: Migrações não serão encontradas se caminho estiver errado

### Configuração de Segurança

#### spring.security.oauth2.resourceserver.jwt.secret
- **Tipo**: string
- **Valor Atual**: `Q4!z8@pW#r2$Lm9^X7eF%uS6bT1&cV0*Y3jH`
- **Descrição**: Chave secreta para validação de tokens JWT
- **Finalidade**: Valida tokens JWT na autenticação
- **Impacto**: Autenticação falhará sem chave válida
- **Usado Por**: AuthController, JwtDecoderConfig, TestJwtConfig
- **Ambiente**: Desenvolvimento (hardcoded), Produção (deve usar secrets)

### Documentação (SpringDoc)

#### springdoc.api-docs.enabled
- **Tipo**: boolean
- **Valor Atual**: `true`
- **Descrição**: Habilita geração de documentação OpenAPI
- **Finalidade**: Fornece documentação interativa da API via Swagger UI
- **Impacto**: Documentação da API não estará disponível se desabilitado

#### springdoc.swagger-ui.path
- **Tipo**: string
- **Valor Atual**: `/swagger-ui.html`
- **Descrição**: Caminho para interface Swagger UI
- **Finalidade**: Documentação interativa da API acessível em http://localhost:8080/swagger-ui.html
- **Relacionamentos**: Liberado no SecurityConfig

### Configuração OpenTelemetry

#### otel.sdk.disabled
- **Tipo**: boolean
- **Valor Atual**: `true`
- **Descrição**: Desabilita auto-configuração do SDK OpenTelemetry
- **Finalidade**: Usa configuração manual ao invés de instrumentação automática
- **Impacto**: Previne conflitos de instrumentação automática
- **Relacionamentos**: Permite que TraceContextFilter customizado funcione adequadamente

#### otel.service.name
- **Tipo**: string
- **Valor Atual**: `recargapay-wallet-api`
- **Descrição**: Nome do serviço para tracing distribuído
- **Finalidade**: Identifica serviço em traces distribuídos e logs
- **Relacionamentos**: Deve corresponder a spring.application.name

#### otel.propagators
- **Tipo**: string
- **Valor Atual**: `tracecontext,baggage`
- **Descrição**: Formatos de propagação de contexto de trace padrão W3C
- **Finalidade**: Habilita tracing distribuído entre serviços
- **Usado Por**: TraceContextFilter para propagação de trace

### Configuração de Logging

#### logging.pattern.console
- **Tipo**: string
- **Valor Atual**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n`
- **Descrição**: Padrão de formato de log do console com correlação de trace
- **Finalidade**: Logging estruturado que inclui traceId e spanId para correlação
- **Impacto**: Essencial para tracing distribuído e correlação de logs
- **Relacionamentos**: Usa spring.application.name e valores MDC do TraceContextFilter

#### logging.level.com.recargapay.wallet
- **Tipo**: string
- **Valor Atual**: `DEBUG`
- **Descrição**: Nível de log específico da aplicação
- **Finalidade**: Logging detalhado para debug do código da aplicação
- **Ambiente**: Desenvolvimento (DEBUG), Produção (INFO recomendado)

### Actuator/Monitoramento

#### management.endpoints.web.exposure.include
- **Tipo**: array
- **Valor Atual**: `health,info,prometheus,metrics,loggers,env`
- **Descrição**: Endpoints do actuator expostos para monitoramento
- **Finalidade**: Habilita health checks, coleta de métricas e gerenciamento em runtime
- **Impacto**: Endpoints não listados não estarão acessíveis
- **Usado Por**: Scraping do Prometheus, health checks do Kubernetes

#### management.endpoint.health.show-details
- **Tipo**: string
- **Valor Atual**: `always`
- **Descrição**: Nível de detalhes do endpoint de health
- **Finalidade**: Mostra informações detalhadas de saúde para debug
- **Ambiente**: Desenvolvimento (always), Produção (when-authorized recomendado)

#### management.endpoint.prometheus.enabled
- **Tipo**: boolean
- **Valor Atual**: `true`
- **Descrição**: Habilita endpoint de métricas Prometheus
- **Finalidade**: Fornece métricas para scraping do Prometheus em /actuator/prometheus
- **Crítico**: Necessário para integração com stack de monitoramento

---

## Configurações do Stack de Monitoramento

### Configuração Loki (loki-config.yaml)

#### auth_enabled
- **Tipo**: boolean
- **Valor Atual**: `false`
- **Descrição**: Desabilita autenticação para Loki
- **Finalidade**: Simplifica configuração do ambiente de desenvolvimento
- **Ambiente**: Desenvolvimento (false), Produção (true recomendado)

#### server.http_listen_port
- **Tipo**: integer
- **Valor Atual**: `3100`
- **Descrição**: Porta do servidor HTTP para Loki
- **Finalidade**: Porta para ingestão de logs e consultas
- **Relacionamentos**: Referenciado no mapeamento de portas do docker-compose.yml e configuração do cliente Promtail

#### limits_config.max_streams_per_user
- **Tipo**: integer
- **Valor Atual**: `10000`
- **Descrição**: Máximo de streams por usuário para prevenir explosão de cardinalidade
- **Finalidade**: Previne erros "Maximum active stream limit exceeded"
- **Crítico**: Essencial para ingestão estável de logs com labels de alta cardinalidade

#### limits_config.ingestion_rate_mb
- **Tipo**: integer
- **Valor Atual**: `4`
- **Descrição**: Limite de taxa de ingestão em MB/s
- **Finalidade**: Controla throughput de ingestão de logs para prevenir sobrecarga
- **Impacto**: Logs podem ser rejeitados se taxa for excedida

### Configuração Promtail (promtail-config.yaml)

#### server.http_listen_port
- **Tipo**: integer
- **Valor Atual**: `9080`
- **Descrição**: Porta do servidor HTTP para Promtail
- **Finalidade**: Evita conflitos de porta com outros serviços

#### clients[0].url
- **Tipo**: string
- **Valor Atual**: `http://loki:3100/loki/api/v1/push`
- **Descrição**: Endpoint Loki para envio de logs
- **Finalidade**: Envia logs coletados para Loki
- **Relacionamentos**: Deve corresponder ao nome do serviço e porta do Loki no docker-compose.yml

#### scrape_configs[0].static_configs[0].labels.__path__
- **Tipo**: string
- **Valor Atual**: `/app/logs/wallet-api*.json`
- **Descrição**: Padrão de caminho de arquivo para coleta de logs
- **Finalidade**: Coleta logs JSON estruturados da aplicação
- **Relacionamentos**: Deve corresponder ao caminho de saída do logback-spring.xml e montagem de volume do docker

#### pipeline_stages.json.expressions
- **Tipo**: object
- **Descrição**: Configuração de extração de campos JSON
- **Finalidade**: Extrai campos como traceId, spanId, operation dos logs JSON
- **Crítico**: Essencial para correlação de logs e filtragem no Grafana

#### pipeline_stages.labels
- **Tipo**: object
- **Descrição**: Labels de baixa cardinalidade para streams Loki
- **Finalidade**: Usa apenas level, logger, operation, service, environment para evitar explosão de streams
- **Crítico**: Campos de alta cardinalidade (traceId, spanId) são extraídos mas não usados como labels

### Configuração Prometheus (prometheus.yml)

#### global.scrape_interval
- **Tipo**: duration
- **Valor Atual**: `15s`
- **Descrição**: Intervalo padrão para scraping de métricas
- **Finalidade**: Balanceia frequência de monitoramento com uso de recursos

#### scrape_configs[1].job_name
- **Tipo**: string
- **Valor Atual**: `recargapay-wallet-api`
- **Descrição**: Nome do job para métricas da wallet API
- **Finalidade**: Identifica fonte de métricas no Prometheus

#### scrape_configs[1].static_configs[0].targets
- **Tipo**: array
- **Valor Atual**: `['host.docker.internal:8080']`
- **Descrição**: Endpoints alvo para coleta de métricas
- **Finalidade**: Faz scraping de métricas do endpoint /actuator/prometheus
- **Relacionamentos**: Deve corresponder à porta do servidor da aplicação

---

## Dependências de Configuração

### Relacionamentos Críticos

1. **Cadeia de Conexão com Banco**:
   - spring.datasource.* → Conexão PostgreSQL
   - spring.flyway.* → Gerenciamento de schema
   - spring.jpa.hibernate.ddl-auto=validate → Validação de schema

2. **Cadeia de Tracing**:
   - otel.* → Configuração OpenTelemetry
   - logging.pattern.console → Inclui traceId/spanId
   - TraceContextFilter → Popula MDC
   - Promtail → Extrai traceId/spanId dos logs

3. **Cadeia de Monitoramento**:
   - management.endpoints.* → Expõe endpoints
   - Prometheus scrape_configs → Coleta métricas
   - Grafana datasources → Visualiza dados

4. **Cadeia de Processamento de Logs**:
   - logback-spring.xml → Gera logs JSON
   - Montagem de volume Docker → Torna logs acessíveis
   - Promtail scrape_configs → Coleta logs
   - Loki limits_config → Previne erros de ingestão

### Dependências de Porta

- **8080**: Servidor da aplicação (padrão, comentado na config)
- **3100**: API HTTP Loki
- **9080**: Servidor HTTP Promtail
- **9090**: Interface web Prometheus
- **3000**: Interface web Grafana
- **5432**: Banco PostgreSQL

---

## Configurações Específicas por Ambiente

### Ambiente de Desenvolvimento
- **Logging**: Nível DEBUG, logging SQL detalhado
- **Segurança**: Chave JWT hardcoded
- **Banco**: PostgreSQL local
- **Monitoramento**: Todos endpoints expostos
- **Loki**: Sem autenticação, armazenamento local

### Recomendações para Produção
- **Logging**: Nível INFO, sem logging SQL
- **Segurança**: Gerenciamento externo de secrets
- **Banco**: PostgreSQL externo com pool de conexões
- **Monitoramento**: Exposição restrita de endpoints
- **Loki**: Autenticação habilitada, armazenamento externo

Esta referência de configuração garante que todas as propriedades sejam documentadas e seus relacionamentos compreendidos para operação e manutenção adequada do sistema.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Configuration Reference in English](../en/configuration-reference.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
