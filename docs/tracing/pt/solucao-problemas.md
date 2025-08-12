# Guia de Solução de Problemas - Rastreamento Distribuído

## 🔍 Problemas Comuns e Soluções

### 1. TraceId/SpanId Aparecendo como Zeros

**Sintomas:**
```
DEBUG [wallet-api] [00000000000000000000000000000000,0000000000000000] [http-nio-8080-exec-1] c.r.w.i.logging.TraceContextFilter
```

**Possíveis Causas:**
- OpenTelemetry SDK não inicializado corretamente
- TraceContextFilter não criando spans adequadamente
- MDC não sendo populado

**Soluções:**
1. **Verificar Criação de Beans OpenTelemetry:**
   ```bash
   # Verificar logs de inicialização da aplicação
   grep -i "opentelemetry" logs/wallet-api*.log
   ```

2. **Verificar Registro do TraceContextFilter:**
   ```bash
   # Verificar se o filtro está sendo chamado
   grep "TraceContextFilter" logs/wallet-api*.log
   ```

3. **Validar Criação de Spans:**
   ```java
   // Adicionar logging de debug no TraceContextFilter
   log.debug("Span context válido: {}", serverSpan.getSpanContext().isValid());
   log.debug("TraceId gerado: {}, spanId: {}", traceId, spanId);
   ```

### 2. TraceId/SpanId Ausentes nos Logs

**Sintomas:**
```
DEBUG [wallet-api] [,] [http-nio-8080-exec-1] c.r.w.core.services.DepositService
```

**Possíveis Causas:**
- MDC não configurado adequadamente no TraceContextFilter
- Contexto de thread não propagado
- Configuração do Logback perdendo campos MDC

**Soluções:**
1. **Verificar População do MDC:**
   ```java
   // Adicionar logging de debug no TraceContextFilter
   log.debug("MDC traceId: {}, spanId: {}", MDC.get("traceId"), MDC.get("spanId"));
   ```

2. **Verificar Configuração do Logback:**
   ```xml
   <!-- Garantir que o pattern inclui campos MDC -->
   <pattern>[%X{traceId:-},%X{spanId:-}]</pattern>
   ```

### 3. Falhas na Inicialização da Aplicação

**Sintomas:**
```
Failed to instantiate [io.opentelemetry.api.OpenTelemetry]: Factory method 'openTelemetry' threw exception
```

**Possíveis Causas:**
- Dependências OpenTelemetry ausentes
- Versões conflitantes do OpenTelemetry
- Configuração inválida

**Soluções:**
1. **Verificar Dependências:**
   ```bash
   # Verificar se todas as dependências necessárias estão presentes
   ./mvnw dependency:tree | grep opentelemetry
   ```

2. **Verificar Compatibilidade de Versões:**
   ```xml
   <!-- Garantir versão consistente do OpenTelemetry -->
   <opentelemetry.version>1.32.0</opentelemetry.version>
   ```

### 4. Problemas de Performance

**Sintomas:**
- Aumento no tempo de resposta
- Alto uso de CPU
- Vazamentos de memória

**Possíveis Causas:**
- Muitos spans sendo criados
- Spans não sendo fechados adequadamente
- Memória não sendo liberada

**Soluções:**
1. **Monitorar Criação de Spans:**
   ```java
   // Adicionar métricas para rastrear criação de spans
   private final Counter spanCreationCounter = Counter.builder("spans.created").register(meterRegistry);
   ```

2. **Verificar Limpeza de Spans:**
   ```java
   // Garantir que spans sejam sempre fechados em blocos finally
   try (Scope scope = span.makeCurrent()) {
       // ... operação
   } finally {
       span.end(); // Sempre fechar spans
   }
   ```

### 5. Erros do Exportador OTLP

**Sintomas:**
```
ERROR [wallet-api] [,] [OkHttp http://localhost:4317/...] i.o.e.internal.http.HttpExporter : Failed to export spans
```

**Possíveis Causas:**
- OpenTelemetry Collector não está rodando
- Configuração incorreta do endpoint OTLP
- Problemas de conectividade de rede

**Soluções:**
1. **Desabilitar Exportador OTLP (Implementação Atual):**
   ```java
   // Remover exportador OTLP da configuração
   SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
       .setResource(resource)
       // Sem processadores de span = apenas local
       .build();
   ```

## 🔧 Ferramentas de Debugging

### 1. Comandos de Análise de Logs

```bash
# Encontrar todos os logs para um trace específico
grep "traceId_aqui" logs/wallet-api*.json

# Contar IDs de trace únicos nos logs
cat logs/wallet-api.json | jq -r '.traceId' | sort | uniq | wc -l

# Encontrar traces com erros
cat logs/wallet-api.json | jq 'select(.level == "ERROR")' | jq -r '.traceId'

# Analisar distribuição de traces
cat logs/wallet-api.json | jq -r '.traceId' | sort | uniq -c | sort -nr
```

### 2. Verificações de Saúde da Aplicação

```bash
# Verificar se o filtro de tracing está ativo
curl -v http://localhost:8080/actuator/health

# Verificar métricas do OpenTelemetry
curl http://localhost:8080/actuator/prometheus | grep otel

# Testar geração de trace
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"walletId": "12345678-1234-1234-1234-123456789012", "amount": 100.00}'
```

### 3. Monitoramento da JVM

```bash
# Monitorar threads da JVM
jstack <pid> | grep -A 5 -B 5 "TraceContext"

# Verificar uso de memória
jmap -histo <pid> | grep -i span

# Monitorar atividade do GC
jstat -gc <pid> 1s 10
```

## 📊 Monitoramento e Alertas

### 1. Métricas Principais para Monitorar

```yaml
# Métricas Prometheus para rastrear
metrics:
  - name: trace_generation_rate
    description: Taxa de geração de trace IDs
    query: rate(traces_created_total[5m])
  
  - name: span_creation_rate
    description: Taxa de criação de spans
    query: rate(spans_created_total[5m])
  
  - name: trace_context_errors
    description: Erros na propagação de contexto de trace
    query: rate(trace_context_errors_total[5m])
```

### 2. Regras de Alerta

```yaml
# Alerta se a geração de traces parar
- alert: TracingDown
  expr: rate(traces_created_total[5m]) == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Rastreamento distribuído não está gerando traces"

# Alerta para muitos erros de contexto
- alert: TraceContextErrors
  expr: rate(trace_context_errors_total[5m]) > 0.1
  for: 1m
  labels:
    severity: warning
  annotations:
    summary: "Alta taxa de erros de contexto de trace"
```

## 🚨 Procedimentos de Emergência

### 1. Desabilitar Tracing Rapidamente

Se o tracing estiver causando problemas, desabilite rapidamente:

```java
// Opção 1: Desabilitar registro do filtro
@ConditionalOnProperty(name = "tracing.enabled", havingValue = "true", matchIfMissing = false)
@Component
public class TraceContextFilter extends OncePerRequestFilter {
    // ... implementação do filtro
}
```

```yaml
# Opção 2: Desabilitar via configuração
tracing:
  enabled: false
```

### 2. Rollback para Versão Anterior

```bash
# Rollback rápido usando Git
git revert <hash-do-commit-de-tracing>
git push origin main

# Reimplantar aplicação
./mvnw clean package
java -jar target/recargapay-wallet-api-*.jar
```

### 3. Soluções Temporárias

```java
// Temporário: Usar trace ID estático se a geração falhar
private String generateTraceId() {
    try {
        UUID uuid = UUID.randomUUID();
        return String.format("%016x%016x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    } catch (Exception e) {
        log.warn("Falha ao gerar trace ID, usando fallback", e);
        return "fallback-trace-" + System.currentTimeMillis();
    }
}
```

## 📋 Checklist de Verificação de Saúde

### Verificações Diárias
- [ ] Verificar se trace IDs estão sendo gerados (não zeros)
- [ ] Verificar logs de erro relacionados ao tracing
- [ ] Monitorar métricas de performance da aplicação
- [ ] Validar se a correlação de logs está funcionando

### Verificações Semanais
- [ ] Revisar padrões de geração de traces
- [ ] Analisar impacto na performance
- [ ] Verificar vazamentos de memória
- [ ] Atualizar dashboards de monitoramento

### Verificações Mensais
- [ ] Revisar e atualizar documentação
- [ ] Analisar dados de trace para insights
- [ ] Planejar otimizações se necessário
- [ ] Atualizar regras de alerta

## 📞 Procedimentos de Escalação

### Nível 1: Problemas da Aplicação
1. Verificar logs da aplicação
2. Validar configuração
3. Reiniciar aplicação se necessário
4. Monitorar para resolução

### Nível 2: Problemas de Performance
1. Analisar métricas de performance
2. Verificar utilização de recursos
3. Considerar desabilitação temporária do tracing
4. Planejar otimização

### Nível 3: Problemas Sistêmicos
1. Escalar para equipe de infraestrutura
2. Considerar procedimentos de rollback
3. Implementar correções emergenciais
4. Revisão pós-incidente

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Troubleshooting in English](../en/troubleshooting.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
