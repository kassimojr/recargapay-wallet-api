# Automação SonarQube com Quality Gate

## 🎯 Visão Geral

Este documento descreve a solução completa de automação para integração do SonarQube com a RecargaPay Wallet API, incluindo geração automática de tokens, validação de cobertura e quality gates que bloqueiam o deployment se a cobertura estiver abaixo de 90%.

## 🔧 Problema Resolvido

**Problema Original**: SonarQube força mudança de senha no primeiro login, quebrando a automação quando volumes Docker são resetados.

**Solução**: Automação completa que gerencia:
- ✅ Gerenciamento automático de credenciais (lida com mudanças de senha)
- ✅ Geração dinâmica de tokens via API
- ✅ Aplicação de quality gate (requisito de 90% de cobertura)
- ✅ Resistência a resets de volume e reinicializações de container
- ✅ Relatórios detalhados de erro e troubleshooting

## 🚀 Como Funciona

### 1. **Configuração Docker Compose**
```yaml
sonarqube:
  image: sonarqube:10.4-community
  environment:
    - SONAR_FORCEAUTHENTICATION=false
    - SONAR_SECURITY_REALM=
    - SONAR_WEB_JAVAADDITIONALOPTS=-Dsonar.web.javaAdditionalOpts=-Dsonar.security.realm=
```

### 2. **Gerenciamento Automático de Credenciais**
O sistema automaticamente:
- Tenta credenciais padrão (`admin:admin`)
- Se forçado a mudar, usa `admin:admin123`
- Lida com ambos cenários transparentemente
- Funciona após resets de volume

### 3. **Processo de Geração de Token**
```bash
# Geração automática de token via API
curl -X POST -u "admin:admin" \
  -d "name=wallet-api-automation-token" \
  -d "type=USER_TOKEN" \
  "http://localhost:9000/api/user_tokens/generate"
```

### 4. **Aplicação de Quality Gate**
```bash
# Comando Maven com validação de cobertura
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=recargapay-wallet-api \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<token-gerado> \
  -s settings.xml
```

## 📋 Uso

### **Inicialização Automatizada (Recomendado)**
```bash
./wallet-api-startup.sh
```

O script automaticamente:
1. ✅ Inicia container SonarQube
2. ✅ Aguarda SonarQube ficar pronto
3. ✅ Configura credenciais automaticamente
4. ✅ Gera token de autenticação
5. ✅ Executa build Maven com análise SonarQube
6. ✅ Valida cobertura >= 90%
7. ✅ **BLOQUEIA deployment se cobertura < 90%**

### **Inicialização Manual do SonarQube**
```bash
./scripts/init-sonarqube.sh
```

## 🔍 Detalhes do Quality Gate

### **Requisito de Cobertura**
- **Mínimo**: 90% de cobertura de instruções
- **Aplicação**: Bloqueio rígido (script sai com código de erro 1)
- **Validação**: Tempo real via API SonarQube

### **Quando Cobertura < 90%**
```
❌ Requisito de cobertura NÃO ATENDIDO! (85.2% < 90%)
🚫 Quality Gate FALHOU - Deployment bloqueado

📈 Para corrigir este problema:
   1. Adicione mais testes unitários para aumentar a cobertura
   2. Foque em classes e métodos não testados
   3. Execute 'mvn clean test jacoco:report' para ver relatório detalhado
   4. Verifique target/site/jacoco/index.html para detalhes de cobertura
```

### **Quando Cobertura >= 90%**
```
✅ Requisito de cobertura ATENDIDO! (92.5% >= 90%)
🎉 Quality Gate PASSOU - Prosseguindo com deployment
```

## 🛠️ Implementação Técnica

### **Componentes Principais**

1. **`wait_for_sonarqube_ready()`**
   - Aguarda até 5 minutos para inicialização do SonarQube
   - Verifica endpoint `/api/system/status`
   - Garante prontidão operacional completa

2. **`setup_sonarqube_credentials()`**
   - Lida com credenciais padrão (`admin:admin`)
   - Gerencia requisito de mudança de senha
   - Retorna credenciais funcionais para geração de token

3. **`get_sonarqube_token()`**
   - Gera novo token de autenticação via API
   - Lida com conflitos de token (revoga existente se necessário)
   - Retorna token válido para análise Maven

4. **`validate_coverage_from_api()`**
   - Recupera dados de cobertura via API SonarQube
   - Aguarda processamento da análise (até 2 minutos)
   - Retorna porcentagem precisa de cobertura

### **Integração Maven**
```xml
<!-- pom.xml - Configuração SonarQube -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</plugin>
```

## 🔧 Arquivos de Configuração

### **Docker Compose**
- **Arquivo**: `docker-compose.yml`
- **Serviço**: `sonarqube`
- **Configurações Principais**: Autenticação forçada desabilitada

### **Configurações Maven**
- **Arquivo**: `settings.xml`
- **Propósito**: Configuração Maven para integração SonarQube

### **Script de Inicialização**
- **Arquivo**: `scripts/init-sonarqube.sh`
- **Propósito**: Configuração manual do SonarQube e quality gate

## 🚨 Troubleshooting

### **Problemas Comuns**

#### **SonarQube Não Iniciando**
```bash
# Verificar status do container
docker-compose ps sonarqube

# Verificar logs
docker-compose logs sonarqube

# Reiniciar se necessário
docker-compose restart sonarqube
```

#### **Falha na Geração de Token**
```bash
# Verificar acessibilidade da API SonarQube
curl -s "http://localhost:9000/api/system/status"

# Geração manual de token
curl -X POST -u "admin:admin" \
  -d "name=test-token" \
  "http://localhost:9000/api/user_tokens/generate"
```

#### **Dados de Cobertura Não Disponíveis**
```bash
# Verificar se análise foi concluída
curl -s -u "admin:admin" \
  "http://localhost:9000/api/measures/component?component=recargapay-wallet-api&metricKeys=coverage"

# Executar análise manualmente
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### **Recuperação de Reset de Volume**
Quando volumes Docker são resetados:
1. ✅ Script detecta reset automaticamente
2. ✅ Reconfigura credenciais
3. ✅ Regenera tokens
4. ✅ Continua normalmente

## 📊 Monitoramento e Relatórios

### **Dashboard SonarQube**
- **URL**: http://localhost:9000
- **Projeto**: `recargapay-wallet-api`
- **Credenciais**: `admin:admin` ou `admin:admin123`

### **Relatórios de Cobertura**
- **Jacoco HTML**: `target/site/jacoco/index.html`
- **SonarQube**: http://localhost:9000/dashboard?id=recargapay-wallet-api

### **Arquivos de Log**
- **Startup**: `logs/startup_YYYYMMDD_HHMMSS.log`
- **Maven**: `logs/maven_YYYYMMDD_HHMMSS.log`
- **SonarQube**: `logs/sonar_YYYYMMDD_HHMMSS.log`

## ✅ Critérios de Sucesso

A automação é bem-sucedida quando:
- ✅ SonarQube inicia automaticamente
- ✅ Credenciais são gerenciadas transparentemente
- ✅ Geração de token funciona consistentemente
- ✅ Validação de cobertura é precisa
- ✅ Quality gate bloqueia deployment quando cobertura < 90%
- ✅ Sistema se recupera de resets de volume
- ✅ Mensagens de erro detalhadas orientam desenvolvedores

## 🎉 Benefícios

1. **Zero Intervenção Manual**: Integração SonarQube totalmente automatizada
2. **Aplicação de Qualidade**: Bloqueio rígido em cobertura insuficiente
3. **Resiliente**: Lida graciosamente com resets de infraestrutura
4. **Amigável ao Desenvolvedor**: Mensagens de erro claras e orientação
5. **Pronto para CI/CD**: Perfeito para pipelines automatizados
6. **Consistente**: Mesmo comportamento em todos os ambientes

---

## 🔗 Documentação Relacionada

- [Onboarding da Equipe](../../onboarding/pt/team-onboarding.md)
- [Configuração de Ambiente](configuracao-ambiente.md)
- [Versões Docker](docker-versions.md)

---

*Esta automação garante que apenas código de alta qualidade com cobertura de teste adequada chegue à produção, mantendo a confiabilidade e manutenibilidade da RecargaPay Wallet API.*

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [SonarQube Automation](../en/sonarqube-automation.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../../docs/README-PT.md).*
