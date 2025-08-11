# Detalhes de Configuração de Monitoramento

## Visão Geral

Este documento fornece explicações detalhadas para todas as configurações do stack de monitoramento, incluindo configurações do Loki, Promtail, Prometheus, Grafana e Tempo.

---

## Configuração do Loki (monitoring/loki/loki-config.yaml)

### Configuração do Servidor

#### server.http_listen_port: 3100
- **Finalidade**: Porta da API HTTP para ingestão e consultas de logs
- **Impacto**: Todos os clientes (Promtail, Grafana) devem conectar nesta porta
- **Relacionamentos**: Referenciado no mapeamento de portas do docker-compose.yml (3100:3100)

### Configuração do Ingester

#### ingester.lifecycler.address: 127.0.0.1
- **Finalidade**: Endereço de bind para o componente ingester
- **Desenvolvimento**: 127.0.0.1 para acesso apenas local
- **Produção**: Deve ser 0.0.0.0 para implantação em cluster

#### ingester.lifecycler.ring.kvstore.store: inmemory
- **Finalidade**: Armazenamento chave-valor para associação do ring
- **Desenvolvimento**: inmemory para simplicidade
- **Produção**: Deve usar consul ou etcd para persistência

#### ingester.lifecycler.ring.replication_factor: 1
- **Finalidade**: Número de réplicas para cada stream de log
- **Desenvolvimento**: 1 para implantação de instância única
- **Produção**: 3+ para alta disponibilidade

#### ingester.chunk_idle_period: 5m
- **Finalidade**: Tempo antes de fazer flush de chunks ociosos para armazenamento
- **Impacto**: Períodos menores = flushes mais frequentes = melhor performance de consulta
- **Trade-off**: Mais operações I/O vs. uso de memória

#### ingester.chunk_retain_period: 30s
- **Finalidade**: Tempo para reter chunks na memória após flush
- **Impacto**: Melhora performance de consulta para logs recentes
- **Memória**: Valores maiores usam mais memória

### Configuração de Schema

#### schema_config.configs[0].from: 2020-10-24
- **Finalidade**: Data de início para esta configuração de schema
- **Requisito**: Deve ser anterior a qualquer entrada de log
- **Impacto**: Afeta como logs são indexados e armazenados

#### schema_config.configs[0].store: boltdb
- **Finalidade**: Backend de armazenamento de índice
- **Desenvolvimento**: boltdb para armazenamento local em arquivo
- **Produção**: cassandra ou bigtable para escalabilidade

#### schema_config.configs[0].object_store: filesystem
- **Finalidade**: Backend de armazenamento de chunks
- **Desenvolvimento**: filesystem para armazenamento local
- **Produção**: s3, gcs ou azure para escalabilidade

#### schema_config.configs[0].schema: v11
- **Finalidade**: Versão do schema de índice
- **Impacto**: Afeta performance de consulta e eficiência de armazenamento
- **Requisito**: Deve corresponder às capacidades da versão do Loki

### Configuração de Armazenamento

#### storage_config.boltdb.directory: /loki/index
- **Finalidade**: Diretório local para arquivos de índice BoltDB
- **Docker**: Mapeado para volume loki_data
- **Permissões**: Deve ser gravável pelo processo Loki

#### storage_config.filesystem.directory: /loki/chunks
- **Finalidade**: Diretório local para arquivos de chunk
- **Docker**: Mapeado para volume loki_data
- **Permissões**: Deve ser gravável pelo processo Loki

### Configuração de Limites (Crítico para Estabilidade)

#### limits_config.max_streams_per_user: 10000
- **Finalidade**: Previne explosão de cardinalidade
- **Problema Resolvido**: Erros HTTP 429 "Maximum active stream limit exceeded"
- **Impacto**: Essencial para ingestão estável de logs com dados de alta cardinalidade

#### limits_config.ingestion_rate_mb: 4
- **Finalidade**: Controla throughput de ingestão (4 MB/s)
- **Impacto**: Logs podem ser rejeitados se taxa for excedida
- **Ajuste**: Aumentar para aplicações de alto volume

#### limits_config.ingestion_burst_size_mb: 6
- **Finalidade**: Permite picos temporários de ingestão (burst de 6 MB)
- **Relacionamento**: Deve ser maior que ingestion_rate_mb
- **Impacto**: Lida com picos de tráfego sem rejeição

#### limits_config.per_stream_rate_limit: 3MB
- **Finalidade**: Limite de taxa por stream individual
- **Impacto**: Previne que um único stream sobrecarregue o sistema
- **Complemento**: Funciona com limites de taxa globais

#### limits_config.max_line_size: 256000
- **Finalidade**: Tamanho máximo para linhas de log individuais (256KB)
- **Impacto**: Entradas de log grandes serão truncadas ou rejeitadas
- **Logs JSON**: Geralmente suficiente para logs estruturados

#### limits_config.max_entries_limit_per_query: 5000
- **Finalidade**: Limita tamanho do resultado de consulta
- **Impacto**: Consultas grandes podem ser truncadas
- **Performance**: Previne exaustão de memória

#### limits_config.cardinality_limit: 100000
- **Finalidade**: Máximo de combinações únicas de labels
- **Impacto**: Previne explosão de cardinalidade de labels
- **Crítico**: Essencial para performance com muitos labels únicos

---

## Configuração do Promtail (monitoring/promtail/promtail-config.yaml)

### Configuração do Servidor

#### server.http_listen_port: 9080
- **Finalidade**: Porta do servidor HTTP para métricas e status do Promtail
- **Evitar Conflitos**: Usa 9080 para evitar conflitos com aplicação (8080)

#### server.grpc_listen_port: 0
- **Finalidade**: Desabilita servidor gRPC
- **Simplificação**: Apenas HTTP necessário para esta implantação

### Rastreamento de Posição

#### positions.filename: /tmp/positions.yaml
- **Finalidade**: Rastreia posição de leitura em arquivos de log
- **Comportamento de Reinício**: Previne releitura de logs após reinício
- **Localização**: Diretório temporário adequado para desenvolvimento

### Configuração de Cliente

#### clients[0].url: http://loki:3100/loki/api/v1/push
- **Finalidade**: Endpoint Loki para envio de logs
- **Docker**: Usa nome do serviço 'loki' do docker-compose.yml
- **Porta**: Deve corresponder ao http_listen_port do Loki

### Configuração de Scrape

#### scrape_configs[0].job_name: wallet-api-json-logs
- **Finalidade**: Identifica esta fonte de logs no Loki
- **Rotulagem**: Aparece como job="wallet-api" nos logs

#### static_configs[0].labels.__path__: /app/logs/wallet-api*.json
- **Finalidade**: Padrão de arquivo para coleta de logs
- **Volume Docker**: /app/logs mapeado para ./logs no docker-compose.yml
- **Padrão**: Corresponde a wallet-api.json e arquivos rotacionados

### Estágios de Pipeline (Crítico para Processamento de Logs)

#### json.expressions
- **Finalidade**: Extrai campos de entradas de log JSON
- **Campos Extraídos**:
  - timestamp: Para ordenação correta de logs
  - level: Nível de log (INFO, DEBUG, ERROR)
  - message: Conteúdo da mensagem de log
  - logger: Nome da classe Java
  - traceId: Identificador de tracing distribuído
  - spanId: Identificador de span
  - operation: Operação de negócio (DEPOSIT, WITHDRAW, etc.)
  - walletId: Identificador da carteira
  - amount: Valor da transação

#### timestamp.source: timestamp
- **Finalidade**: Usa timestamp extraído para ordenação de logs
- **Formato**: Formato RFC3339Nano do logback
- **Crítico**: Garante ordenação cronológica correta

#### labels (Apenas Baixa Cardinalidade)
- **Incluídos**: level, logger, operation, service, environment
- **Excluídos**: traceId, spanId, walletId (alta cardinalidade)
- **Finalidade**: Previne explosão de streams mantendo consultabilidade
- **Crítico**: Campos de alta cardinalidade causam erros "too many streams"

---

## Configuração do Prometheus (monitoring/prometheus/prometheus.yml)

### Configuração Global

#### global.scrape_interval: 15s
- **Finalidade**: Intervalo padrão para coleta de métricas
- **Balanceamento**: Frequência vs. uso de recursos
- **Override**: Jobs individuais podem especificar intervalos diferentes

#### global.evaluation_interval: 15s
- **Finalidade**: Frequência de avaliação de regras de alerta
- **Consistência**: Corresponde ao intervalo de scrape para avaliação de regras

### Configurações de Scrape

#### Job: prometheus (Auto-monitoramento)
- **Target**: localhost:9090
- **Finalidade**: Prometheus monitora a si mesmo
- **Métricas**: Métricas internas do Prometheus

#### Job: recargapay-wallet-api
- **Target**: host.docker.internal:8080
- **Caminho de Métricas**: /actuator/prometheus
- **Intervalo de Scrape**: 5s (mais frequente que padrão)
- **Finalidade**: Coleta métricas da aplicação
- **Docker**: host.docker.internal resolve para máquina host

#### Job: docker
- **Target**: host.docker.internal:9323
- **Finalidade**: Métricas do daemon Docker (se habilitado)
- **Opcional**: Pode não estar disponível em todos os ambientes

---

## Configuração do Grafana

### Datasources (monitoring/grafana/datasources.yml)

#### Datasource Prometheus
- **URL**: http://prometheus:9090
- **Finalidade**: Visualização de métricas
- **Docker**: Usa nome do serviço do docker-compose.yml

#### Datasource Loki
- **URL**: http://loki:3100
- **Finalidade**: Visualização e correlação de logs
- **Docker**: Usa nome do serviço do docker-compose.yml

#### Datasource Tempo
- **URL**: http://tempo:3200
- **Finalidade**: Visualização de tracing distribuído
- **Docker**: Usa nome do serviço do docker-compose.yml

### Dashboards (monitoring/grafana/dashboards.yml)

#### Provedor de Dashboard
- **Caminho**: /etc/grafana/dashboards
- **Finalidade**: Carrega automaticamente arquivos JSON de dashboard
- **Volume Docker**: ./monitoring/dashboards mapeado para container

---

## Configuração do Tempo (monitoring/tempo/tempo-config.yaml)

### Configuração do Servidor

#### server.http_listen_port: 3200
- **Finalidade**: API HTTP para consultas de trace
- **Grafana**: Usado pelo Grafana para visualização de traces

#### server.grpc_listen_port: 9095
- **Finalidade**: API gRPC para ingestão de traces
- **Uso**: Receptores de trace OTLP

### Configuração do Distribuidor

#### distributor.receivers.otlp.protocols.grpc.endpoint: 0.0.0.0:4317
- **Finalidade**: Recebe traces OTLP via gRPC
- **Padrão**: Porta padrão OpenTelemetry
- **Docker**: Exposto como 4317:4317

#### distributor.receivers.otlp.protocols.http.endpoint: 0.0.0.0:4318
- **Finalidade**: Recebe traces OTLP via HTTP
- **Alternativa**: Alternativa HTTP ao gRPC

### Configuração de Armazenamento

#### storage.trace.backend: local
- **Finalidade**: Armazenamento local em arquivo para traces
- **Desenvolvimento**: Adequado para desenvolvimento
- **Produção**: Deve usar armazenamento em nuvem (s3, gcs)

#### storage.trace.local.path: /var/tempo
- **Finalidade**: Diretório de armazenamento local
- **Docker**: Mapeado para volume tempo_data

---

## Relacionamentos de Configuração

### Integração Docker Compose

1. **Nomes de Serviço**: Usados em URLs (loki:3100, prometheus:9090)
2. **Mapeamentos de Porta**: Acesso externo aos serviços
3. **Montagens de Volume**: Armazenamento persistente e arquivos de config
4. **Dependências**: Ordem de inicialização dos serviços

### Fluxo de Logs

1. **Aplicação** → Logs JSON → **Sistema de Arquivos**
2. **Promtail** → Faz scrape de arquivos → **Extrai JSON**
3. **Promtail** → Envia logs → **Loki**
4. **Grafana** → Consulta → **Loki** → Exibe logs

### Fluxo de Métricas

1. **Aplicação** → Expõe métricas → **/actuator/prometheus**
2. **Prometheus** → Faz scrape → **Aplicação**
3. **Grafana** → Consulta → **Prometheus** → Exibe métricas

### Fluxo de Traces

1. **Aplicação** → Gera traces → **TraceContextFilter**
2. **Logs** → Incluem traceId/spanId → **Correlação**
3. **Grafana** → Correlaciona → **Logs por traceId**

---

## Resolução de Problemas Comuns

### "Maximum active stream limit exceeded"
- **Causa**: Labels de alta cardinalidade no Promtail
- **Solução**: Remover traceId, spanId da seção labels
- **Config**: Manter apenas em json.expressions

### Erros "Connection refused"
- **Causa**: Serviço não pronto ou porta errada
- **Verificar**: Status do serviço Docker e mapeamentos de porta
- **Solução**: Verificar dependências de serviço

### Métricas ausentes no Prometheus
- **Causa**: Target ou caminho errado
- **Verificar**: Acessibilidade do endpoint /actuator/prometheus
- **Solução**: Verificar management.endpoints.web.exposure.include

### Logs não aparecem no Grafana
- **Causa**: Promtail não encontra arquivos ou rejeição do Loki
- **Verificar**: Caminhos de arquivo, permissões, limites do Loki
- **Solução**: Verificar montagens de volume e geração de arquivos

Esta referência detalhada de configuração garante compreensão e manutenção adequadas do stack de monitoramento.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Monitoring Configuration Details in English](../en/monitoring-configuration-details.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
