# Consultas Loki para TraceId e Operações de Negócio

Este documento explica como consultar logs por `traceId`, `spanId` e operações de negócio (`operation`) no Grafana Loki da Digital Wallet API.

## 🎯 Campos Disponíveis para Consulta

### **Campos Diretos (Visíveis na Interface)**
A partir das melhorias implementadas, os seguintes campos estão disponíveis **diretamente** na interface do Grafana:

- **`operation`**: Tipo de operação (`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `API_REQUEST_COMPLETED`)
- **`status`**: Status da operação (`START`, `SUCCESS`, `ERROR`)
- **`walletId`**: ID da carteira (disponível para operações de negócio)
- **`level`**: Nível do log (`INFO`, `DEBUG`, `ERROR`)
- **`logger`**: Classe que gerou o log
- **`traceId`**: ID de rastreamento distribuído
- **`spanId`**: ID do span atual

### **Campos no JSON Aninhado**
Campos adicionais disponíveis via extração JSON:
- **`amount`**: Valor da transação
- **`currency`**: Moeda (BRL)
- **`transactionId`**: ID da transação gerada
- **`fromWalletId`** / **`toWalletId`**: Para transferências
- **`errorType`**: Tipo de erro (quando aplicável)

## 📊 Consultas Diretas (Recomendadas)

### **Por Operação**
```logql
# Todas as operações de depósito
{job="wallet-api", operation="DEPOSIT"}

# Operações de depósito bem-sucedidas
{job="wallet-api", operation="DEPOSIT", status="SUCCESS"}

# Operações de saque com erro
{job="wallet-api", operation="WITHDRAW", status="ERROR"}

# Todas as transferências
{job="wallet-api", operation="TRANSFER"}
```

### **Por Nível de Log**
```logql
# Logs de erro com operação específica
{job="wallet-api", level="ERROR", operation="DEPOSIT"}

# Logs de info para todas as operações
{job="wallet-api", level="INFO"} | operation != ""
```

## 🔍 Consultas com Extração JSON

### **Por Valores Específicos**
```logql
# Operações em carteira específica
{job="wallet-api", operation="DEPOSIT"} | json | walletId="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

# Transações acima de determinado valor
{job="wallet-api", operation="DEPOSIT"} | json | amount > 100

# Operações com moeda específica
{job="wallet-api"} | json | currency="BRL"
```

### **Por TraceId (Rastreamento Distribuído)**
```logql
# Todos os logs de uma jornada específica
{job="wallet-api"} | json | traceId="859ad6f5630636b1e9b62696309e5e7e"

# Logs de uma operação específica por traceId
{job="wallet-api", operation="DEPOSIT"} | traceId="859ad6f5630636b1e9b62696309e5e7e"
```

## 📈 Consultas Avançadas

### **Análise de Performance**
```logql
# Operações que demoraram mais que 1 segundo
{job="wallet-api", operation="DEPOSIT"} | json | processingTimeMs > 1000

# Média de tempo de processamento por operação
rate({job="wallet-api", operation="DEPOSIT"}[5m]) | json | avg(processingTimeMs)
```

### **Monitoramento de Erros**
```logql
# Todos os erros de operações de negócio
{job="wallet-api", status="ERROR"}

# Taxa de erro por operação
sum(rate({job="wallet-api", status="ERROR"}[5m])) by (operation)
```

## 🔧 Benefícios das Melhorias

### **Antes (Consulta Complexa)**
```logql
# Era necessário usar extração JSON para tudo
{job="wallet-api"} | json | operation=~"DEPOSIT.*"
```

### **Agora (Consulta Direta)**
```logql
# Consulta direta e mais eficiente
{job="wallet-api", operation="DEPOSIT"}
```

### **Vantagens**
1. **🚀 Performance**: Consultas diretas são mais rápidas
2. **👁️ Visibilidade**: Campos aparecem como colunas na interface
3. **🎯 Filtros**: Filtros diretos no painel do Grafana
4. **📊 Dashboards**: Criação de dashboards mais eficientes
5. **🔍 Debugging**: Identificação rápida de problemas

## 💡 Dicas de Uso

### **Filtros Combinados**
```logql
# Operações de depósito em carteira específica nos últimos 5 minutos
{job="wallet-api", operation="DEPOSIT"} | json | walletId="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" [5m]
```

### **Agregações**
```logql
# Contagem de operações por status
sum by (status) (count_over_time({job="wallet-api", operation="DEPOSIT"}[1h]))

# Volume total de transações
sum(rate({job="wallet-api", operation="DEPOSIT"}[5m])) | json | sum(amount)
```

## ⚠️ Observações Importantes

1. **Labels vs JSON**: Use labels (`operation`, `status`) para filtros principais e JSON para valores específicos
2. **Performance**: Evite usar `walletId` como label (alta cardinalidade)
3. **Intervalos**: Use intervalos de tempo para limitar o escopo das consultas
4. **Case Sensitive**: Nomes de campos são sensíveis a maiúsculas

## 🛠️ Solução de Problemas

### **Se campos não aparecem**
1. Verifique se a aplicação foi reiniciada após as melhorias
2. Confirme se o Promtail foi reiniciado
3. Aguarde alguns minutos para ingestão dos novos logs

### **Se consultas retornam vazio**
1. Verifique o intervalo de tempo selecionado
2. Confirme se há operações sendo executadas
3. Use `{job="wallet-api"}` primeiro para verificar se logs estão chegando

### **Para debugging**
```logql
# Verificar se logs estão sendo ingeridos
{job="wallet-api"} | limit 10

# Verificar estrutura dos logs
{job="wallet-api", operation="DEPOSIT"} | limit 1
```

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Loki TraceID Queries in English](../en/loki-queries-traceid.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
