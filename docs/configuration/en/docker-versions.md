# Docker Image Versions

This document tracks the Docker image versions used in the RecargaPay Wallet API project and the rationale behind version selection.

## Current Image Versions

| Service | Image | Version | Rationale |
|---------|-------|---------|-----------|
| **PostgreSQL** | `postgres` | `16` | LTS version, stable and widely adopted |
| **Redis** | `redis` | `7-alpine` | Latest stable major version, Alpine for smaller footprint |
| **SonarQube** | `sonarqube` | `25.7.0.110598-community` | Latest stable community edition |
| **Prometheus** | `prom/prometheus` | `v2.45.6` | LTS version, proven stability in production |
| **Grafana** | `grafana/grafana` | `11.2.0` | Stable version with modern UI and features |
| **Loki** | `grafana/loki` | `3.1.1` | Stable version with performance improvements |
| **Promtail** | `grafana/promtail` | `3.1.1` | Must match Loki version for compatibility |
| **Tempo** | `grafana/tempo` | `2.4.2` | Stable version, compatible with current stack |

## Version Selection Criteria

### 🎯 **Primary Criteria**
- **Stability**: Proven track record in production environments
- **Compatibility**: Inter-service compatibility within the observability stack
- **Security**: Regular security updates and patches
- **Community Support**: Active maintenance and community adoption

### 🚫 **Why We Avoid `latest`**
- **Unpredictable Updates**: Automatic updates can introduce breaking changes
- **Non-Reproducible Builds**: Different environments may have different versions
- **Debugging Complexity**: Unknown exact version makes troubleshooting difficult
- **Production Risk**: Uncontrolled updates in production environments

## Compatibility Matrix

### Grafana Stack Compatibility
- **Loki 3.1.1** ↔ **Promtail 3.1.1**: Same version required for protocol compatibility
- **Grafana 11.2.0** ↔ **Loki 3.1.1**: Fully compatible, tested combination
- **Grafana 11.2.0** ↔ **Tempo 2.4.2**: Compatible for distributed tracing

### Monitoring Stack
- **Prometheus v2.45.6** ↔ **Grafana 11.2.0**: Standard integration, well-tested
- **All services**: Compatible with Docker Compose health checks

## Update Strategy

### 🔄 **Regular Updates**
1. **Monthly Review**: Check for security updates and patches
2. **Quarterly Evaluation**: Assess new stable versions
3. **Testing Protocol**: Always test in development before production
4. **Documentation**: Update this document with any version changes

### 📋 **Update Checklist**
- [ ] Check release notes for breaking changes
- [ ] Verify compatibility with dependent services
- [ ] Test in development environment
- [ ] Update documentation
- [ ] Validate observability pipeline functionality
- [ ] Update CI/CD pipelines if needed

## Version History

| Date | Service | Old Version | New Version | Reason |
|------|---------|-------------|-------------|---------|
| 2025-08-13 | Prometheus | `latest` | `v2.45.6` | Initial version pinning for stability |
| 2025-08-13 | Grafana | `latest` | `10.4.7` | Initial version pinning for stability |
| 2025-08-13 | Loki | `latest` | `2.9.8` | Initial version pinning for stability |
| 2025-08-13 | Promtail | `latest` | `2.9.8` | Initial version pinning for compatibility |
| 2025-08-13 | Tempo | `latest` | `2.4.2` | Initial version pinning for stability |
| 2025-08-15 | Prometheus | `v2.45.6` | `v2.45.6` | Maintained stable LTS version |
| 2025-08-15 | Grafana | `10.4.7` | `11.2.0` | Updated to verified stable version |
| 2025-08-15 | Loki | `2.9.8` | `3.1.1` | Updated to stable version with compatibility |
| 2025-08-15 | Promtail | `2.9.8` | `3.1.1` | Updated to match Loki version |
| 2025-08-15 | Tempo | `2.4.2` | `2.4.2` | Maintained stable version |

## Troubleshooting

### Common Issues After Version Updates

#### **Service Won't Start**
```bash
# Check container logs
docker-compose logs [service-name]

# Verify image availability
docker pull [image:version]
```

#### **Compatibility Issues**
```bash
# Restart all services in correct order
docker-compose down
docker-compose up -d

# Check service health
docker-compose ps
```

#### **Data Migration**
- **Loki**: Data format is backward compatible
- **Grafana**: Dashboard configurations are preserved
- **Prometheus**: Metrics data is backward compatible

## References

- [Prometheus Release Notes](https://github.com/prometheus/prometheus/releases)
- [Grafana Release Notes](https://github.com/grafana/grafana/releases)
- [Loki Release Notes](https://github.com/grafana/loki/releases)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

> **Note**: This document should be updated whenever Docker image versions are changed. Always test version updates in development before applying to production.

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Versões Docker](../pt/docker-versions.md)

---

*For more information, see the [main project documentation](../../../README.md).*
