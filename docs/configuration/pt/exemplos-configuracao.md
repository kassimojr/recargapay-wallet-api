# Exemplos de Configuração

## Visão Geral

Este documento fornece exemplos práticos de configuração para diferentes cenários, incluindo configurações de desenvolvimento, produção, teste e otimização de performance.

---

## Configurações de Desenvolvimento

### application.yml - Desenvolvimento

```yaml
spring:
  application:
    name: recargapay-wallet-api
  
  # Configuração de banco para desenvolvimento
  datasource:
    url: jdbc:postgresql://localhost:5432/walletdb
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver
  
  # JPA/Hibernate para desenvolvimento
  jpa:
    hibernate:
      ddl-auto: validate  # Sempre validate com Flyway
    show-sql: true        # Útil para debug
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true  # Formata SQL para melhor legibilidade
  
  # Flyway para gerenciamento de schema
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  # Segurança - chave hardcoded para desenvolvimento
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: Q4!z8@pW#r2$Lm9^X7eF%uS6bT1&cV0*Y3jH

# OpenTelemetry - configuração manual
otel:
  sdk:
    disabled: true  # Desabilita auto-configuração
  service:
    name: recargapay-wallet-api
  propagators: tracecontext,baggage

# SpringDoc para documentação
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    path: /swagger-ui.html

# Logging para desenvolvimento
logging:
  level:
    com.recargapay.wallet: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"

# Actuator - todos endpoints expostos para desenvolvimento
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
```

---

## Configurações de Produção

### application.yml - Produção

```yaml
spring:
  application:
    name: recargapay-wallet-api
  
  # Configuração de banco para produção
  datasource:
    url: ${DB_URL:jdbc:postgresql://db-cluster:5432/walletdb}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
  
  # JPA/Hibernate para produção
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false  # Desabilitado em produção
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  # Flyway
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
  
  # Segurança - usando secrets externos
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: ${JWT_SECRET}

# Servidor
server:
  port: 8080
  servlet:
    context-path: /api
  compression:
    enabled: true
  http2:
    enabled: true

# OpenTelemetry
otel:
  sdk:
    disabled: true
  service:
    name: recargapay-wallet-api
    version: ${APP_VERSION:1.0.0}
    environment: ${ENVIRONMENT:production}
  propagators: tracecontext,baggage

# SpringDoc - restrito em produção
springdoc:
  api-docs:
    enabled: ${API_DOCS_ENABLED:false}
  swagger-ui:
    path: /swagger-ui.html

# Logging para produção
logging:
  level:
    root: WARN
    com.recargapay.wallet: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /app/logs/wallet-api.log
    max-size: 100MB
    max-history: 30

# Actuator - endpoints restritos
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
    prometheus:
      enabled: true
  security:
    enabled: true
```

---

## Configurações de Teste

### application-test.yml

```yaml
spring:
  # Banco em memória para testes
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  # JPA para testes
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recria schema para cada teste
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  
  # Flyway desabilitado para testes (usa create-drop)
  flyway:
    enabled: false
  
  # Segurança simplificada para testes
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: test-secret-key-for-testing-only

# OpenTelemetry desabilitado para testes
otel:
  sdk:
    disabled: true

# Logging para testes
logging:
  level:
    com.recargapay.wallet: DEBUG
    org.springframework.test: INFO
    org.testcontainers: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# Actuator para testes
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## Configurações de Monitoramento

### Loki - Alta Disponibilidade

```yaml
auth_enabled: true

server:
  http_listen_port: 3100
  grpc_listen_port: 9096

ingester:
  lifecycler:
    address: 0.0.0.0
    ring:
      kvstore:
        store: consul
        consul:
          host: consul:8500
      replication_factor: 3
    num_tokens: 512
  chunk_idle_period: 3m
  chunk_retain_period: 1m
  max_transfer_retries: 0

schema_config:
  configs:
    - from: 2020-10-24
      store: cassandra
      object_store: s3
      schema: v11
      index:
        prefix: index_
        period: 168h

storage_config:
  cassandra:
    addresses: cassandra1,cassandra2,cassandra3
    port: 9042
    keyspace: loki
    consistency: QUORUM
  
  aws:
    s3: s3://loki-chunks-bucket
    region: us-east-1

limits_config:
  max_streams_per_user: 50000
  ingestion_rate_mb: 16
  ingestion_burst_size_mb: 32
  per_stream_rate_limit: 8MB
  retention_period: 744h  # 31 dias
```

### Promtail - Produção

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /var/lib/promtail/positions.yaml

clients:
  - url: https://loki.example.com/loki/api/v1/push
    basic_auth:
      username: ${LOKI_USERNAME}
      password: ${LOKI_PASSWORD}

scrape_configs:
  - job_name: wallet-api-json-logs
    static_configs:
      - targets:
          - localhost
        labels:
          job: wallet-api
          environment: production
          __path__: /app/logs/wallet-api*.json
    
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            message: message
            logger: logger
            traceId: traceId
            spanId: spanId
            operation: operation
            walletId: walletId
            amount: amount
            userId: userId
      
      - timestamp:
          source: timestamp
          format: RFC3339Nano
      
      - labels:
          level: level
          logger: logger
          operation: operation
          service: service
          environment: environment
      
      - output:
          source: message
```

---

## Otimização de Performance

### Configuração de Pool de Conexões

```yaml
spring:
  datasource:
    hikari:
      # Pool sizing
      maximum-pool-size: 20
      minimum-idle: 5
      
      # Timeouts
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      
      # Performance
      leak-detection-threshold: 60000
      
      # Configurações específicas PostgreSQL
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

### Configuração JPA Otimizada

```yaml
spring:
  jpa:
    properties:
      hibernate:
        # Batch processing
        jdbc:
          batch_size: 20
          batch_versioned_data: true
        order_inserts: true
        order_updates: true
        
        # Cache de segundo nível
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
        
        # Estatísticas
        generate_statistics: false
        
        # Lazy loading
        enable_lazy_load_no_trans: false
```

---

## Configurações de Segurança

### Segurança Endurecida

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: ${JWT_SECRET}
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}

server:
  # SSL/TLS
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
  
  # Security headers
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict

# Actuator com segurança
management:
  endpoints:
    web:
      exposure:
        include: health,info
      base-path: /management
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
    roles: ADMIN
```

---

## Configurações de Ambiente Docker

### docker-compose.yml - Produção

```yaml
version: '3.8'

services:
  wallet-api:
    image: recargapay/wallet-api:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_URL=jdbc:postgresql://postgres:5432/walletdb
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - postgres
      - loki
    networks:
      - wallet-network
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=walletdb
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - wallet-network

  loki:
    image: grafana/loki:2.9.0
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - ./monitoring/loki/loki-config.yaml:/etc/loki/local-config.yaml
      - loki_data:/loki
    networks:
      - wallet-network

volumes:
  postgres_data:
  loki_data:

networks:
  wallet-network:
    driver: bridge
```

---

## Troubleshooting e Debug

### Configuração de Debug Avançado

```yaml
logging:
  level:
    # Debug específico da aplicação
    com.recargapay.wallet: DEBUG
    
    # Debug de segurança
    org.springframework.security: DEBUG
    org.springframework.security.web: DEBUG
    
    # Debug de banco
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    
    # Debug de transações
    org.springframework.transaction: DEBUG
    
    # Debug de OpenTelemetry
    io.opentelemetry: DEBUG
    
    # Debug de HTTP
    org.springframework.web: DEBUG
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"

# Actuator para debug
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    configprops:
      show-values: always
    env:
      show-values: always
```

---

## Configurações de Teste de Carga

### application-load-test.yml

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 5000
      idle-timeout: 600000

logging:
  level:
    root: WARN
    com.recargapay.wallet: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} %-5level - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
```

Estes exemplos fornecem configurações prontas para diferentes cenários e ambientes, facilitando a implantação e manutenção do sistema.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Configuration Examples in English](../en/configuration-examples.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
