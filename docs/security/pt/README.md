# 🔒 Segurança & Autenticação

Esta seção cobre todos os aspectos de segurança da RecargaPay Wallet API, incluindo autenticação, autorização e melhores práticas de segurança.

## 📋 Navegação Rápida

| 📄 Documento | 📝 Descrição | 🎯 Público |
|--------------|--------------|------------|
| [Configuração de Segurança](configuracao-seguranca.md) | Setup e configuração completa de segurança | Desenvolvedores, DevOps |
| [Autenticação JWT](configuracao-seguranca.md) | Configuração e uso de tokens JWT | Desenvolvedores |
| [Segurança de Ambiente](../../configuration/pt/configuracao-ambiente.md#configuração-de-segurança) | Configurações de segurança por ambiente | DevOps, SysAdmin |

## 🎯 Visão Geral de Segurança

### Autenticação & Autorização
- **Autenticação baseada em JWT** com segredos configuráveis
- **Controle de acesso baseado em roles** para diferentes endpoints
- **Segurança a nível de método** com anotações
- **Proteção de endpoints do actuator** por ambiente

### Headers de Segurança
- **Configuração CORS** para requisições cross-origin
- **Headers de segurança** configurados automaticamente
- **Implementação de Content Security Policy**
- **Proteção XSS e CSRF**

### Segurança de Ambiente
- **Sem credenciais hardcoded** no código fonte
- **Validação de variáveis de ambiente** na inicialização
- **Segurança fail-fast** - app não inicia sem configuração adequada
- **Diferentes níveis de segurança** por ambiente (dev/test/hml/prod)

## 🚀 Início Rápido

### Para Desenvolvedores
1. **Setup JWT**: [Configuração JWT](configuracao-seguranca.md)
2. **Configurar ambiente**: [Configuração de Segurança](configuracao-seguranca.md)
3. **Testar autenticação**: Usar coleções de API fornecidas

### Para DevOps
1. **Setup de ambiente**: [Segurança de Ambiente](../../configuration/pt/configuracao-ambiente.md#configuração-de-segurança)
2. **Hardening de produção**: [Config de Segurança](configuracao-seguranca.md)
3. **Setup de monitoramento**: [Guia de Monitoramento](../../monitoring/pt/README.md)

## 🔗 Documentação Relacionada

- **🏠 Documentação Principal**: [README do Projeto](../../README-PT.md)
- **⚙️ Configuração**: [Setup de Ambiente](../../configuration/pt/)
- **🚀 Onboarding**: [Setup do Time](../../onboarding/pt/)
- **📊 Monitoramento**: [Guia de Monitoramento](../../monitoring/pt/README.md)

## 🛡️ Melhores Práticas de Segurança

### Desenvolvimento
- Sempre usar variáveis de ambiente para dados sensíveis
- Nunca commitar arquivos `.env` ou segredos
- Usar segredos JWT fortes (mínimo 256 bits)
- Testar configurações de segurança regularmente

### Produção
- Usar portas separadas para gerenciamento do actuator
- Restringir endpoints do actuator ao mínimo necessário
- Implementar segurança de rede adequada
- Monitorar eventos de segurança e padrões de acesso

---

## 🌍 Versões de Idioma

- 🇧🇷 **Português**: Você está aqui!
- 🇺🇸 **English**: [English README](../en/README.md)

---

*Para mais informações, consulte a [documentação principal do projeto](../../README-PT.md).*
