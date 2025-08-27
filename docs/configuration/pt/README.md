# Documentação de Configuração

## Visão Geral

Este diretório contém documentação abrangente para todos os arquivos de configuração e propriedades utilizados no projeto Digital Wallet API. Cada configuração é documentada detalhadamente com sua finalidade, impacto e relacionamentos.

## Estrutura da Documentação

### Documentação Principal (Português)

- **[Referência de Configuração](referencia-configuracao.md)** - Referência completa para todas as propriedades de configuração
- **[Detalhes de Configuração de Monitoramento](detalhes-configuracao-monitoramento.md)** - Configurações detalhadas do stack de monitoramento
- **[Exemplos de Configuração](exemplos-configuracao.md)** - Exemplos práticos e cenários

### Documentação em Inglês

- **[Configuration Reference](../en/configuration-reference.md)** - Versão em inglês da referência completa
- **[Monitoring Configuration Details](../en/monitoring-configuration-details.md)** - Detalhes do monitoramento em inglês
- **[Configuration Examples](../en/configuration-examples.md)** - Exemplos em inglês

## Referência Rápida

### Arquivos de Configuração da Aplicação

| Arquivo | Finalidade | Ambiente |
|---------|------------|----------|
| `src/main/resources/application.yml` | Configuração principal da aplicação | Todos |
| `src/test/resources/application-test.yml` | Configuração específica para testes | Apenas testes |

### Arquivos de Configuração do Stack de Monitoramento

| Arquivo | Finalidade | Serviço |
|---------|------------|---------|
| `monitoring/loki/loki-config.yaml` | Configuração de agregação de logs | Loki |
| `monitoring/promtail/promtail-config.yaml` | Configuração de coleta de logs | Promtail |
| `monitoring/prometheus/prometheus.yml` | Configuração de coleta de métricas | Prometheus |
| `monitoring/grafana/datasources.yml` | Configuração de fontes de dados | Grafana |
| `monitoring/grafana/dashboards.yml` | Provisionamento de dashboards | Grafana |
| `monitoring/tempo/tempo-config.yaml` | Configuração de tracing distribuído | Tempo |

## Principais Categorias de Configuração

### 🔧 Aplicação Principal
- **Spring Framework**: Nome da aplicação, perfis, configuração básica
- **Banco de Dados**: Conexão PostgreSQL, configurações JPA/Hibernate
- **Segurança**: Configuração JWT, autenticação
- **Migração**: Configurações de migração de banco Flyway

### 📊 Monitoramento e Observabilidade
- **Logging**: Logging JSON estruturado com correlação de trace
- **Métricas**: Exposição de métricas Prometheus via Actuator
- **Tracing**: Tracing distribuído OpenTelemetry
- **Health Checks**: Monitoramento de saúde da aplicação

### 🔍 Gerenciamento de Logs
- **Coleta**: Scraping e processamento de arquivos pelo Promtail
- **Armazenamento**: Agregação e indexação de logs pelo Loki
- **Visualização**: Consultas e dashboards de logs no Grafana

### 📈 Métricas e Alertas
- **Coleta**: Scraping de métricas pelo Prometheus
- **Armazenamento**: Armazenamento de métricas em séries temporais
- **Visualização**: Dashboards de métricas no Grafana

## Relacionamentos Críticos de Configuração

### Cadeia de Banco de Dados
```
spring.datasource.* → Conexão PostgreSQL
spring.flyway.* → Gerenciamento de Schema  
spring.jpa.hibernate.ddl-auto=validate → Validação de Schema
```

### Cadeia de Logging
```
Aplicação → Logs JSON → Sistema de Arquivos
Promtail → Faz Scrape de Arquivos → Extrai JSON
Promtail → Envia Logs → Loki
Grafana → Consulta Loki → Exibe Logs
```

### Cadeia de Métricas
```
Aplicação → /actuator/prometheus → Endpoint de Métricas
Prometheus → Faz Scrape de Métricas → Armazena Dados
Grafana → Consulta Prometheus → Exibe Métricas
```

### Cadeia de Tracing
```
TraceContextFilter → Gera traceId/spanId
MDC → Popula Contexto de Log → Logs JSON
Promtail → Extrai traceId/spanId → Loki
Grafana → Correlaciona por traceId → Visualização de Trace
```

## Dependências de Porta

| Porta | Serviço | Finalidade |
|-------|---------|------------|
| 8080 | Wallet API | Servidor principal da aplicação |
| 3100 | Loki | Ingestão e consultas de logs |
| 9080 | Promtail | Métricas de coleta de logs |
| 9090 | Prometheus | Coleta de métricas e interface |
| 3000 | Grafana | Dashboards de visualização |
| 5432 | PostgreSQL | Servidor de banco de dados |

## Notas Específicas por Ambiente

### Desenvolvimento
- **Logging**: Nível DEBUG habilitado
- **Segurança**: Secrets hardcoded (aceitável)
- **Banco**: PostgreSQL local
- **Monitoramento**: Todos endpoints expostos
- **Armazenamento**: Armazenamento local em arquivos

### Recomendações para Produção
- **Logging**: Nível INFO, formato estruturado
- **Segurança**: Gerenciamento externo de secrets
- **Banco**: PostgreSQL externo com pooling
- **Monitoramento**: Exposição restrita de endpoints
- **Armazenamento**: Backends de armazenamento em nuvem

## Padrões Comuns de Configuração

### Alta Disponibilidade
- Múltiplas instâncias Loki com armazenamento compartilhado
- Federação Prometheus para escalabilidade
- Instâncias de aplicação com balanceamento de carga

### Endurecimento de Segurança
- JWT com gerenciamento externo de chaves
- Criptografia de conexão com banco
- Endpoints actuator restritos
- Autenticação para serviços de monitoramento

### Otimização de Performance
- Tuning de pool de conexões
- Políticas de retenção de logs
- Gerenciamento de cardinalidade de métricas
- Otimização de performance de consultas

## Referência Rápida para Troubleshooting

### "Maximum active stream limit exceeded"
- **Causa**: Labels de alta cardinalidade no Promtail
- **Correção**: Remover traceId/spanId da seção labels
- **Config**: Manter apenas em json.expressions

### Erros Connection Refused
- **Causa**: Serviço não pronto ou porta errada
- **Verificar**: Status do serviço Docker e mapeamentos de porta
- **Correção**: Verificar dependências de serviço no docker-compose.yml

### Métricas Ausentes
- **Causa**: Target ou caminho errado no Prometheus
- **Verificar**: Acessibilidade do endpoint /actuator/prometheus
- **Correção**: Verificar management.endpoints.web.exposure.include

### Logs Não Aparecem
- **Causa**: Acesso a arquivos do Promtail ou rejeição do Loki
- **Verificar**: Caminhos de arquivo, permissões, limites do Loki
- **Correção**: Verificar montagens de volume e geração de arquivos

## Melhores Práticas

### Gerenciamento de Configuração
1. **Variáveis de Ambiente**: Usar para dados sensíveis em produção
2. **Validação**: Sempre validar configurações antes do deploy
3. **Documentação**: Manter esta documentação atualizada com mudanças
4. **Testes**: Testar mudanças de configuração em desenvolvimento primeiro

### Segurança
1. **Secrets**: Nunca commitar secrets no controle de versão
2. **Acesso**: Restringir acesso a endpoints de monitoramento em produção
3. **Criptografia**: Usar TLS para todas comunicações externas
4. **Auditoria**: Logar mudanças de configuração

### Performance
1. **Monitoramento**: Monitorar impacto das configurações na performance
2. **Tuning**: Revisar e ajustar regularmente baseado em padrões de uso
3. **Escalabilidade**: Planejar para requisitos de escalabilidade horizontal
4. **Otimização**: Otimizar baseado em métricas de uso real

Esta documentação garante compreensão abrangente de todos os aspectos de configuração para operação e manutenção efetiva do sistema.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [English README](../en/README.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
