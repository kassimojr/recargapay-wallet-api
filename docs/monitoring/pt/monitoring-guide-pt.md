# Digital Wallet API - Guia de Monitoramento

Este documento descreve a abordagem de monitoramento para a Digital Wallet API, incluindo as métricas coletadas, configuração de dashboards e configuração de alertas.

## Visão Geral

O stack de monitoramento consiste em:

- **Spring Boot Actuator**: Expõe métricas da aplicação e endpoints de saúde
- **Micrometer com Prometheus**: Coleta e formata métricas no formato Prometheus
- **Prometheus**: Banco de dados de séries temporais para armazenamento e consulta de métricas
- **Grafana**: Visualização e dashboards para métricas e alertas
- **AlertManager**: Processamento de alertas e notificação

## Coleta de Métricas

### Métricas Padrão

As seguintes métricas padrão são coletadas automaticamente:

- **Métricas JVM**: Uso de memória, coleta de lixo, utilização de threads
- **Métricas do Sistema**: Uso de CPU, carga média, descritores de arquivo
- **Métricas HTTP**: Taxas de requisição, tempos de resposta, códigos de status
- **Pool de Conexões do Banco de Dados**: Uso de conexões, tempo de espera, timeouts

### Métricas de Negócio Customizadas

Implementamos métricas customizadas específicas para a Wallet API:

- **Métricas de Transação**:
  - `wallet_transaction_count_total`: Contagem de transações por tipo (depósito, saque, transferência)
  - `wallet_transaction_amount_total`: Valor total de transações por tipo
  - `wallet_transaction_duration_seconds`: Tempo de processamento da transação
  - `wallet_transaction_errors_total`: Contagem de erros de transação

- **Métricas da Carteira**:
  - `wallet_balance`: Saldo atual nas carteiras (como um gauge)

## Implementação da Coleta de Métricas

A coleta de métricas é implementada de várias maneiras:

1. **Coleta Automática**: Spring Boot Actuator coleta automaticamente métricas do sistema, JVM e HTTP

2. **Coleta Baseada em Aspectos**: A classe `MetricsAspect` intercepta operações da carteira para registrar:
   - Contagens de transações
   - Valores de transações
   - Duração do processamento
   - Contagens de erros

3. **Coleta Baseada em Anotações**: Anotação `@Timed` para timing preciso de endpoints da API

## Implementação de Métricas

### Métricas Disponíveis

A API Wallet expõe as seguintes métricas personalizadas:

| Nome da Métrica | Tipo | Descrição | Tags |
|-----------------|------|-----------|------|
| `wallet_transaction_count_total` | Counter | Número total de transações de carteira | `operation` (deposit, withdrawal, transfer) |
| `wallet_transaction_amount_total` | Counter | Valor total das transações de carteira | `operation`, `currency` |
| `wallet_transaction_duration_seconds` | Timer | Tempo levado para processar transações | `operation` |
| `wallet_transaction_errors_total` | Counter | Número total de erros de transação | `operation`, `error` |
| `wallet_balance` | Gauge | Saldo atual da carteira | `wallet_id`, `currency` |
| `http_request_duration_seconds` | Timer | Duração da requisição HTTP | `endpoint` |

### Suporte a Moedas Personalizadas

O sistema de métricas agora suporta a configuração da moeda padrão e a especificação de moedas personalizadas para transações:

#### Configuração

A moeda padrão pode ser configurada em `application-monitoring.yml`:

```yaml
wallet:
  metrics:
    default-currency: BRL  # Mude para sua moeda padrão
```

#### Usando Moedas Personalizadas

Os métodos de serviço podem especificar uma moeda personalizada ao registrar métricas:

```java
// Registrando com moeda padrão
metricsService.recordDepositTransaction(amount, durationMs);

// Registrando com moeda personalizada
metricsService.recordDepositTransaction(amount, durationMs, "USD");
```

O aspecto de métricas detectará automaticamente códigos de moeda nos parâmetros do método quando possível.

### Constantes de Métricas Centralizadas

Todos os nomes de métricas e chaves de tags agora estão centralizados na classe `MetricsConstants` para melhor manutenção. Ao adicionar novas métricas, atualize esta classe para manter a consistência.

### Pontos de Integração de Métricas

As métricas são coletadas automaticamente nos seguintes pontos:

1. **Camada de Serviço**: Através de aspectos AOP que interceptam operações de depósito, saque e transferência
2. **Camada de Controlador**: Usando a anotação `@Timed` do Micrometer diretamente nos métodos do controlador para medir o desempenho dos endpoints
3. **Registro Ad-hoc**: Injetando o `MetricsService` e chamando seus métodos diretamente

## Configuração do Prometheus

As métricas são expostas em `/actuator/prometheus` e coletadas pelo Prometheus usando recursos ServiceMonitor.

Configurações principais:
- Intervalo de coleta de 15 segundos
- Período de retenção: 15 dias
- Timeout de coleta otimizado: 14 segundos

## Alertas

Configuramos os seguintes alertas:

| Nome do Alerta | Descrição | Limiar | Severidade |
|------------|-------------|-----------|----------|
| WalletApiHighResponseTime | Tempo de resposta da API muito alto | P95 > 500ms por 5m | Aviso |
| WalletApiHighErrorRate | Taxa de erro HTTP muito alta | > 5% por 2m | Crítico |
| WalletApiTransactionErrors | Erros de transação de negócio | > 0,5/seg por 2m | Crítico |
| WalletApiHighJvmMemoryUsage | Uso de memória JVM muito alto | > 80% por 5m | Aviso |
| WalletApiHighDbConnections | Uso do pool de conexões do BD | > 75% por 2m | Aviso |
| WalletApiDown | API não está respondendo | Indisponível por > 1m | Crítico |

As notificações de alerta estão configuradas para serem enviadas para:
- Canal do Slack: #wallet-api-alerts
- Email: devops@digitalwallet.com

## Dashboards

Criamos um dashboard abrangente no Grafana para a Wallet API que inclui:

1. **Visão Geral de Transações**:
   - Taxas de transações por tipo (depósito, saque, transferência)
   - Contagens de erros de transação
   - Duração das transações (p95, p50)

2. **Desempenho da API**:
   - Taxa de requisições por endpoint
   - Distribuição do tempo de resposta
   - Taxa de erro por código de status

3. **Utilização de Recursos**:
   - Uso de memória JVM
   - Uso do pool de conexões do banco de dados
   - Métricas de CPU e memória do sistema

## Implantação

O stack de monitoramento é implantado como parte da infraestrutura Kubernetes usando Helm charts:

- Prometheus e Grafana são instalados no namespace `monitoring`
- Recursos ServiceMonitor e PrometheusRule são implantados para configurar o monitoramento
- Credenciais de acesso são armazenadas como secrets do Kubernetes

## Instruções de Configuração

1. Execute o script de instalação para implantar Prometheus e Grafana:

```bash
./kubernetes/monitoring/install-monitoring.sh
```

2. Acesse os dashboards do Grafana usando a URL e credenciais fornecidas pelo script de instalação.

3. A aplicação wallet-api deve ser executada com o perfil Spring `monitoring` habilitado para expor métricas:

```yaml
SPRING_PROFILES_ACTIVE: dev,monitoring
```

## Solução de Problemas

### Problemas Comuns

1. **Métricas não aparecem no Prometheus**:
   - Verifique se a aplicação está rodando com o perfil `monitoring`
   - Verifique se o ServiceMonitor está direcionado corretamente para a aplicação
   - Verifique se o endpoint de métricas está acessível: `curl http://wallet-api:8080/actuator/prometheus`

2. **Alertas não disparando**:
   - Verifique se o PrometheusRule está aplicado corretamente
   - Verifique as condições de alerta na UI do Prometheus
   - Verifique a configuração do AlertManager

3. **Dashboard não mostrando dados**:
   - Verifique se a fonte de dados Prometheus está configurada corretamente no Grafana
   - Verifique por erros de sintaxe PromQL nas consultas do painel
   - Verifique se as métricas existem no Prometheus

### Logs

Logs relevantes podem ser encontrados em:

- Logs da aplicação: `kubectl logs -l app=wallet-api`
- Logs do Prometheus: `kubectl logs -n monitoring -l app=prometheus`
- Logs do Grafana: `kubectl logs -n monitoring -l app=grafana`

## Diagrama de Arquitetura

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Wallet API │     │  Prometheus │     │   Grafana   │
│             │────>│             │────>│             │
│/actuator/...│     │ Time-series │     │ Dashboards  │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │                    ▲
                           ▼                    │
                    ┌─────────────┐     ┌─────────────┐
                    │ AlertManager│     │  ServiceMon │
                    │             │     │ PrometheusR │
                    │Notificações│      │   Config    │
                    └─────────────┘     └─────────────┘
```

> **Nota**: Este documento é uma tradução do guia oficial de monitoramento (monitoring-guide.md). 
> O documento oficial em inglês deve ser considerado como a referência principal para conformidade com 
> os padrões do projeto, enquanto esta versão em português serve como referência adicional.
