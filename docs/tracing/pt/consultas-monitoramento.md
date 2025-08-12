# Guia de Consultas e Monitoramento

## 📊 Consultas Loki para Correlação de Logs

### Correlação Básica de Traces

#### Encontrar Todos os Logs para um Trace Específico
```logql
{job="wallet-api"} |= "6ed5c3ad90a37e0437be0bf3f15cb9d6"
```

#### Encontrar Todos os Logs para um Span Específico
```logql
{job="wallet-api"} |= "6425741b3b8a7581"
```

#### Filtrar por Tipo de Operação
```logql
{job="wallet-api"} | json | operation="DEPOSIT"
```

### Consultas Avançadas

#### Timeline de Trace para uma Requisição
```logql
{job="wallet-api"} | json 
| traceId="6ed5c3ad90a37e0437be0bf3f15cb9d6" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

#### Apenas Traces com Erro
```logql
{job="wallet-api"} | json 
| level="ERROR" 
| line_format "TraceId: {{.traceId}} - {{.message}}"
```

#### Análise de Performance por Endpoint
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_RECEIVED" 
| path="/api/v1/wallets/deposit"
| line_format "{{.timestamp}} - {{.traceId}} - {{.method}} {{.path}}"
```

#### Encontrar Transações Demoradas
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| duration > 1000
| line_format "Requisição lenta: {{.traceId}} levou {{.duration}}ms"
```

### Consultas de Lógica de Negócio

#### Operações de Depósito
```logql
{job="wallet-api"} | json 
| operation="DEPOSIT" 
| line_format "{{.timestamp}} - Depósito: {{.walletId}} valor={{.amount}} trace={{.traceId}}"
```

#### Operações de Saque
```logql
{job="wallet-api"} | json 
| operation="WITHDRAW" 
| line_format "{{.timestamp}} - Saque: {{.walletId}} valor={{.amount}} trace={{.traceId}}"
```

#### Operações de Transferência
```logql
{job="wallet-api"} | json 
| operation="TRANSFER" 
| line_format "{{.timestamp}} - Transferência: {{.fromWalletId}} -> {{.toWalletId}} valor={{.amount}} trace={{.traceId}}"
```

#### Operações que Falharam
```logql
{job="wallet-api"} | json 
| level="ERROR" 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER"
| line_format "Falha {{.operation}}: {{.traceId}} - {{.message}}"
```

## 📈 Painéis do Dashboard Grafana

### 1. Painel de Volume de Requisições

```json
{
  "title": "Volume de Requisições por Trace",
  "type": "stat",
  "targets": [
    {
      "expr": "count by (traceId) (count_over_time({job=\"wallet-api\"} | json [5m]))",
      "legendFormat": "Requisições por Trace"
    }
  ]
}
```

### 2. Painel de Distribuição de Traces

```json
{
  "title": "Distribuição de Traces",
  "type": "piechart",
  "targets": [
    {
      "expr": "count by (operation) (count_over_time({job=\"wallet-api\"} | json | operation!=\"\" [1h]))",
      "legendFormat": "{{operation}}"
    }
  ]
}
```

### 3. Painel de Taxa de Erro por Trace

```json
{
  "title": "Taxa de Erro por Trace",
  "type": "timeseries",
  "targets": [
    {
      "expr": "rate(count_over_time({job=\"wallet-api\"} | json | level=\"ERROR\" [5m]))",
      "legendFormat": "Taxa de Erro"
    }
  ]
}
```

## 🔍 Fluxos de Debugging

### 1. Investigar Requisição com Falha

**Passo 1: Encontrar o erro**
```logql
{job="wallet-api"} | json | level="ERROR" | line_format "{{.timestamp}} {{.traceId}} {{.message}}"
```

**Passo 2: Obter contexto completo do trace**
```logql
{job="wallet-api"} | json | traceId="<trace_id_do_erro>" | line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

**Passo 3: Analisar fluxo da requisição**
```logql
{job="wallet-api"} | json 
| traceId="<trace_id_do_erro>" 
| operation=~"API_REQUEST_RECEIVED|API_REQUEST_COMPLETED"
| line_format "{{.timestamp}} {{.operation}} {{.method}} {{.path}} {{.duration}}ms"
```

### 2. Investigação de Performance

**Passo 1: Encontrar requisições lentas**
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| duration > 2000
| line_format "Lenta: {{.traceId}} {{.path}} levou {{.duration}}ms"
```

**Passo 2: Analisar trace lento**
```logql
{job="wallet-api"} | json 
| traceId="<trace_id_lento>" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

**Passo 3: Identificar gargalo**
```logql
{job="wallet-api"} | json 
| traceId="<trace_id_lento>" 
| logger=~".*Repository.*|.*Service.*|.*Controller.*"
| line_format "{{.timestamp}} {{.logger}} : {{.message}}"
```

### 3. Análise de Lógica de Negócio

**Passo 1: Encontrar operações específicas de carteira**
```logql
{job="wallet-api"} | json 
| walletId="12345678-1234-1234-1234-123456789012"
| line_format "{{.timestamp}} {{.operation}} {{.amount}} trace={{.traceId}}"
```

**Passo 2: Rastrear mudanças no saldo da carteira**
```logql
{job="wallet-api"} | json 
| walletId="12345678-1234-1234-1234-123456789012" 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER"
| line_format "{{.timestamp}} {{.operation}} valor={{.amount}} saldo={{.newBalance}}"
```

## 📊 Alertas de Monitoramento

### 1. Alertas Baseados no Loki

#### Alerta de Alta Taxa de Erro
```yaml
- alert: HighErrorRate
  expr: |
    (
      sum(rate(loki_request_duration_seconds_count{status_code=~"5.."}[5m])) 
      / 
      sum(rate(loki_request_duration_seconds_count[5m]))
    ) > 0.05
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "Alta taxa de erro detectada na wallet API"
    description: "Taxa de erro é {{ $value | humanizePercentage }}"
```

#### Alerta de Trace IDs Ausentes
```yaml
- alert: MissingTraceIds
  expr: |
    sum(rate(count_over_time({job="wallet-api"} | json | traceId="" [5m]))) > 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Trace IDs estão ausentes dos logs"
    description: "{{ $value }} logs por segundo estão sem trace IDs"
```

#### Alerta de Requisições Lentas
```yaml
- alert: SlowRequests
  expr: |
    sum(rate(count_over_time({job="wallet-api"} | json | operation="API_REQUEST_COMPLETED" | duration > 5000 [5m]))) > 0.1
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "Requisições lentas detectadas"
    description: "{{ $value }} requisições lentas por segundo (>5s)"
```

## 🎯 Exemplos de Casos de Uso

### 1. Cenário de Suporte ao Cliente

**Cliente reporta**: "Meu depósito não funcionou às 10:30"

**Passos de investigação**:
```logql
# Passo 1: Encontrar depósitos próximos a esse horário
{job="wallet-api"} | json 
| operation="DEPOSIT" 
| timestamp >= "2025-07-22T10:25:00Z" 
| timestamp <= "2025-07-22T10:35:00Z"
| line_format "{{.timestamp}} {{.walletId}} {{.amount}} {{.traceId}}"

# Passo 2: Verificar erros nesse período
{job="wallet-api"} | json 
| level="ERROR" 
| timestamp >= "2025-07-22T10:25:00Z" 
| timestamp <= "2025-07-22T10:35:00Z"
| line_format "{{.timestamp}} {{.traceId}} {{.message}}"

# Passo 3: Análise completa do trace suspeito
{job="wallet-api"} | json 
| traceId="<trace_id_suspeito>" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

### 2. Otimização de Performance

**Objetivo**: Identificar endpoints mais lentos

**Consultas de análise**:
```logql
# Passo 1: Encontrar endpoints mais lentos
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| line_format "{{.path}} {{.duration}}" 
| pattern "<path> <duration>" 
| duration > 1000

# Passo 2: Analisar endpoint lento específico
{job="wallet-api"} | json 
| path="/api/v1/wallets/transfer" 
| operation="API_REQUEST_COMPLETED" 
| duration > 2000
| line_format "{{.timestamp}} {{.traceId}} levou {{.duration}}ms"

# Passo 3: Análise profunda de traces lentos
{job="wallet-api"} | json 
| traceId="<trace_id_lento>" 
| logger=~".*Service.*|.*Repository.*"
| line_format "{{.timestamp}} {{.logger}} : {{.message}}"
```

### 3. Monitoramento de Segurança

**Objetivo**: Monitorar atividade suspeita

**Consultas de segurança**:
```logql
# Múltiplas tentativas de falha do mesmo usuário
{job="wallet-api"} | json 
| level="ERROR" 
| userId!="" 
| line_format "{{.timestamp}} {{.userId}} {{.message}}"

# Transações de alto valor
{job="wallet-api"} | json 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER" 
| amount > 10000
| line_format "{{.timestamp}} {{.operation}} alto valor: {{.amount}} trace={{.traceId}}"

# Padrões de acesso incomuns
{job="wallet-api"} | json 
| operation="API_REQUEST_RECEIVED" 
| client_ip!="" 
| line_format "{{.timestamp}} {{.client_ip}} {{.path}} {{.traceId}}"
```

## 📋 Melhores Práticas

### 1. Otimização de Consultas

- **Use intervalos de tempo específicos**: Sempre limite consultas a períodos relevantes
- **Filtre cedo**: Aplique filtros o mais cedo possível na consulta
- **Use parsing estruturado**: Aproveite o parsing JSON para melhor performance
- **Limite conjuntos de resultados**: Use `| limit 100` para grandes conjuntos de resultados

### 2. Design de Dashboard

- **Agrupe métricas relacionadas**: Organize painéis por função de negócio
- **Use visualizações apropriadas**: Tabelas para detalhes, gráficos para tendências
- **Defina intervalos de tempo significativos**: Padrão para horários comerciais relevantes
- **Adicione capacidades de drill-down**: Vincule painéis a visualizações detalhadas

### 3. Estratégia de Alertas

- **Defina limites apropriados**: Baseado em dados históricos e requisitos de negócio
- **Use múltiplos níveis de severidade**: Crítico, aviso, informativo
- **Inclua contexto nos alertas**: Forneça trace IDs e timestamps relevantes
- **Teste condições de alerta**: Verifique regularmente se os alertas disparam corretamente

---

*Este guia fornece capacidades abrangentes de consulta e monitoramento para a implementação atual de tracing. Para cenários avançados, consulte [upgrade-tracing-distribuido.md](./upgrade-tracing-distribuido.md).*

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Queries and Monitoring in English](../en/queries-and-monitoring.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
