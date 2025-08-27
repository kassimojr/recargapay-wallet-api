# 🚀 Checklist de Integração do Time

Bem-vindo ao time da Digital Wallet API! Use este checklist abrangente para garantir que você esteja devidamente configurado com todas as ferramentas, configurações e conhecimento necessários para contribuir efetivamente.

## 📋 Checklist de Início Rápido

### ✅ Pré-requisitos
- [ ] Repositório Git clonado localmente
- [ ] Java 21+ instalado
- [ ] Maven 3.6+ instalado
- [ ] Docker e Docker Compose instalados (para banco de dados e Redis)
- [ ] Sua IDE favorita configurada (IntelliJ IDEA, VS Code, etc.)

### ✅ Configuração do Ambiente

#### Passo 1: Criar Configuração Local
- [ ] Copiar template de ambiente: `cp src/main/resources/templates/.env.template .env`
- [ ] Editar arquivo `.env` com seus valores pessoais:
  ```bash
  # Configuração do Banco de Dados
  DB_USERNAME=seu_usuario_banco_dados
  DB_PASSWORD=sua_senha_banco_dados
  
  # Configuração de Segurança
  JWT_SECRET=sua_chave_secreta_jwt_pelo_menos_32_caracteres
  ADMIN_USERNAME=seu_usuario_admin
  ADMIN_PASSWORD=sua_senha_admin
  
  # Configuração do Redis
  REDIS_PASSWORD=sua_senha_redis
  ```

#### Passo 2: Verificar Segurança
- [ ] Confirmar que `.env` está no `.gitignore` (não deve ser commitado)
- [ ] Usar senhas fortes (mínimo 12 caracteres)
- [ ] Usar JWT secret único (mínimo 32 caracteres)
- [ ] Nunca compartilhar seu arquivo `.env` com outros

#### Passo 3: Iniciar Serviços
- [ ] Iniciar todos os serviços: `docker-compose up -d`
- [ ] Verificar se os serviços estão rodando:
  ```bash
  docker ps  # Deve mostrar postgres, redis, loki, grafana, promtail
  ```
- [ ] Verificar saúde dos serviços:
  ```bash
  curl http://localhost:8080/actuator/health
  ```

#### Passo 4: Testar Aplicação
- [ ] Iniciar aplicação: `./mvnw spring-boot:run`
- [ ] Verificar se a aplicação iniciou sem erros
- [ ] Testar endpoint de saúde: `curl http://localhost:8080/actuator/health`
- [ ] Testar autenticação:
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}'
  ```
- [ ] Confirmar que o token JWT é retornado

### ✅ Entendendo a Arquitetura

#### Conceitos Fundamentais
- [ ] **Arquitetura Hexagonal**: Entender o padrão ports and adapters
- [ ] **Domain-Driven Design**: Compreender agregados, entidades e value objects
- [ ] **Clean Architecture**: Separação de responsabilidades por camadas
- [ ] **CQRS Pattern**: Separação entre comandos e queries
- [ ] **Event Sourcing**: Como eventos são utilizados no sistema

#### Estrutura do Código
- [ ] **Camada de Domínio**: Lógica de negócio (`com.digital.wallet.domain`)
- [ ] **Camada de Aplicação**: Casos de uso (`com.digital.wallet.application`)
- [ ] **Camada de Infraestrutura**: Integrações externas (`com.digital.wallet.infra`)
- [ ] **Camada de API**: Controllers REST e DTOs (`com.digital.wallet.api`)

### ✅ Fluxo de Desenvolvimento

#### Qualidade de Código
- [ ] Executar cobertura de código: `./mvnw jacoco:report`
- [ ] Verificar relatório de cobertura em `target/site/jacoco/index.html`
- [ ] Executar análise estática: `./mvnw spotbugs:check`
- [ ] Executar testes: `./mvnw test`
- [ ] Verificar se todos os testes passam

#### Git Workflow
- [ ] Criar branch para sua feature: `git checkout -b feature/sua-feature`
- [ ] Fazer commits pequenos e descritivos
- [ ] Seguir convenção de commits: `feat:`, `fix:`, `docs:`, etc.
- [ ] Fazer push da branch: `git push origin feature/sua-feature`
- [ ] Criar pull requests para revisão de código
- [ ] Garantir que o pipeline CI/CD passe

### ✅ Ferramentas e Recursos

#### Ferramentas de Desenvolvimento
- [ ] **Testes de API**: Importar [Coleção Postman](../../collections/postman/) ou [Coleção Insomnia](../../collections/insomnia/)
- [ ] **Banco de Dados**: Acessar PostgreSQL via `localhost:5432`
- [ ] **Cache**: Acessar Redis via `localhost:6379`
- [ ] **Monitoramento**: Acessar Grafana via `http://localhost:3000`
- [ ] **Logs**: Acessar Loki via Grafana para consultas de log

#### Documentação Essencial
- [ ] **Guia de Configuração**: [Setup de Ambiente](../../configuration/pt/)
- [ ] **Guia de Segurança**: [Configuração de Segurança](../../security/pt/)
- [ ] **Guia de Monitoramento**: [Setup de Observabilidade](../../monitoring/pt/)
- [ ] **Guia de Tracing**: [Rastreamento Distribuído](../../tracing/pt/)

### ✅ Integração com o Time

#### Comunicação
- [ ] Entrar nos canais de comunicação do time
- [ ] Apresentar-se para os membros da equipe
- [ ] Agendar sessão de onboarding com tech lead
- [ ] Participar das reuniões diárias (daily standup)

#### Cultura e Práticas
- [ ] Revisar padrões de código do time
- [ ] Entender processo de code review
- [ ] Fazer perguntas - o time está aqui para ajudar!
- [ ] Documentar melhorias ou sugestões que você tenha

### ✅ Primeiras Contribuições

#### Tarefas para Começar
- [ ] Corrigir um pequeno bug ou erro de digitação
- [ ] Melhorar documentação ou comentários
- [ ] Otimizar uma query ou uso de cache

#### Caminho de Escalação
1. **Questões Técnicas**: Perguntar para membros do time ou tech lead
2. **Problemas de Configuração**: Consultar documentação ou pedir ajuda
3. **Questões de Arquitetura**: Revisar documentação ou agendar sessão de arquitetura
4. **Problemas Urgentes**: Contatar líder técnico imediatamente

### 🎯 Critérios de Sucesso

Você está pronto para contribuir quando conseguir:
- [ ] Iniciar a aplicação localmente sem erros
- [ ] Executar todos os testes com sucesso
- [ ] Fazer uma chamada de API autenticada
- [ ] Acessar ferramentas de monitoramento e logging
- [ ] Criar uma funcionalidade simples ou correção seguindo padrões do time

### 📞 Contatos de Emergência

- **Problemas de Segurança**: Contatar time de segurança imediatamente
- **Problemas de Produção**: Seguir procedimentos de resposta a incidentes
- **Questões Urgentes**: Contatar tech lead ou gerente de engenharia

---

🎉 **Bem-vindo ao time!** Você agora está pronto para começar a contribuir com a Digital Wallet API. Não hesite em fazer perguntas e compartilhar suas ideias!

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [Team Onboarding in English](../en/team-onboarding.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
