# 📊 Monitoramento & Observabilidade

Esta seção cobre a configuração abrangente de monitoramento e observabilidade para a RecargaPay Wallet API, incluindo coleta de métricas, dashboards, alertas e rastreamento distribuído.

## 📋 Navegação Rápida

| 📄 Documento | 📝 Descrição | 🎯 Público |
|--------------|--------------|------------|
| [Setup de Observabilidade](guia-monitoramento-pt.md) | Configuração completa do stack de observabilidade | DevOps, SRE |
| [Guia de Dashboards](guia-monitoramento-pt.md#explorando-os-dashboards-do-grafana) | Criação e configuração de dashboards Grafana | Desenvolvedores, SRE |
| [Referência de Métricas](guia-monitoramento-pt.md#visualizando-métricas) | Lista completa de métricas disponíveis | Desenvolvedores, SRE |
| [Guia de Alertas](guia-monitoramento-pt.md#melhores-práticas) | Configuração de alertas e notificações | DevOps, SRE |

## 🎯 Stack de Observabilidade

### Componentes Principais
- **Grafana** - Visualização e dashboards (porta 3000)
- **Loki** - Agregação e consulta de logs
- **Promtail** - Coleta e encaminhamento de logs
- **Prometheus** - Coleta de métricas (via Spring Boot Actuator)
- **OpenTelemetry** - Integração de rastreamento distribuído

### Visão Geral da Arquitetura
```
Aplicação → Actuator → Métricas Prometheus
     ↓
Logs Estruturados → Promtail → Loki → Grafana
     ↓
Contexto de Tracing (traceId/spanId) → Correlação
```

## 🔧 Implementação Técnica

### Health Checks
- **Indicadores Customizados**: WalletDatabaseHealthIndicator, WalletServiceHealthIndicator
- **Grupos de Saúde**: Probes de Readiness (`/actuator/health/readiness`) e Liveness (`/actuator/health/liveness`)
- **Integração Kubernetes**: Pronto para monitoramento de saúde em orquestração de containers

### Coleta de Métricas
- **Micrometer @Timed Padrão**: Tempos de resposta da API para todos os endpoints de controller
- **Gauges Customizados**: Rastreamento de saldo de carteiras em tempo real
- **Contadores**: Contagem de transações por tipo e taxas de erro
- **Endpoint**: `/actuator/prometheus` para exposição de métricas

### Configuração de Logging
- **Logging Estruturado**: Formato JSON com timestamp, thread, nível e mensagem
- **Contexto de Trace**: Inclui traceId e spanId para correlação distribuída
- **Rotação de Logs**: Rotação automática (arquivos de 10MB, histórico de 10 arquivos)
- **Níveis de Log Dinâmicos**: Ajustáveis via endpoints `/actuator/loggers`

### Rastreamento Distribuído
- **Anotação @Traced**: Construída sobre a API @Observed do Micrometer
- **Operações Principais**: Todos os métodos de serviço (depósito, saque, transferência) são rastreados
- **Correlação de Trace**: IDs de trace propagados nos logs para rastreamento end-to-end

## 🚀 Começando

### Para Desenvolvedores
1. **[Setup Grafana](guia-monitoramento-pt.md#acessando-os-componentes)** - Configuração inicial do Grafana
2. **[Consultas de Log](../../tracing/pt/consultas-loki-traceid.md)** - Como consultar logs estruturados
3. **[Guia de Métricas](guia-monitoramento-pt.md#visualizando-métricas)** - Entender métricas disponíveis

### Para DevOps/SRE
1. **[Setup Completo](guia-monitoramento-pt.md)** - Configuração completa do stack
2. **[Guia de Alertas](guia-monitoramento-pt.md#melhores-práticas)** - Configurar alertas importantes
3. **[Guia de Dashboards](guia-monitoramento-pt.md#explorando-os-dashboards-do-grafana)** - Criar dashboards personalizados

## 📈 Métricas Principais

### Métricas da Aplicação
- **Métricas de Requisições HTTP**: Tempos de resposta, códigos de status, throughput
- **Métricas do Banco de Dados**: Uso do pool de conexões, performance de queries
- **Métricas de Cache**: Taxas de hit/miss, performance do Redis
- **Métricas da JVM**: Uso de memória, garbage collection, thread pools

### Métricas de Negócio
- **Operações de Carteira**: Criação, atualizações de saldo, contagem de transações
- **Métricas Financeiras**: Volumes de transação, taxas de sucesso
- **Atividade do Usuário**: Padrões de uso da API, taxas de autenticação

### Métricas de Infraestrutura
- **Recursos do Sistema**: CPU, memória, uso de disco
- **Métricas de Rede**: Contagem de conexões, uso de banda
- **Métricas de Container**: Saúde e performance dos containers Docker

## 🔍 Gerenciamento de Logs

### Logging Estruturado
- **Formato JSON** para legibilidade por máquina
- **Rastreamento distribuído** com correlação traceId/spanId
- **Informações contextuais** para debugging
- **Logging consciente de segurança** (sem dados sensíveis)

### Níveis de Log por Ambiente
| Ambiente | Aplicação | Framework | Banco de Dados |
|----------|-----------|-----------|----------------|
| **Desenvolvimento** | DEBUG | INFO | DEBUG |
| **Teste** | INFO | WARN | WARN |
| **Homologação** | INFO | WARN | ERROR |
| **Produção** | ERROR | ERROR | ERROR |

## 🚨 Estratégia de Alertas

### Alertas Críticos
- **Aplicação fora do ar** - Serviço indisponível
- **Falhas de conexão com banco** - Problemas de acesso a dados
- **Altas taxas de erro** - Erros da aplicação > 5%
- **Esgotamento de memória/CPU** - Restrições de recursos

### Alertas de Aviso
- **Tempos de resposta lentos** - Degradação de performance
- **Taxas de cache miss** - Impacto na performance
- **Espaço em disco baixo** - Preocupações de armazenamento
- **Altos volumes de transação** - Planejamento de capacidade

## 🛠️ Solução de Problemas

### Problemas Comuns

#### Logs Não Aparecem no Grafana
1. Verificar configuração do Promtail e caminhos de arquivo
2. Verificar ingestão do Loki e limites de stream
3. Validar formato de log e estrutura JSON
4. Verificar montagens de volume Docker e permissões

#### Métricas Não Disponíveis
1. Verificar se endpoints do Actuator estão expostos
2. Verificar configuração de métricas do Spring Boot
3. Validar configuração de scraping do Prometheus
4. Garantir conectividade de rede adequada

#### Problemas de Dashboard
1. Verificar configuração da fonte de dados do Grafana
2. Verificar sintaxe de query e intervalos de tempo
3. Validar nomes de métricas e labels
4. Verificar permissões e acesso ao dashboard

## 🔗 Documentação Relacionada

- **🏠 Documentação Principal**: [README do Projeto](../../../README.md)
- **⚙️ Configuração**: [Setup de Ambiente](../../configuration/pt/configuracao-ambiente.md)
- **🔍 Tracing**: [Rastreamento Distribuído](../../tracing/pt/)
- **🔒 Segurança**: [Monitoramento de Segurança](../../security/pt/configuracao-seguranca.md#monitoramento-de-segurança)

## 🎯 Melhores Práticas

### Desenvolvimento
- Usar logging estruturado consistentemente
- Incluir contexto relevante nas mensagens de log
- Monitorar métricas da aplicação durante desenvolvimento
- Testar funcionalidades de observabilidade localmente

### Produção
- Configurar alertas abrangentes
- Monitorar métricas de negócio junto com métricas técnicas
- Revisão regular da efetividade dos dashboards
- Planejamento de capacidade baseado em tendências de métricas

### Práticas do Time
- Definir SLIs/SLOs para serviços críticos
- Revisões regulares e melhorias de observabilidade
- Procedimentos de resposta a incidentes usando dados de observabilidade
- Compartilhamento de conhecimento sobre melhores práticas de monitoramento

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [English README](../en/README.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
