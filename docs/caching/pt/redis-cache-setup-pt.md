# Guia de Configuração do Cache Distribuído Redis

## Visão Geral

Este guia explica como configurar e usar o cache distribuído Redis na RecargaPay Wallet API seguindo **melhores práticas da indústria**. O cache Redis melhora a performance armazenando dados frequentemente acessados em memória, reduzindo consultas ao banco de dados e melhorando os tempos de resposta.

A implementação segue **padrões da indústria financeira** com:
- **Nomenclatura hierárquica de chaves** (`namespace:entidade:operacao:versao`)
- **TTLs conservadores** para consistência de dados financeiros
- **Serialização JDK** para serialização/deserialização confiável de objetos
- **Cache distribuído** entre múltiplas instâncias da aplicação
- **Versionamento automático de cache** para invalidação em deployments

## Configuração

### Variáveis de Ambiente

Adicione as seguintes configurações Redis ao seu arquivo `.env`:

```bash
# Configuração do Banco de Dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=recargapay_wallet
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_db_password

# Configuração JWT
JWT_SECRET=your_super_secure_jwt_secret_key_here_minimum_256_bits

# Configuração Redis para Cache Distribuído
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_secure_password_here

# Configuração de Cache
APP_CACHE_VERSION=v1

# Configuração TTL do Cache (Padrões da Indústria Financeira)
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Configuração do Usuário Admin
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_secure_admin_password

# Configuração da Aplicação
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Configuração de Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG
```

### Configuração da Aplicação

A configuração de cache agora é **totalmente configurável** via variáveis de ambiente no `application.yml`:

```yaml
# Configuração da Aplicação
app:
  # Configuração do usuário
  user:
    username: ${ADMIN_USERNAME}
    password: ${ADMIN_PASSWORD}
  
  # Configuração do cache
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      # Valores TTL em minutos seguindo padrões da indústria financeira
      default: ${CACHE_TTL_DEFAULT_MINUTES}           # Padrão conservador: 2 minutos
      wallet-list: ${CACHE_TTL_WALLET_LIST_MINUTES}   # Coleção de carteiras: 3 minutos  
      wallet-single: ${CACHE_TTL_WALLET_SINGLE_MINUTES} # Carteira individual: 1 minuto
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS} # Dados financeiros críticos: 30 segundos
      wallet-transactions: ${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES} # Dados históricos: 10 minutos
      user-profile: ${CACHE_TTL_USER_PROFILE_MINUTES} # Dados do usuário: 15 minutos
```

### Configuração Docker

O Redis é configurado automaticamente no `docker-compose.yml`:

```yaml
redis:
  image: redis:7-alpine
  container_name: wallet-redis
  restart: unless-stopped
  ports:
    - "6379:6379"
  command: redis-server --appendonly yes
  volumes:
    - redis_data:/data
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Configuração do Cache

Configuração do cache Redis no `application.yml`:

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

# Configuração de Cache
app:
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      # Valores TTL em minutos seguindo padrões da indústria financeira
      default: ${CACHE_TTL_DEFAULT_MINUTES}           # Padrão conservador: 2 minutos
      wallet-list: ${CACHE_TTL_WALLET_LIST_MINUTES}   # Coleção de carteiras: 3 minutos  
      wallet-single: ${CACHE_TTL_WALLET_SINGLE_MINUTES} # Carteira individual: 1 minuto
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS} # Dados financeiros críticos: 30 segundos
      wallet-transactions: ${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES} # Dados históricos: 10 minutos
      user-profile: ${CACHE_TTL_USER_PROFILE_MINUTES} # Dados do usuário: 15 minutos
```

## Arquitetura do Cache

### Regiões de Cache e TTLs (Padrões da Indústria Financeira)

O cache é organizado em regiões com **TTLs conservadores** apropriados para dados financeiros:

| **Região do Cache** | **Propósito** | **TTL** | **Padrão da Chave** |
|---------------------|---------------|---------|---------------------|
| `wallet-list` | Coleções de carteiras | 3 minutos | `wallet-api:wallet-list:v1:all` |
| `wallet-single` | Carteiras individuais | 1 minuto | `wallet-api:wallet-single:v1:{walletId}` |
| `wallet-balance` | Dados de saldo | 30 segundos | `wallet-api:wallet-balance:v1:{walletId}` |
| `wallet-transactions` | Histórico de transações | 10 minutos | `wallet-api:wallet-transactions:v1:{walletId}` |
| `user-profile` | Perfis de usuário | 15 minutos | `wallet-api:user-profile:v1:{userId}` |

### Convenção de Nomenclatura de Chaves

Seguindo **melhores práticas da indústria**, as chaves de cache usam nomenclatura hierárquica:

```
{aplicacao}:{regiao-cache}:{versao}:{identificador}
```

**Exemplos:**
- `wallet-api:wallet-list:v1:all`
- `wallet-api:wallet-single:v1:123e4567-e89b-12d3-a456-426614174000`
- `wallet-api:wallet-balance:v1:123e4567-e89b-12d3-a456-426614174000`

### Classe de Configuração do Cache

A classe `CacheConfig` configura o gerenciador de cache Redis com **padrões da indústria** e **TTLs configuráveis**:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${app.cache.version}")
    private String cacheVersion;
    
    // Configuração TTL do cache via variáveis de ambiente (em minutos/segundos)
    @Value("${app.cache.ttl.default}")
    private int defaultTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-list}")
    private int walletListTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-single}")
    private int walletSingleTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-balance}")
    private int walletBalanceSeconds;
    
    @Value("${app.cache.ttl.wallet-transactions}")
    private int walletTransactionsTtlMinutes;
    
    @Value("${app.cache.ttl.user-profile}")
    private int userProfileTtlMinutes;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
        
        // Configuração padrão do cache com TTL configurável para dados financeiros
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jdkSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> applicationName + ":" + cacheName + ":" + cacheVersion + ":");

        // Configurações customizadas seguindo padrões da indústria financeira
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("wallet-list", defaultConfig.entryTtl(Duration.ofMinutes(walletListTtlMinutes)));
        cacheConfigurations.put("wallet-single", defaultConfig.entryTtl(Duration.ofMinutes(walletSingleTtlMinutes)));
        cacheConfigurations.put("wallet-balance", defaultConfig.entryTtl(Duration.ofSeconds(walletBalanceSeconds)));
        cacheConfigurations.put("wallet-transactions", defaultConfig.entryTtl(Duration.ofMinutes(walletTransactionsTtlMinutes)));
        cacheConfigurations.put("user-profile", defaultConfig.entryTtl(Duration.ofMinutes(userProfileTtlMinutes)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

### Características Principais

- **🔧 TTLs Configuráveis**: Todos os valores TTL são carregados de variáveis de ambiente
- **🚫 Sem Valores Padrão**: Todas as anotações `@Value` requerem variáveis de ambiente (sem fallbacks padrão)
- **🏗️ Chaves Hierárquicas**: Padrão `nomeAplicacao:regiaoCache:versao:chave`
- **💾 Serialização JDK**: Serialização confiável de objetos sem problemas de compatibilidade JSON
- **🔄 Controle de Versão**: Invalidação automática do cache via mudanças de versão

## Implementação nos Serviços

### Anotações de Cache

Os serviços usam anotações Spring Cache com **invalidação adequada de cache**:

#### Operações de Leitura (Caching)

```java
// Listar todas as carteiras
@Cacheable(value = "wallet-list", key = "'all'")
public List<Wallet> findAll() {
    return walletRepository.findAll();
}

// Buscar carteira por ID
@Cacheable(value = "wallet-single", key = "#walletId")
public Wallet findById(UUID walletId) {
    return walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
}
```

#### Operações de Escrita (Invalidação de Cache)

```java
// Criar carteira - invalida cache de lista e individual
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#result.id", condition = "#result != null")
})
public Wallet create(Wallet wallet) {
    // Implementação
}

// Depósito - invalida caches afetados
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#walletId")
})
public Transaction deposit(UUID walletId, BigDecimal amount) {
    // Implementação
}

// Transferência - invalida ambas as carteiras
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#fromWalletId"),
    @CacheEvict(value = "wallet-single", key = "#toWalletId")
})
public List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
    // Implementação
}
```

## Serialização de Entidades

As entidades devem implementar `Serializable` para serialização JDK:

```java
@Entity
@Table(name = "wallets")
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Campos e métodos da entidade
}
```

## Exemplos de Uso

### Testando Funcionalidade do Cache

1. **Iniciar a aplicação com Redis:**
   ```bash
   docker-compose up -d redis
   ./mvnw spring-boot:run
   ```

2. **Testar cache hit/miss:**
   ```bash
   # Primeira requisição (cache miss) - resposta mais lenta
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets
   
   # Segunda requisição (cache hit) - resposta mais rápida
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets
   ```

3. **Verificar chaves de cache no Redis:**
   ```bash
   docker exec -it wallet-redis redis-cli
   > KEYS wallet-api:*
   > TTL wallet-api:wallet-list:v1:all
   ```

### Testando Invalidação de Cache

1. **Criar uma carteira (invalida cache):**
   ```bash
   curl -X POST -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"userId":"123e4567-e89b-12d3-a456-426614174000","initialBalance":100.00}' \
        http://localhost:8080/api/wallets
   ```

2. **Verificar que o cache foi invalidado:**
   ```bash
   # Esta será um cache miss devido à invalidação
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets
   ```

## ⚠️ **IMPORTANTE: Metodologia Correta de Validação do Cache**

### **🚨 Problemas Comuns na Validação**

Muitos desenvolvedores enfrentam dificuldades ao validar o cache Redis devido a **problemas de timing e metodologia**. Esta seção explica como validar corretamente.

#### **❌ Metodologia Incorreta (Que NÃO Funciona):**

```bash
# PROBLEMA: Teste manual com delays
1. Fazer requisição no Postman/Insomnia
2. [Tempo passa - mudança de aplicação]
3. [Tempo passa - abertura do terminal]
4. [Tempo passa - digitação dos comandos]
5. Verificar chaves: KEYS wallet-api:*
6. Resultado: (empty array) ❌ - Chave já expirou!
```

**Por que falha:**
- **TTL curto**: 3 minutos para dados financeiros
- **Delay humano**: 2-5 minutos entre requisição e verificação
- **Padrão incorreto**: `wallet-api:*` ao invés de `recargapay-wallet-api:*`

#### **✅ Metodologia Correta (Que Funciona):**

```bash
# SOLUÇÃO: Teste automatizado sem delays
# 1. Obter token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  -s | jq -r '.token')

# 2. Limpar cache para teste limpo
docker exec wallet-redis redis-cli FLUSHALL

# 3. Primeira requisição (cache miss) com cronômetro
echo "=== PRIMEIRA REQUISIÇÃO (CACHE MISS) ==="
start_time=$(date +%s%N)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets -s > /dev/null
end_time=$(date +%s%N)
first_time=$(( (end_time - start_time) / 1000000 ))
echo "Tempo: ${first_time}ms"

# 4. Verificar chave IMEDIATAMENTE (sem delay)
echo "=== VERIFICANDO CHAVE CRIADA ==="
docker exec wallet-redis redis-cli KEYS "*"
docker exec wallet-redis redis-cli TTL "recargapay-wallet-api:wallet-list:v1:all"

# 5. Segunda requisição (cache hit) com cronômetro
echo "=== SEGUNDA REQUISIÇÃO (CACHE HIT) ==="
start_time=$(date +%s%N)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets -s > /dev/null
end_time=$(date +%s%N)
second_time=$(( (end_time - start_time) / 1000000 ))
echo "Tempo: ${second_time}ms"

# 6. Calcular melhoria
if [ $SECOND_TIME -lt $FIRST_TIME ]; then
    IMPROVEMENT=$(( (FIRST_TIME - SECOND_TIME) * 100 / FIRST_TIME ))
    echo -e "\n=== RESULTADO ==="
    echo "Cache Miss: ${FIRST_TIME}ms"
    echo "Cache Hit: ${SECOND_TIME}ms"
    echo "Melhoria: ${IMPROVEMENT}%"
else
    echo -e "\n=== RESULTADO ==="
    echo "Cache Miss: ${FIRST_TIME}ms"
    echo "Cache Hit: ${SECOND_TIME}ms"
    echo "Melhoria: 0%"
fi

# 8. Informações finais
echo -e "\n=== INFORMAÇÕES FINAIS ==="
echo "🔑 Chaves no Redis:"
docker exec wallet-redis redis-cli KEYS "*"
echo "📊 Estatísticas Redis:"
docker exec wallet-redis redis-cli INFO keyspace
```

### **🔧 Comandos de Diagnóstico**

#### **Verificação de Conectividade:**

```bash
# Testar conexão Redis
docker exec -it wallet-redis redis-cli ping
# Resultado esperado: PONG

# Verificar status da aplicação
curl -s http://localhost:8080/actuator/health | jq .components.redis
# Resultado esperado: {"status": "UP", "details": {"version": "7.4.5"}}
```

#### **Monitoramento em Tempo Real:**

```bash
# Monitorar comandos Redis em tempo real
docker exec -it wallet-redis redis-cli MONITOR

# Em outro terminal, fazer requisições e ver os comandos sendo executados
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets
```

#### **Análise de Performance:**

```bash
# Estatísticas detalhadas do Redis
docker exec -it wallet-redis redis-cli INFO stats

# Informações de memória
docker exec -it wallet-redis redis-cli INFO memory

# Informações sobre chaves
docker exec -it wallet-redis redis-cli INFO keyspace
```

### **📋 Checklist de Validação**

Use este checklist para garantir que o cache está funcionando corretamente:

- [ ] **Conectividade**: `docker exec -it wallet-redis redis-cli ping` retorna `PONG`
- [ ] **Aplicação**: `/actuator/health` mostra Redis como `UP`
- [ ] **Autenticação**: Token JWT obtido com sucesso
- [ ] **Cache Miss**: Primeira requisição cria chave no Redis
- [ ] **Cache Hit**: Segunda requisição é mais rápida
- [ ] **TTL**: Chaves têm TTL apropriado (30s-3min)
- [ ] **Nomenclatura**: Chaves seguem padrão `recargapay-wallet-api:*`
- [ ] **Expiração**: Chaves expiram após TTL configurado
- [ ] **Invalidação**: Cache é limpo após operações de escrita

### **🚨 Troubleshooting Específico**

#### Problema: "Chaves não aparecem"

```bash
# Diagnóstico:
1. Verificar padrão correto: KEYS "*" (não "wallet-api:*")
2. Verificar timing: Fazer verificação imediatamente após requisição
3. Verificar TTL: docker exec wallet-redis redis-cli TTL "chave"
4. Verificar autenticação: Requisição deve retornar HTTP 200
```

#### Problema: "Cache não melhora performance"

```bash
# Diagnóstico:
1. Usar cronômetro automatizado (não manual)
2. Limpar cache antes do teste: FLUSHALL
3. Fazer múltiplas requisições para ver padrão
4. Verificar se dados estão sendo buscados do banco vs Redis
```

#### Problema: "TTL muito baixo"

```bash
# Explicação:
- TTL de 3 minutos é CORRETO para dados financeiros
- Padrão da indústria: 30s-3min para dados críticos
- Para teste: Use script automatizado, não verificação manual
```

## Melhores Práticas

### Cache de Dados Financeiros
1. **TTLs Conservadores**: Use TTLs curtos (30s-3min) para dados financeiros
2. **Invalidação Imediata**: Sempre invalide cache após mudanças de saldo
3. **Consistência > Performance**: Prefira precisão dos dados sobre performance do cache

### Gerenciamento de Chaves
1. **Nomenclatura Hierárquica**: Use padrão `namespace:entidade:operacao:versao`
2. **Versionamento**: Inclua versão nas chaves para invalidação em deployments
3. **Padrões Consistentes**: Mantenha nomenclatura consistente entre serviços

### Monitoramento
1. **Taxa de Cache Hit**: Monitore e busque >70% de taxa de hit
2. **Uso de Memória**: Configure alertas para uso de memória do Redis
3. **Monitoramento de TTL**: Verifique se chaves estão expirando conforme esperado
4. **Rastreamento de Erros**: Monitore exceções relacionadas ao cache

### Segurança
1. **Sem Dados Sensíveis**: Nunca faça cache de informações sensíveis como senhas
2. **Controle de Acesso**: Proteja instância Redis em produção
3. **Criptografia de Dados**: Considere criptografia para dados sensíveis em cache

## Considerações para Produção

### Deployment
1. **Cache Warming**: Considere pré-carregar dados críticos após deployment
2. **Rollout Gradual**: Teste comportamento do cache em ambiente de staging
3. **Monitoramento**: Configure monitoramento abrangente de cache

### Escalabilidade
1. **Cluster Redis**: Considere cluster Redis para alta disponibilidade
2. **Pool de Conexões**: Ajuste configurações do pool para carga
3. **Gerenciamento de Memória**: Planeje requisitos de memória do Redis

### Backup e Recuperação
1. **Persistência AOF**: Habilitada por padrão em nossa configuração
2. **Backups Regulares**: Considere backups periódicos dos dados Redis
3. **Recuperação de Desastres**: Planeje recuperação da instância Redis

Esta implementação segue **melhores práticas da indústria** usadas por empresas como Netflix, Uber e instituições financeiras, garantindo confiabilidade, performance e manutenibilidade.

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Redis Configuration in English](../en/redis-cache-setup.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
