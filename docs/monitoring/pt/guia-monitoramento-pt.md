# Guia de Monitoramento da API Wallet RecargaPay

## Introdução ao Monitoramento e Observabilidade

Sistemas de software modernos necessitam de monitoramento abrangente para garantir confiabilidade, disponibilidade e desempenho. Este guia explica como usar a estrutura de monitoramento implementada para a API Wallet RecargaPay.

### Conceitos Fundamentais

**Monitoramento** envolve a coleta, processamento e exibição de métricas sobre seu sistema para entender sua saúde e desempenho.

**Observabilidade** estende o monitoramento ao fornecer insights mais profundos sobre o que está acontecendo dentro do sistema, tipicamente através de três pilares:
1. **Métricas**: Dados numéricos sobre o desempenho do sistema
2. **Logs**: Registros detalhados de texto dos eventos
3. **Rastreamento (Tracing)**: Acompanhamento do fluxo de requisições através de sistemas distribuídos

## Componentes da Estrutura de Monitoramento

A solução de monitoramento da API Wallet RecargaPay é construída com ferramentas padrão da indústria:

### 1. Spring Boot Actuator
Fornece endpoints para expor saúde da aplicação, métricas e informações operacionais.

### 2. Micrometer
Uma biblioteca de instrumentação de métricas que coleta e distribui dados de métricas.

### 3. Prometheus
Uma ferramenta de código aberto de monitoramento e alertas que extrai e armazena métricas.

### 4. Grafana
Uma plataforma de visualização que exibe métricas em dashboards personalizáveis.

## Configuração e Inicialização

### Iniciando o Ambiente de Monitoramento

1. Navegue até a raiz do projeto:
   ```bash
   cd /caminho/para/recargapay-wallet-api
   ```

2. Execute o script de configuração de monitoramento:
   ```bash
   ./monitoring.sh
   ```

3. Aguarde que todos os serviços iniciem (PostgreSQL, Prometheus, Grafana, etc.)

### Acessando os Componentes

- **API Wallet**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (use admin/admin como credenciais)
- **SonarQube**: http://localhost:9000

## Usando os Recursos de Monitoramento

### Verificações de Saúde (Health Checks)

Os health checks fornecem informações sobre o status operacional da API:

1. Acesse o status geral de saúde:
   ```
   http://localhost:8080/actuator/health
   ```

2. Acesse verificações de saúde específicas:
   - Sonda de prontidão (readiness): `http://localhost:8080/actuator/health/readiness`
   - Sonda de vida (liveness): `http://localhost:8080/actuator/health/liveness`
   - Saúde do serviço de carteira: `http://localhost:8080/actuator/health/wallet`

Uma resposta típica de health check se parece com:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "walletService": {
      "status": "UP"
    }
  }
}
```

### Visualizando Métricas

As métricas brutas estão disponíveis no formato Prometheus em:
```
http://localhost:8080/actuator/prometheus
```

As métricas principais incluem:
- `wallet_balance_total`: Saldos atuais das carteiras
- `wallet_transaction_count`: Número de transações por tipo
- `http_server_requests_seconds`: Tempos de resposta da API
- `jvm_memory_used_bytes`: Uso de memória

### Usando o Prometheus

1. Acesse o Prometheus em http://localhost:9090
2. No campo de consulta, insira nomes de métricas como `wallet_balance_total`
3. Clique em "Execute" para ver o resultado
4. Use a aba de gráfico para visualizar métricas ao longo do tempo

Consultas avançadas:
- `rate(http_server_requests_seconds_count{uri="/api/v1/wallet"}[5m])`: Taxa de requisições nos últimos 5 minutos
- `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))`: Tempo de resposta no percentil 95

### Explorando os Dashboards do Grafana

1. Acesse o Grafana em http://localhost:3000 (login com admin/admin)
2. Navegue até Dashboards → RecargaPay → Wallet API Monitoring

O dashboard contém:
- **Painéis de visão geral**: Saldo total atual e contagem de transações
- **Métricas de desempenho**: Tempos de resposta da API e taxas de erro
- **Painéis de transação**: Detalhamento de tipos e valores de transações
- **Saúde do sistema**: Uso de recursos do servidor e métricas JVM

Para personalizar o dashboard:
1. Clique no ícone de engrenagem no topo
2. Selecione "Editar" 
3. Modifique os painéis conforme necessário
4. Salve suas alterações

### Ajustando Níveis de Log

O ajuste de nível de log em tempo de execução está disponível em:
```
http://localhost:8080/actuator/loggers
```

Para alterar um nível de log:
1. Envie uma requisição POST com o nível desejado:
   ```bash
   curl -X POST http://localhost:8080/actuator/loggers/com.recargapay.wallet \
     -H 'Content-Type: application/json' \
     -d '{"configuredLevel": "DEBUG"}'
   ```
2. Níveis disponíveis: TRACE, DEBUG, INFO, WARN, ERROR

## Testando o Sistema de Monitoramento

### Gerando Dados de Exemplo

Use estes comandos para gerar métricas:

1. Criar uma carteira:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet \
     -H 'Content-Type: application/json' \
     -d '{"userId":"user123","initialBalance":100.00}'
   ```
   Anote o ID da carteira retornado para comandos subsequentes.

2. Depositar fundos:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet/{ID_DA_CARTEIRA}/deposit \
     -H 'Content-Type: application/json' \
     -d '{"amount":50.00}'
   ```

3. Sacar fundos:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet/{ID_DA_CARTEIRA}/withdraw \
     -H 'Content-Type: application/json' \
     -d '{"amount":25.00}'
   ```

### Verificando a Coleta de Métricas

1. Após gerar transações, verifique o dashboard do Grafana
2. Observe as mudanças em:
   - Saldo da carteira
   - Contagem de transações
   - Gráficos de tempo de resposta da API

## Entendendo a Implementação

### Implementação de Métricas

Classes principais:
- `MetricsConfig`: Configura a coleta de métricas
- Anotações `@Timed`: Adicionadas aos métodos do controlador para rastrear tempos de resposta
- Métodos de serviço com métricas para detalhes de transação

### Implementação de Health Checks

Indicadores de saúde personalizados:
- `WalletDatabaseHealthIndicator`: Verifica a conectividade do banco de dados
- `WalletServiceHealthIndicator`: Valida a funcionalidade do serviço de carteira

### Rastreamento Distribuído

Detalhes da implementação:
- `TracingConfig`: Configura a API de Observação do Micrometer
- Anotação `@Traced`: Aplicada aos métodos de serviço para rastreamento
- Logs aprimorados com traceId e spanId para correlação

## Melhores Práticas

1. **Monitoramento Regular**: Verifique os dashboards regularmente, não apenas durante incidentes
2. **Estabelecimento de Linha de Base**: Aprenda o que é "normal" para suas métricas
3. **Organização de Dashboards**: Agrupe métricas relacionadas para análise mais fácil
4. **Configuração de Alertas**: Configure alertas para limites críticos
5. **Diretrizes de Logging**: Mantenha níveis e formatos de logs consistentes
6. **Medição de Desempenho**: Use métricas para impulsionar melhorias de desempenho

## Solução de Problemas

### Problemas Comuns

1. **Serviços não iniciam**: Verifique os logs do Docker com `docker logs wallet-prometheus`
2. **Métricas ausentes**: Verifique se os alvos do Prometheus estão ativos na UI do Prometheus
3. **Dashboard não mostrando dados**: Verifique a configuração da fonte de dados do Grafana

### Soluções Rápidas

1. Reinicie a stack: `docker-compose down && ./start-monitoring.sh`
2. Verifique a saúde dos serviços: `docker-compose ps`
3. Verifique a conectividade da rede: `curl http://localhost:8080/actuator/health`

## Conclusão

Esta configuração de monitoramento fornece visibilidade abrangente na API Wallet RecargaPay. Ao revisar regularmente métricas e verificações de saúde, você pode garantir desempenho ideal e identificar rapidamente problemas antes que afetem os usuários. A combinação de métricas, verificações de saúde e rastreamento distribuído cria uma solução robusta de observabilidade para toda a aplicação.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Monitoring Guide in English](../en/monitoring-guide-en.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
