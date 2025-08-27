# Versões das Imagens Docker

Este documento rastreia as versões das imagens Docker utilizadas no projeto Digital Wallet API e a justificativa por trás da seleção de versões.

## Versões Atuais das Imagens

| Serviço | Imagem | Versão | Justificativa |
|---------|--------|--------|---------------|
| **PostgreSQL** | `postgres` | `16` | Versão LTS, estável e amplamente adotada |
| **Redis** | `redis` | `7-alpine` | Versão major estável mais recente, Alpine para menor footprint |
| **SonarQube** | `sonarqube` | `25.7.0.110598-community` | Versão estável mais recente da edição community |
| **Prometheus** | `prom/prometheus` | `v2.45.6` | Versão LTS, estabilidade comprovada em produção |
| **Grafana** | `grafana/grafana` | `11.2.0` | Versão estável com UI moderna e recursos |
| **Loki** | `grafana/loki` | `3.1.1` | Versão estável com melhorias de performance |
| **Promtail** | `grafana/promtail` | `3.1.1` | Deve corresponder à versão do Loki para compatibilidade |
| **Tempo** | `grafana/tempo` | `2.4.2` | Versão estável, compatível com o stack atual |

## Critérios de Seleção de Versões

### 🎯 **Critérios Principais**
- **Estabilidade**: Histórico comprovado em ambientes de produção
- **Compatibilidade**: Compatibilidade entre serviços dentro do stack de observabilidade
- **Segurança**: Atualizações regulares de segurança e patches
- **Suporte da Comunidade**: Manutenção ativa e adoção pela comunidade

### 🚫 **Por que Evitamos `latest`**
- **Atualizações Imprevisíveis**: Atualizações automáticas podem introduzir mudanças que quebram funcionalidades
- **Builds Não Reproduzíveis**: Diferentes ambientes podem ter versões diferentes
- **Complexidade de Debug**: Versão exata desconhecida torna troubleshooting difícil
- **Risco de Produção**: Atualizações descontroladas em ambientes de produção

## Matriz de Compatibilidade

### Compatibilidade do Stack Grafana
- **Loki 3.1.1** ↔ **Promtail 3.1.1**: Mesma versão necessária para compatibilidade de protocolo
- **Grafana 11.2.0** ↔ **Loki 3.1.1**: Totalmente compatível, combinação testada
- **Grafana 11.2.0** ↔ **Tempo 2.4.2**: Compatível para rastreamento distribuído

### Stack de Monitoramento
- **Prometheus v2.45.6** ↔ **Grafana 11.2.0**: Integração padrão, bem testada
- **Todos os serviços**: Compatíveis com health checks do Docker Compose

## Estratégia de Atualização

### 🔄 **Atualizações Regulares**
1. **Revisão Mensal**: Verificar atualizações de segurança e patches
2. **Avaliação Trimestral**: Avaliar novas versões estáveis
3. **Protocolo de Teste**: Sempre testar em desenvolvimento antes da produção
4. **Documentação**: Atualizar este documento com qualquer mudança de versão

### 📋 **Checklist de Atualização**
- [ ] Verificar notas de release para mudanças que quebram funcionalidades
- [ ] Verificar compatibilidade com serviços dependentes
- [ ] Testar em ambiente de desenvolvimento
- [ ] Atualizar documentação
- [ ] Validar funcionalidade do pipeline de observabilidade
- [ ] Atualizar pipelines de CI/CD se necessário

## Histórico de Versões

| Data | Serviço | Versão Anterior | Nova Versão | Motivo |
|------|---------|-----------------|-------------|---------|
| 2025-08-13 | Prometheus | `latest` | `v2.45.6` | Fixação inicial de versão para estabilidade |
| 2025-08-13 | Grafana | `latest` | `10.4.7` | Fixação inicial de versão para estabilidade |
| 2025-08-13 | Loki | `latest` | `2.9.8` | Fixação inicial de versão para estabilidade |
| 2025-08-13 | Promtail | `latest` | `2.9.8` | Fixação inicial de versão para compatibilidade |
| 2025-08-13 | Tempo | `latest` | `2.4.2` | Fixação inicial de versão para estabilidade |
| 2025-08-15 | Prometheus | `v2.45.6` | `v2.45.6` | Mantida versão LTS estável |
| 2025-08-15 | Grafana | `10.4.7` | `11.2.0` | Atualizada para versão estável verificada |
| 2025-08-15 | Loki | `2.9.8` | `3.1.1` | Atualizada para versão estável com compatibilidade |
| 2025-08-15 | Promtail | `2.9.8` | `3.1.1` | Atualizada para corresponder à versão do Loki |
| 2025-08-15 | Tempo | `2.4.2` | `2.4.2` | Mantida versão estável |

## Solução de Problemas

### Problemas Comuns Após Atualizações de Versão

#### **Serviço Não Inicia**
```bash
# Verificar logs do container
docker-compose logs [nome-do-serviço]

# Verificar disponibilidade da imagem
docker pull [imagem:versão]
```

#### **Problemas de Compatibilidade**
```bash
# Reiniciar todos os serviços na ordem correta
docker-compose down
docker-compose up -d

# Verificar saúde dos serviços
docker-compose ps
```

#### **Migração de Dados**
- **Loki**: Formato de dados é retrocompatível
- **Grafana**: Configurações de dashboard são preservadas
- **Prometheus**: Dados de métricas são retrocompatíveis

## Referências

- [Notas de Release do Prometheus](https://github.com/prometheus/prometheus/releases)
- [Notas de Release do Grafana](https://github.com/grafana/grafana/releases)
- [Notas de Release do Loki](https://github.com/grafana/loki/releases)
- [Melhores Práticas do Docker](https://docs.docker.com/develop/dev-best-practices/)

---

> **Nota**: Este documento deve ser atualizado sempre que as versões das imagens Docker forem alteradas. Sempre teste atualizações de versão em desenvolvimento antes de aplicar em produção.

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Docker Versions](../en/docker-versions.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
