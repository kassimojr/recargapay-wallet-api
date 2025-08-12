# Implementação Atual de Rastreamento Distribuído

## 📋 Visão Geral

A implementação atual de rastreamento distribuído na Recargapay Wallet API fornece **correlação básica de logs** através da geração de `traceId` e `spanId` únicos para cada requisição HTTP. Esta abordagem permite rastrear o fluxo completo de uma transação através dos logs estruturados, facilitando debugging, monitoramento e suporte ao cliente.

## 🏗️ Arquitetura

### Componentes Principais

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Request  │───▶│ TraceContextFilter│───▶│  Application    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │ OpenTelemetry SDK│    │ Structured Logs │
                       └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │  Span Context    │    │   Loki/Grafana  │
                       └──────────────────┘    └─────────────────┘
```

### Fluxo de Requisição

1. **Requisição HTTP chega** ao `TraceContextFilter`
2. **Span é criado** com traceId e spanId únicos
3. **MDC é populado** com os identificadores
4. **Requisição prossegue** através da aplicação
5. **Logs são gerados** com traceId/spanId incluídos
6. **Span é finalizado** ao fim da requisição
7. **MDC é limpo** para evitar vazamentos

## 🔧 Componentes Técnicos

### 1. TraceContextFilter

**Localização**: `src/main/java/com/recargapay/wallet/infra/logging/TraceContextFilter.java`

```java
@Component
public class TraceContextFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);
    private final Tracer tracer;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Extrair contexto de tracing de headers HTTP (se existir)
        Context parentContext = GlobalOpenTelemetry.getPropagators()
            .getTextMapPropagator()
            .extract(Context.current(), request, HttpServletRequestGetter.INSTANCE);
        
        // Criar span representando esta requisição HTTP
        Span serverSpan = tracer.spanBuilder(request.getMethod() + " " + request.getRequestURI())
                .setParent(parentContext)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        try (Scope scope = serverSpan.makeCurrent()) {
            SpanContext spanContext = serverSpan.getSpanContext();
            String traceId = spanContext.getTraceId();
            String spanId = spanContext.getSpanId();

            // Fallback para IDs baseados em UUID se necessário
            if ("00000000000000000000000000000000".equals(traceId)) {
                traceId = generateTraceId();
            }
            if ("0000000000000000".equals(spanId)) {
                spanId = generateSpanId();
            }

            // Popular MDC para logging
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);

            log.debug("Trace context set: traceId={}, spanId={}", traceId, spanId);

            // Continuar processamento da requisição
            filterChain.doFilter(request, response);
            
        } finally {
            // Limpar MDC e finalizar span
            MDC.remove("traceId");
            MDC.remove("spanId");
            serverSpan.end();
        }
    }
}
```

**Características**:
- **Um span por requisição HTTP**: Simplicidade e confiabilidade
- **Geração de fallback**: UUIDs se OpenTelemetry falhar
- **Gestão de lifecycle**: Criação, ativação e limpeza adequadas
- **Propagação de contexto**: Suporte a headers W3C Trace Context

### 2. OpenTelemetryConfig

**Localização**: `src/main/java/com/recargapay/wallet/config/OpenTelemetryConfig.java`

```java
@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
            .merge(Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, "wallet-api")
                .put(ResourceAttributes.SERVICE_VERSION, "1.0.0")
                .build());

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setTextMapPropagator(TextMapPropagator.composite(
                W3CTraceContextPropagator.getInstance(),
                W3CBaggagePropagator.getInstance()
            ))
            .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("wallet-api", "1.0.0");
    }
}
```

**Características**:
- **Configuração manual**: Evita conflitos de auto-configuração
- **Sem exportador OTLP**: Previne erros de conexão
- **Propagadores W3C**: Padrão da indústria
- **Resource attributes**: Identificação do serviço

### 3. Configuração de Logging (Logback)

**Localização**: `src/main/resources/logback-spring.xml`

```xml
<configuration>
    <springProfile name="!prod">
        <!-- Console Appender para desenvolvimento -->
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <mdc/>
                    <message/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>

    <springProfile name="prod">
        <!-- File Appender para produção -->
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/wallet-api.json</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/wallet-api-%d{yyyy-MM-dd}.json</fileNamePattern>
                <maxHistory>7</maxHistory>
                <totalSizeCap>1GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <mdc/>
                    <message/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>

    <root level="INFO">
        <springProfile name="!prod">
            <appender-ref ref="CONSOLE"/>
        </springProfile>
        <springProfile name="prod">
            <appender-ref ref="FILE"/>
        </springProfile>
    </root>
</configuration>
```

**Características**:
- **Logs estruturados JSON**: Facilita parsing e análise
- **Inclusão automática do MDC**: traceId e spanId em todos os logs
- **Rotação diária**: Gestão automática de arquivos
- **Retenção configurável**: 7 dias ou 1GB total

### 4. Configuração da Aplicação

**Localização**: `src/main/resources/application.yml`

```yaml
management:
  tracing:
    enabled: false
    sampling:
      probability: 1.0

otel:
  sdk:
    disabled: true
  springboot:
    auto-configuration:
      enabled: false
  propagators: tracecontext,baggage
  exporter:
    otlp:
      enabled: false
    metrics:
      enabled: false
    logs:
      enabled: false
  resource:
    attributes:
      service.name: wallet-api
      service.version: 1.0.0
```

**Características**:
- **Auto-configuração desabilitada**: Evita conflitos
- **Propagadores W3C**: Padrão da indústria
- **Exportadores desabilitados**: Operação local apenas
- **Atributos de resource**: Identificação do serviço

## 📊 Exemplo de Logs Gerados

### Log de Requisição HTTP
```json
{
  "timestamp": "2025-07-22T06:45:32.123Z",
  "level": "DEBUG",
  "logger": "c.r.w.i.logging.TraceContextFilter",
  "traceId": "6ed5c3ad90a37e0437be0bf3f15cb9d6",
  "spanId": "6425741b3b8a7581",
  "message": "Trace context set: traceId=6ed5c3ad90a37e0437be0bf3f15cb9d6, spanId=6425741b3b8a7581"
}
```

### Log de Operação de Negócio
```json
{
  "timestamp": "2025-07-22T06:45:32.145Z",
  "level": "INFO",
  "logger": "c.r.w.core.services.DepositService",
  "traceId": "6ed5c3ad90a37e0437be0bf3f15cb9d6",
  "spanId": "6425741b3b8a7581",
  "message": "Processing deposit",
  "operation": "DEPOSIT",
  "walletId": "12345678-1234-1234-1234-123456789012",
  "amount": 100.00,
  "userId": "user123"
}
```

### Log de Resposta HTTP
```json
{
  "timestamp": "2025-07-22T06:45:32.167Z",
  "level": "INFO",
  "logger": "c.r.w.i.logging.ApiLoggingInterceptor",
  "traceId": "6ed5c3ad90a37e0437be0bf3f15cb9d6",
  "spanId": "6425741b3b8a7581",
  "message": "API request completed",
  "operation": "API_REQUEST_COMPLETED",
  "method": "POST",
  "path": "/api/v1/wallets/deposit",
  "status": 200,
  "duration": 44
}
```

## ✅ Vantagens da Implementação Atual

### 1. **Simplicidade**
- Configuração mínima necessária
- Fácil de entender e manter
- Baixo risco de problemas

### 2. **Confiabilidade**
- Um span por requisição = comportamento previsível
- Fallback para UUID se OpenTelemetry falhar
- Gestão adequada de lifecycle

### 3. **Performance**
- Overhead mínimo (< 5ms por requisição)
- Sem exportação remota = sem latência de rede
- Operação totalmente local

### 4. **Correlação Efetiva**
- Todos os logs de uma requisição têm o mesmo traceId
- Fácil busca e agregação no Loki/Grafana
- Suporte completo a debugging

### 5. **Compatibilidade**
- Padrões W3C Trace Context
- Integração com ferramentas padrão da indústria
- Preparado para evolução futura

## ⚠️ Limitações Conhecidas

### 1. **Granularidade Limitada**
- **Problema**: Apenas um span por requisição HTTP
- **Impacto**: Não identifica gargalos internos específicos
- **Workaround**: Logs detalhados em pontos críticos

### 2. **Sem Instrumentação Automática**
- **Problema**: Não rastreia chamadas de banco, HTTP, etc.
- **Impacto**: Visibilidade limitada de dependências
- **Workaround**: Logging manual em integrações

### 3. **SpanId Constante**
- **Problema**: Mesmo spanId em toda a requisição
- **Impacto**: Não diferencia etapas internas
- **Workaround**: Usar campos customizados nos logs

### 4. **Sem Métricas Automáticas**
- **Problema**: Não gera métricas de tracing automaticamente
- **Impacto**: Dependência de logs para análise
- **Workaround**: Métricas customizadas via Micrometer

## 🎯 Casos de Uso Ideais

### ✅ **Funciona Bem Para:**
- Debugging de transações específicas
- Correlação de logs por requisição
- Suporte ao cliente (rastrear operações)
- Monitoramento básico de performance
- Análise de padrões de erro
- Auditoria de operações

### ❌ **Não Adequado Para:**
- Análise detalhada de performance interna
- Rastreamento de dependências complexas
- APM (Application Performance Monitoring) avançado
- Otimização granular de código
- Distributed tracing entre microsserviços

## 🔧 Configuração e Uso

### Pré-requisitos
```xml
<!-- Dependências necessárias no pom.xml -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.32.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>1.32.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-mdc-1.0</artifactId>
    <version>1.32.0-alpha</version>
</dependency>
```

### Verificação de Funcionamento
```bash
# 1. Iniciar aplicação
./mvnw spring-boot:run

# 2. Fazer uma requisição
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Content-Type: application/json" \
  -d '{"walletId": "12345678-1234-1234-1234-123456789012", "amount": 100.00}'

# 3. Verificar logs
tail -f logs/wallet-api.json | jq '.traceId, .spanId, .message'

# 4. Buscar por traceId específico
grep "6ed5c3ad90a37e0437be0bf3f15cb9d6" logs/wallet-api.json | jq .
```

### Consultas Loki Básicas
```logql
# Todos os logs de um trace
{job="wallet-api"} |= "6ed5c3ad90a37e0437be0bf3f15cb9d6"

# Logs de erro com traceId
{job="wallet-api"} | json | level="ERROR" | traceId!=""

# Timeline de uma requisição
{job="wallet-api"} | json | traceId="6ed5c3ad90a37e0437be0bf3f15cb9d6" | line_format "{{.timestamp}} {{.logger}} : {{.message}}"
```

## 🚀 Próximos Passos

Para evoluir para rastreamento distribuído granular, consulte:
- **[Plano de Upgrade](./upgrade-tracing-distribuido.md)** - Roadmap completo
- **[Solução de Problemas](./solucao-problemas.md)** - Troubleshooting
- **[Consultas e Monitoramento](./consultas-monitoramento.md)** - Queries avançadas

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Current Implementation in English](../en/current-implementation.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
