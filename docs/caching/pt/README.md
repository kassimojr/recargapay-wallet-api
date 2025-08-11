# 💾 Cache Redis & Performance

Esta seção cobre a implementação do cache distribuído Redis na RecargaPay Wallet API, incluindo setup, configuração e estratégias de otimização de performance.

## 📋 Navegação Rápida

| 📄 Documento | 📝 Descrição | 🎯 Público |
|--------------|--------------|------------|
| [Setup do Cache Redis](redis-cache-setup-pt.md) | Guia completo de implementação do cache Redis | Desenvolvedores, DevOps |

## 🚀 Visão Geral do Cache

### Estratégia de Cache Distribuído
- **Baseado em Redis** para escalabilidade
- **Padrões TTL da indústria financeira** (30s-15min)
- **Nomenclatura hierárquica** de chaves para organização
- **Versionamento automático** do cache para deploys
- **Serialização JDK** para confiabilidade

### Regiões de Cache & TTLs

| Região do Cache | TTL | Caso de Uso | Impacto na Performance |
|-----------------|-----|-------------|------------------------|
| `wallet-list` | 3 minutos | Coleção de carteiras | Alto - Reduz carga no BD |
| `wallet-single` | 1 minuto | Dados de carteira individual | Médio - Atualizações frequentes |
| `wallet-balance` | 30 segundos | Dados financeiros críticos | Crítico - Precisão em tempo real |
| `wallet-transactions` | 10 minutos | Dados históricos | Baixo - Raramente muda |
| `user-profile` | 15 minutos | Informações do usuário | Baixo - Dados estáticos |

## 🎯 Começando

### Para Desenvolvedores
1. **Setup Redis**: [Guia Completo de Setup](redis-cache-setup-pt.md)
2. **Configurar ambiente**: [Variáveis de Ambiente](redis-cache-setup-pt.md#variáveis-de-ambiente)
3. **Testar cache**: [Metodologia de Validação](redis-cache-setup-pt.md)
4. **Produção**: [Considerações de Produção](redis-cache-setup-pt.md)

### Para DevOps
1. **Setup de produção**: [Configuração Redis](redis-cache-setup-pt.md)
2. **Monitoramento**: [Monitoramento do Cache](redis-cache-setup-pt.md)
3. **Troubleshooting**: [Solução de Problemas](redis-cache-setup-pt.md)

## 🏗️ Integração com Arquitetura

### Arquitetura Hexagonal
- **Camada de Domínio**: Lógica de negócio agnóstica ao cache
- **Camada de Aplicação**: Anotações de cache (`@Cacheable`, `@CacheEvict`)
- **Camada de Infraestrutura**: Configuração e gerenciamento de conexão Redis

### Integração com Serviços
```java
@Cacheable(value = "wallet-single", key = "#walletId")
public Wallet findById(UUID walletId) {
    // Lógica de negócio - cached automaticamente
}

@CacheEvict(value = {"wallet-list", "wallet-single"}, key = "#walletId")
public void updateWallet(UUID walletId) {
    // Invalidação do cache em atualizações
}
```

## 🔧 Destaques da Configuração

### Configuração Baseada em Ambiente
```yaml
app:
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      default: ${CACHE_TTL_DEFAULT_MINUTES}
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS}
      # ... outros TTLs configuráveis
```

### Funcionalidades Prontas para Produção
- **Pool de conexões** para alta performance
- **Failover automático** e mecanismos de retry
- **Versionamento de cache** para deploys sem downtime
- **Monitoramento abrangente** e health checks

## 📊 Benefícios de Performance

### Redução da Carga no Banco de Dados
- **Até 80% de redução** em queries do banco para dados cached
- **Tempos de resposta mais rápidos** para dados frequentemente acessados
- **Escalabilidade melhorada** para cenários de alto tráfego

### Conformidade com Dados Financeiros
- **TTLs conservadores** garantem frescor dos dados
- **Invalidação imediata** em operações financeiras
- **Trilha de auditoria** através de logging estruturado

## 🔗 Documentação Relacionada

- **🏠 Documentação Principal**: [README do Projeto](../../../README.md)
- **⚙️ Configuração**: [Setup de Ambiente](../../configuration/pt/configuracao-ambiente.md)
- **🔒 Segurança**: [Configuração de Segurança](../../security/pt/configuracao-seguranca.md)
- **📊 Monitoramento**: [Setup de Observabilidade](../../monitoring/pt/)

## 🛡️ Melhores Práticas

### Desenvolvimento
- Testar comportamento do cache em ambiente local
- Monitorar taxas de hit/miss do cache
- Validar configurações de TTL
- Usar scripts de validação fornecidos

### Produção
- Monitorar uso de memória do Redis
- Configurar alertas para falhas do cache
- Testes regulares de backup e recovery
- Tuning de performance baseado em métricas

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [English README](../en/README.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../README.md).*
