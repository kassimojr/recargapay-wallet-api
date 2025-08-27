# Documentação de Rastreamento Distribuído

## 📋 Visão Geral

Esta documentação descreve a implementação de rastreamento distribuído (distributed tracing) na Digital Wallet API. O sistema atual fornece correlação básica de logs através de `traceId` e `spanId` únicos por requisição HTTP, permitindo rastrear o fluxo completo de uma transação através dos logs estruturados.

## 📁 Estrutura da Documentação

### 📖 Guias Principais
- **[implementacao-atual.md](./implementacao-atual.md)** - Documentação detalhada da implementação atual
- **[upgrade-tracing-distribuido.md](./upgrade-tracing-distribuido.md)** - Plano completo para upgrade para rastreamento granular
- **[solucao-problemas.md](./solucao-problemas.md)** - Guia de troubleshooting e solução de problemas
- **[consultas-monitoramento.md](./consultas-monitoramento.md)** - Queries Loki, dashboards Grafana e monitoramento

### 🔄 Estado Atual vs Futuro

| Aspecto | **Implementação Atual** | **Upgrade Futuro** |
|---------|------------------------|-------------------|
| **Granularidade** | 1 span por requisição HTTP | Múltiplos spans por operação |
| **TraceId** | Único por requisição | Único por jornada de negócio |
| **SpanId** | Único por requisição | Único por operação/método |
| **Instrumentação** | Manual via filtro | Automática + manual |
| **Complexidade** | Baixa | Média-Alta |
| **Overhead** | Mínimo | Baixo-Médio |

## 🚀 Início Rápido

### 1. Verificar Logs Estruturados

```bash
# Verificar se traceId/spanId estão sendo gerados
tail -f logs/wallet-api.json | jq '.traceId, .spanId'

# Encontrar logs de uma requisição específica
grep "6ed5c3ad90a37e0437be0bf3f15cb9d6" logs/wallet-api.json
```

### 2. Consultas Básicas no Loki

```logql
# Todos os logs de um trace específico
{job="wallet-api"} |= "6ed5c3ad90a37e0437be0bf3f15cb9d6"

# Operações de depósito
{job="wallet-api"} | json | operation="DEPOSIT"

# Erros com traceId
{job="wallet-api"} | json | level="ERROR" | line_format "{{.traceId}}: {{.message}}"
```

### 3. Testar Correlação

```bash
# Fazer uma requisição e capturar o traceId dos logs
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Content-Type: application/json" \
  -d '{"walletId": "12345678-1234-1234-1234-123456789012", "amount": 100.00}'

# Buscar todos os logs relacionados no Loki
{job="wallet-api"} | json | traceId="<trace_id_capturado>"
```

## 📊 Métricas Principais

### Indicadores de Saúde do Tracing
- ✅ **TraceId válido**: Não deve ser `00000000000000000000000000000000`
- ✅ **SpanId válido**: Não deve ser `0000000000000000`
- ✅ **Correlação**: Todos os logs de uma requisição devem ter o mesmo traceId
- ✅ **Performance**: Overhead < 5ms por requisição

### Queries de Monitoramento
```logql
# Taxa de geração de traces
rate(count_over_time({job="wallet-api"} | json | traceId!="" [5m]))

# Traces com erro
{job="wallet-api"} | json | level="ERROR" | traceId!=""

# Requisições lentas
{job="wallet-api"} | json | operation="API_REQUEST_COMPLETED" | duration > 2000
```

## 🎯 Casos de Uso

### 1. **Suporte ao Cliente**
- Rastrear transações específicas por traceId
- Identificar falhas em operações de carteira
- Correlacionar logs de diferentes componentes

### 2. **Análise de Performance**
- Identificar requisições lentas
- Analisar gargalos por endpoint
- Monitorar tendências de performance

### 3. **Debugging e Troubleshooting**
- Investigar erros específicos
- Rastrear fluxo de execução
- Identificar padrões de falha

### 4. **Monitoramento Operacional**
- Alertas baseados em traces
- Dashboards de observabilidade
- Métricas de negócio

## 🔧 Configuração Atual

### Componentes Principais
- **TraceContextFilter**: Gera e propaga contexto de tracing
- **OpenTelemetryConfig**: Configuração manual do SDK
- **Logback**: Logging estruturado com MDC
- **Loki/Grafana**: Ingestão e visualização de logs

### Dependências
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-mdc-1.0</artifactId>
</dependency>
```

## 📈 Roadmap

### Fase 1: Atual ✅
- [x] Correlação básica por requisição HTTP
- [x] Logs estruturados com traceId/spanId
- [x] Integração com Loki/Grafana
- [x] Documentação completa

### Fase 2: Planejada 📋
- [ ] Spans granulares por operação
- [ ] Instrumentação automática de serviços
- [ ] Métricas customizadas
- [ ] Sampling configurável
- [ ] Propagação entre microsserviços

### Fase 3: Futuro 🔮
- [ ] Distributed tracing completo
- [ ] APM integration
- [ ] Machine learning para detecção de anomalias
- [ ] Otimizações de performance

## 🆘 Suporte

### Problemas Comuns
- **TraceId zerado**: Verificar configuração do OpenTelemetry
- **Logs não correlacionados**: Validar MDC e filtro
- **Performance degradada**: Revisar overhead do tracing

### Recursos Adicionais
- [Solução de Problemas](./solucao-problemas.md)
- [Consultas e Monitoramento](./consultas-monitoramento.md)
- [Plano de Upgrade](./upgrade-tracing-distribuido.md)

### Documentação Relacionada

- **🏠 Documentação Principal**: [README do Projeto](../../README-PT.md)
- **📊 Monitoramento**: [Configuração de Monitoramento](../../monitoring/pt/README.md)
- **⚙️ Configuração**: [Configuração de Logging](../../configuration/pt/configuracao-ambiente.md#configuração-de-logging)
- **🔒 Segurança**: [Configuração de Segurança](../../security/pt/configuracao-seguranca.md)

### Contato
Para dúvidas técnicas ou sugestões de melhoria, consulte a documentação específica ou entre em contato com a equipe de desenvolvimento.

---

*Esta documentação é mantida em sincronia com a implementação atual. Para atualizações, consulte o histórico de commits e a documentação técnica.*

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [English README](../en/README.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
