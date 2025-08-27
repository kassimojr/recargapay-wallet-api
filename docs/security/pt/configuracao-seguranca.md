# Guia de Configuração de Segurança

Este documento fornece uma visão abrangente do sistema de configuração de segurança implementado na Digital Wallet API, seguindo as melhores práticas da indústria para deploy seguro de aplicações.

## Resumo Executivo

A Digital Wallet API foi migrada com sucesso de credenciais hardcoded para um sistema de configuração externalizada e segura. Esta implementação garante zero risco de exposição de credenciais mantendo excelência operacional em todos os ambientes.

## Melhorias de Segurança Implementadas

### 1. Eliminação de Credenciais Hardcoded
- **Antes**: Credenciais embutidas no código fonte (`admin`, `admin`)
- **Depois**: Todos os dados sensíveis externalizados via variáveis de ambiente
- **Impacto**: Zero risco de exposição de credenciais no controle de versão

### 2. Implementação de Configuração Fail-Fast
- **Antes**: Aplicação iniciava com valores padrão se configuração ausente
- **Depois**: Aplicação falha ao iniciar sem configuração adequada
- **Impacto**: Detecção imediata de problemas de configuração, previne incidentes em produção

### 3. Adoção de Convenção de Nomenclatura Genérica
- **Antes**: Nomes específicos de role (`adminUsername`, `adminPassword`)
- **Depois**: Nomes genéricos (`ADMIN_USERNAME`, `ADMIN_PASSWORD`)
- **Impacto**: Redução de divulgação de informações, melhoria na manutenibilidade

### 4. Hierarquia de Configuração Aprimorada
```
Ordem de Prioridade (Maior para Menor):
1. Variáveis de Ambiente do Sistema
2. Arquivo .env (desenvolvimento)
3. application-{profile}.yml
4. application.yml
```

## Estrutura de Configuração Atual

### Propriedades da Aplicação
```yaml
# application.yml - Produção (Sem Fallbacks)
app:
  user:
    username: ${ADMIN_USERNAME}          # Falha se não fornecido
    password: ${ADMIN_PASSWORD}          # Falha se não fornecido

# application-dev.yml - Desenvolvimento (Fallbacks Seguros)
app:
  user:
    username: ${ADMIN_USERNAME:testuser}     # Fallback apenas para dev
    password: ${ADMIN_PASSWORD:testpass}     # Fallback apenas para dev
```

### Variáveis de Ambiente
```bash
# Obrigatórias para todos os ambientes
ADMIN_USERNAME=seu_usuario_admin
ADMIN_PASSWORD=sua_senha_admin_segura
JWT_SECRET=sua_chave_jwt_super_segura_com_minimo_256_bits

# Segurança do banco de dados
DB_USERNAME=seu_usuario_db
DB_PASSWORD=sua_senha_db_segura

# Segurança do Redis
REDIS_PASSWORD=senha_redis_segura_aqui
```

## Configuração de Segurança JWT

### Requisitos do Segredo JWT
- **Comprimento mínimo**: 256 bits (32 caracteres)
- **Complexidade**: Mix de letras, números e caracteres especiais
- **Unicidade**: Segredo diferente por ambiente
- **Rotação**: Rotação regular de segredo recomendada

### Configuração JWT
```yaml
# Configurações JWT são tratadas pelo Spring Security
# Segredo é carregado da variável de ambiente
JWT_SECRET=${JWT_SECRET}
```

### Validação de Token
```java
// Validação automática pelo Spring Security
// Lógica de validação customizada no SecurityConfig
@EnableWebSecurity
public class SecurityConfig {
    // Configuração JWT com segredo baseado em ambiente
}
```

## Segurança Específica por Ambiente

### Ambiente de Desenvolvimento
```yaml
# application-dev.yml
management:
  endpoints:
    web:
      exposure:
        include: "*"  # Todos os endpoints para debugging
  endpoint:
    health:
      show-details: always  # Detalhes completos de saúde
```

### Ambiente de Teste
```yaml
# application-test.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # Endpoints limitados
  endpoint:
    health:
      show-details: always  # Detalhes completos para testes
```

### Ambiente de Homologação
```yaml
# application-hml.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics  # Restrição moderada
  endpoint:
    health:
      show-details: when-authorized  # Acesso apenas para autorizados
```

### Ambiente de Produção
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus  # Endpoints mínimos
  endpoint:
    health:
      show-details: never  # Sem exposição de detalhes
  server:
    port: 9090  # Porta separada para gerenciamento
```

## Headers de Segurança & CORS

### Headers de Segurança Automáticos
A aplicação configura automaticamente:
- **X-Content-Type-Options**: `nosniff`
- **X-Frame-Options**: `DENY`
- **X-XSS-Protection**: `1; mode=block`
- **Strict-Transport-Security**: Enforcement HTTPS
- **Content-Security-Policy**: Proteção XSS

### Configuração CORS
```java
@CrossOrigin(origins = {"http://localhost:3000", "https://seu-frontend.com"})
// Configurado conforme requisitos do ambiente
```

## Monitoramento de Segurança

### Health Checks
```bash
# Verificar status de segurança da aplicação
curl http://localhost:8080/actuator/health

# Verificar headers de segurança
curl -I http://localhost:8080/api/v1/wallets
```

### Teste de Autenticação
```bash
# Testar endpoint de login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seu_usuario_admin","password":"sua_senha_admin"}'

# Testar endpoint protegido
curl -H "Authorization: Bearer SEU_JWT_TOKEN" \
  http://localhost:8080/api/v1/wallets
```

## Checklist de Validação de Segurança

### Verificação de Segurança Pré-Deploy
- [ ] **Variáveis de Ambiente**: Todas as variáveis necessárias definidas
- [ ] **Segredo JWT**: Mínimo 32 caracteres, único por ambiente
- [ ] **Credenciais do Banco**: Senhas fortes, únicas por ambiente
- [ ] **Senha do Redis**: Senha segura configurada
- [ ] **Sem Valores Hardcoded**: Sem segredos no código fonte
- [ ] **Endpoints do Actuator**: Adequadamente restritos por ambiente
- [ ] **Detalhes de Saúde**: Nível de exposição apropriado definido

### Validação de Segurança em Runtime
- [ ] **Autenticação**: Endpoint de login funcionando corretamente
- [ ] **Autorização**: Endpoints protegidos requerem JWT válido
- [ ] **Health Checks**: Componentes de segurança mostrando como saudáveis
- [ ] **Tratamento de Erros**: Sem informações sensíveis em respostas de erro
- [ ] **Logging**: Eventos de segurança adequadamente logados (sem dados sensíveis)

## Troubleshooting de Problemas de Segurança

### Problemas Comuns

#### 1. Aplicação Não Inicia
```bash
# Verificar se todas as variáveis de ambiente necessárias estão definidas
env | grep -E "(ADMIN_|JWT_|DB_|REDIS_)"

# Verificar comprimento do segredo JWT
echo -n "$JWT_SECRET" | wc -c  # Deve ser >= 32
```

#### 2. Falhas de Autenticação
```bash
# Testar credenciais
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"'$ADMIN_USERNAME'","password":"'$ADMIN_PASSWORD'"}'

# Verificar configuração do segredo JWT
grep -r "JWT_SECRET" src/main/resources/
```

#### 3. Problemas de Acesso ao Actuator
```bash
# Verificar configuração do actuator por ambiente
curl http://localhost:8080/actuator/health
curl http://localhost:9090/actuator/health  # Porta de gerenciamento em produção
```

## Melhores Práticas de Segurança

### Desenvolvimento
1. **Nunca commitar arquivos `.env`** no controle de versão
2. **Usar segredos diferentes** para cada ambiente
3. **Testar configurações de segurança** regularmente
4. **Monitorar logs de segurança** para atividade suspeita

### Produção
1. **Usar portas separadas** para endpoints do actuator
2. **Implementar segurança a nível de rede** (firewalls, VPNs)
3. **Cronograma de rotação regular** de credenciais
4. **Setup de monitoramento e alertas** de segurança

### Diretrizes do Time
1. **Treinamento de segurança** para todos os membros do time
2. **Foco em revisão de código** nas configurações de segurança
3. **Procedimentos de resposta a incidentes** para problemas de segurança
4. **Avaliações e atualizações regulares** de segurança

## Documentação Relacionada

- **Documentação Principal**: [README do Projeto](../../README-PT.md)
- **Setup de Ambiente**: [Guia de Configuração](../../configuration/pt/configuracao-ambiente.md)
- **Onboarding do Time**: [Setup de Segurança](../../onboarding/pt/integracao-time.md#passo-2-verificar-segurança)
- **Monitoramento de Segurança**: [Guia de Monitoramento](../../monitoring/pt/)

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Security Configuration in English](../en/security-config.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
