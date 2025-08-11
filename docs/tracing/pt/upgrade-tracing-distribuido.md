# Plano de Upgrade para Rastreamento Distribuído Granular

## 📋 Visão Geral

Este documento apresenta um plano detalhado para evoluir a implementação atual de rastreamento (um span por requisição HTTP) para um sistema de **rastreamento distribuído granular** com múltiplos spans por operação.

## 🎯 Objetivos do Upgrade

### Estado Atual
- ✅ Um span por requisição HTTP
- ✅ TraceId único por requisição
- ✅ SpanId único por requisição
- ✅ Correlação básica de logs

### Estado Futuro
- 🎯 Múltiplos spans por requisição
- 🎯 TraceId único por jornada de negócio
- 🎯 SpanId único por operação/método
- 🎯 Instrumentação automática de serviços
- 🎯 Métricas detalhadas de performance
- 🎯 Rastreamento de dependências

## 📊 Estimativa de Complexidade

| Componente | Complexidade | Tempo Estimado | Risco |
|------------|--------------|----------------|-------|
| **Dependências** | Baixa | 0.5 dia | Baixo |
| **Configuração** | Média | 1 dia | Médio |
| **Instrumentação de Serviços** | Alta | 2 dias | Alto |
| **Instrumentação de Repositórios** | Média | 1 dia | Médio |
| **Utilitários de Tracing** | Média | 0.5 dia | Baixo |
| **Testes** | Alta | 1 dia | Alto |
| **Documentação** | Baixa | 0.5 dia | Baixo |

**Total Estimado: 6-7 dias de desenvolvimento**

## 🔧 Mudanças Necessárias

### 1. Dependências (pom.xml)

```xml
<!-- Instrumentação automática Spring Boot -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>1.32.0-alpha</version>
</dependency>

<!-- Instrumentação manual e anotações -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-annotations</artifactId>
    <version>1.32.0</version>
</dependency>

<!-- Micrometer Observation para Spring Boot 3+ -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
```

### 2. Configuração (application.yml)

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0

otel:
  sdk:
    disabled: false
  springboot:
    auto-configuration:
      enabled: true
  instrumentation:
    spring-webmvc:
      enabled: true
    jdbc:
      enabled: true
  propagators: tracecontext,baggage
  resource:
    attributes:
      service.name: wallet-api
      service.version: 1.0.0
```

### 3. Utilitários de Tracing

```java
@Component
public class TracingUtils {
    
    private final Tracer tracer;
    
    /**
     * Executa uma operação dentro de um span customizado
     */
    public <T> T withSpan(String spanName, Supplier<T> operation) {
        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();
            
        try (Scope scope = span.makeCurrent()) {
            return operation.get();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Adiciona atributos ao span atual
     */
    public void addSpanAttributes(Map<String, Object> attributes) {
        Span currentSpan = Span.current();
        if (currentSpan.isRecording()) {
            attributes.forEach((key, value) -> {
                if (value instanceof String) {
                    currentSpan.setAttribute(key, (String) value);
                } else if (value instanceof Long) {
                    currentSpan.setAttribute(key, (Long) value);
                } else {
                    currentSpan.setAttribute(key, String.valueOf(value));
                }
            });
        }
    }
}
```

### 4. Instrumentação de Serviços

```java
@Service
@Observed(name = "deposit.service")
public class DepositService {
    
    private final TracingUtils tracingUtils;
    
    @WithSpan("deposit.validate")
    public void validateDeposit(DepositRequestDTO request) {
        tracingUtils.addSpanAttributes(Map.of(
            "wallet.id", request.getWalletId().toString(),
            "deposit.amount", request.getAmount().doubleValue()
        ));
        
        // Lógica de validação...
    }
    
    @WithSpan("deposit.process")
    public DepositResponseDTO processDeposit(DepositRequestDTO request) {
        return tracingUtils.withSpan("deposit.business-logic", () -> {
            
            // 1. Validar requisição
            validateDeposit(request);
            
            // 2. Buscar carteira
            Wallet wallet = tracingUtils.withSpan("deposit.fetch-wallet", () -> {
                return walletRepository.findById(request.getWalletId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
            });
            
            // 3. Processar depósito
            BigDecimal newBalance = tracingUtils.withSpan("deposit.calculate-balance", () -> {
                return wallet.getBalance().add(request.getAmount());
            });
            
            // 4. Salvar alterações
            tracingUtils.withSpan("deposit.save-wallet", () -> {
                wallet.setBalance(newBalance);
                walletRepository.save(wallet);
            });
            
            return new DepositResponseDTO(wallet.getId(), newBalance);
        });
    }
}
```

## 📊 Resultados Esperados

### Antes (Estado Atual)
```
Requisição HTTP POST /api/v1/wallets/deposit
└── Span: POST /api/v1/wallets/deposit (traceId: abc123, spanId: def456)
    ├── Log: TraceContextFilter (traceId: abc123, spanId: def456)
    ├── Log: DepositService (traceId: abc123, spanId: def456)
    └── Log: WalletRepository (traceId: abc123, spanId: def456)
```

### Depois (Estado Futuro)
```
Requisição HTTP POST /api/v1/wallets/deposit
└── Root Span: HTTP POST /api/v1/wallets/deposit (traceId: abc123, spanId: def456)
    ├── Child Span: deposit.validate (traceId: abc123, spanId: ghi789)
    ├── Child Span: deposit.process (traceId: abc123, spanId: jkl012)
    │   ├── Child Span: deposit.fetch-wallet (traceId: abc123, spanId: mno345)
    │   ├── Child Span: deposit.calculate-balance (traceId: abc123, spanId: pqr678)
    │   └── Child Span: deposit.save-wallet (traceId: abc123, spanId: stu901)
    └── Logs com traceId consistente mas spanIds únicos por operação
```

## 🧪 Estratégia de Testes

### Testes Unitários
```java
@Test
void shouldCreateSpansForDepositOperation() {
    // Given
    DepositRequestDTO request = new DepositRequestDTO(UUID.randomUUID(), BigDecimal.valueOf(100.00));
    
    // When
    depositService.processDeposit(request);
    
    // Then
    List<SpanData> spans = spanExporter.getFinishedSpanItems();
    assertThat(spans).hasSize(5); // Root + 4 child spans
    assertThat(spans).extracting(SpanData::getName)
        .contains("deposit.process", "deposit.validate", "deposit.fetch-wallet");
}
```

## 📈 Impacto na Performance

| Componente | Overhead Estimado | Impacto |
|------------|------------------|---------|
| **Criação de Spans** | 0.1-0.5ms por span | Baixo |
| **Atributos de Span** | 0.01ms por atributo | Mínimo |
| **Total por Requisição** | 2-5ms | Aceitável |

## 🚀 Plano de Migração

### Fase 1: Preparação (1 dia)
- [ ] Atualizar dependências no pom.xml
- [ ] Configurar OpenTelemetry avançado
- [ ] Criar utilitários de tracing

### Fase 2: Instrumentação Básica (2 dias)
- [ ] Instrumentar camada de serviços
- [ ] Adicionar spans em operações críticas
- [ ] Validar geração de spans

### Fase 3: Instrumentação Avançada (2 dias)
- [ ] Instrumentar repositórios
- [ ] Adicionar spans de banco de dados
- [ ] Instrumentar controllers

### Fase 4: Validação e Otimização (1 dia)
- [ ] Testes de performance
- [ ] Ajustes de configuração
- [ ] Validação em ambiente de teste

## ⚠️ Riscos e Mitigações

### Riscos Identificados
1. **Overhead de Performance**: Múltiplos spans podem impactar performance
2. **Complexidade de Debugging**: Mais spans = mais complexidade
3. **Conflitos de Dependências**: Instrumentação automática pode conflitar

### Estratégias de Mitigação
1. **Sampling Configurável**: Reduzir overhead em produção
2. **Instrumentação Seletiva**: Apenas operações críticas
3. **Testes Extensivos**: Validar antes do deploy

## 📚 Recursos Adicionais

- [Documentação OpenTelemetry Java](https://opentelemetry.io/docs/languages/java/)
- [Spring Boot Observability](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.observability)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)

---

*Este plano fornece um roadmap detalhado para implementar rastreamento distribuído granular, mantendo a estabilidade e performance da aplicação.*

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Distributed Tracing Upgrade in English](../en/distributed-tracing-upgrade.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
