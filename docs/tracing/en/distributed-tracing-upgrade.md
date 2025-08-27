# Distributed Tracing Upgrade Plan

## 📋 Overview

This document outlines the complete upgrade plan to implement **granular distributed tracing** in the Digital Wallet API, moving from the current single-span-per-request approach to a multi-span hierarchical tracing system.

## 🎯 Upgrade Goals

### Current State
```
TraceId: abc123... 
SpanId: span001 (same for entire request)

HTTP Request → Controller → Service → Repository → Response
     ↓             ↓          ↓          ↓           ↓
  span001       span001    span001    span001    span001
```

### Target State
```
TraceId: abc123... (same for entire transaction)

┌─ SpanId: span001 (HTTP Request - WalletController.deposit)
│  ├─ SpanId: span002 (Business Logic - DepositService.deposit)  
│  │  ├─ SpanId: span003 (Validation - DepositService.validateParams)
│  │  └─ SpanId: span004 (Database - WalletRepository.save)
│  └─ SpanId: span005 (Response Processing - Controller)
```

## 🔧 Implementation Complexity

**Estimated Effort**: 4-6 days
- **Development**: 2-3 days
- **Testing**: 1-2 days  
- **Documentation**: 0.5 day
- **Integration**: 0.5 day

**Complexity Level**: **MEDIUM-HIGH**

## 📦 Dependencies Changes

### Add to pom.xml

```xml
<!-- Micrometer Observation API for @Observed annotations -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-observation</artifactId>
</dependency>

<!-- Micrometer-OpenTelemetry bridge -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

<!-- AOP support for @Observed -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
```

## 🏗️ Architecture Changes

### 1. Configuration Layer

#### New File: `TracingConfig.java`
```java
@Configuration
@EnableAspectJAutoProxy
public class TracingConfig {
    
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
    
    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
    
    @Bean
    public SpanContextUpdater spanContextUpdater() {
        return new SpanContextUpdater();
    }
}
```

#### Modified: `OpenTelemetryConfig.java`
```java
@Configuration
public class OpenTelemetryConfig {
    
    @Bean
    public OpenTelemetry openTelemetry(ObservationRegistry observationRegistry) {
        // ... existing configuration ...
        
        // Add observation registry integration
        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(contextPropagators)
            .buildAndRegisterGlobal();
    }
}
```

### 2. Span Context Management

#### New File: `SpanContextUpdater.java`
```java
@Component
public class SpanContextUpdater implements ObservationHandler<Observation.Context> {
    
    @Override
    public void onStart(Observation.Context context) {
        Span currentSpan = Span.current();
        if (currentSpan.getSpanContext().isValid()) {
            String traceId = currentSpan.getSpanContext().getTraceId();
            String spanId = currentSpan.getSpanContext().getSpanId();
            
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
        }
    }
    
    @Override
    public void onStop(Observation.Context context) {
        // Restore parent span context if exists
        Span parentSpan = getParentSpan();
        if (parentSpan != null && parentSpan.getSpanContext().isValid()) {
            MDC.put("spanId", parentSpan.getSpanContext().getSpanId());
        }
    }
    
    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
```

### 3. Service Layer Changes

#### DepositService.java
```java
@Service
public class DepositService implements DepositUseCase {
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Observed(name = "wallet.deposit", contextualName = "wallet-deposit")
    public Transaction deposit(UUID walletId, BigDecimal amount) {
        return Observation.createNotStarted("wallet.deposit.validation", observationRegistry)
            .observe(() -> {
                validateDepositParams(walletId, amount);
                return executeDeposit(walletId, amount);
            });
    }
    
    @Observed(name = "wallet.deposit.execute", contextualName = "wallet-deposit-execute")
    private Transaction executeDeposit(UUID walletId, BigDecimal amount) {
        // Implementation with automatic span creation
    }
    
    @Observed(name = "wallet.deposit.validate", contextualName = "wallet-deposit-validate")
    private void validateDepositParams(UUID walletId, BigDecimal amount) {
        // Validation logic with automatic span creation
    }
}
```

#### WithdrawService.java
```java
@Service
public class WithdrawService implements WithdrawUseCase {
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Observed(name = "wallet.withdraw", contextualName = "wallet-withdraw")
    public Transaction withdraw(UUID walletId, BigDecimal amount) {
        // Similar pattern to DepositService
    }
}
```

#### TransferFundsService.java
```java
@Service
public class TransferFundsService implements TransferFundsUseCase {
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Observed(name = "wallet.transfer", contextualName = "wallet-transfer")
    public List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        return Observation.createNotStarted("wallet.transfer.execution", observationRegistry)
            .observe(() -> {
                // Create child spans for each major operation
                validateTransfer(fromWalletId, toWalletId, amount);
                return executeTransfer(fromWalletId, toWalletId, amount);
            });
    }
}
```

### 4. Repository Layer Changes

#### WalletRepositoryImpl.java
```java
@Repository
@Primary
public class WalletRepositoryImpl implements WalletRepository {
    
    @Override
    @Observed(name = "db.wallet.findById", contextualName = "db-wallet-findById")
    public Optional<Wallet> findById(UUID walletId) {
        // Database operation with automatic span creation
    }
    
    @Override
    @Observed(name = "db.wallet.save", contextualName = "db-wallet-save")
    public Wallet save(Wallet wallet) {
        // Database operation with automatic span creation
    }
    
    @Override
    @Observed(name = "db.wallet.createTransaction", contextualName = "db-wallet-createTransaction")
    public Transaction createTransaction(UUID walletId, BigDecimal amount, 
                                       TransactionType type, UUID userId, LocalDateTime timestamp) {
        // Database operation with automatic span creation
    }
}
```

### 5. Controller Layer Changes

#### WalletController.java
```java
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDTO> deposit(@Valid @RequestBody DepositRequestDTO dto) {
        return Observation.createNotStarted("http.wallet.deposit", observationRegistry)
            .contextualName("http-wallet-deposit")
            .observe(() -> {
                Transaction transaction = depositUseCase.deposit(dto.getWalletId(), dto.getAmount());
                return ResponseEntity.ok(transactionMapper.toDTO(transaction));
            });
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO dto) {
        return Observation.createNotStarted("http.wallet.withdraw", observationRegistry)
            .contextualName("http-wallet-withdraw")
            .observe(() -> {
                Transaction transaction = withdrawUseCase.withdraw(dto.getWalletId(), dto.getAmount());
                return ResponseEntity.ok(transactionMapper.toDTO(transaction));
            });
    }
}
```

### 6. Utility Classes

#### New File: `TracingUtils.java`
```java
@Component
public class TracingUtils {
    
    private final ObservationRegistry observationRegistry;
    
    public TracingUtils(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }
    
    public static <T> T withSpan(String spanName, Supplier<T> operation) {
        return Observation.createNotStarted(spanName, observationRegistry)
            .observe(operation);
    }
    
    public static void withSpan(String spanName, Runnable operation) {
        Observation.createNotStarted(spanName, observationRegistry)
            .observe(operation);
    }
    
    public static void updateMDCWithCurrentSpan() {
        Span currentSpan = Span.current();
        if (currentSpan.getSpanContext().isValid()) {
            MDC.put("traceId", currentSpan.getSpanContext().getTraceId());
            MDC.put("spanId", currentSpan.getSpanContext().getSpanId());
        }
    }
}
```

## 📊 Expected Results

### Log Output Example (After Upgrade)

```
2025-07-22T03:27:20.123-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,6425741b3b8a7581] [http-nio-8080-exec-1] c.r.w.i.logging.TraceContextFilter : Request received: POST /api/v1/wallets/deposit

2025-07-22T03:27:20.125-03:00 INFO  [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,7f3a8b2c9d1e4f56] [http-nio-8080-exec-1] c.r.w.adapter.controllers.v1.WalletController : Starting deposit operation

2025-07-22T03:27:20.127-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,8e4b9c3d0f2a5g67] [http-nio-8080-exec-1] c.r.w.core.services.DepositService : Validating deposit parameters

2025-07-22T03:27:20.130-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,9f5c0d4e1g3b6h78] [http-nio-8080-exec-1] c.r.w.adapter.repositories.impl.WalletRepositoryImpl : Executing database query

2025-07-22T03:27:20.145-03:00 INFO  [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,7f3a8b2c9d1e4f56] [http-nio-8080-exec-1] c.r.w.adapter.controllers.v1.WalletController : Deposit completed successfully
```

### Key Improvements

- **Same traceId**: `6ed5c3ad90a37e0437be0bf3f15cb9d6` across all operations
- **Different spanIds**: Each layer/operation has unique spanId
- **Hierarchical structure**: Parent-child relationship between spans
- **Granular timing**: Measure time spent in each component

## 🧪 Testing Strategy

### 1. Unit Tests

#### TracingConfigTest.java
```java
@SpringBootTest
class TracingConfigTest {
    
    @Test
    void shouldCreateObservedAspect() {
        // Test aspect creation and configuration
    }
    
    @Test
    void shouldCreateObservationRegistry() {
        // Test registry creation and configuration
    }
}
```

#### SpanContextUpdaterTest.java
```java
@ExtendWith(MockitoExtension.class)
class SpanContextUpdaterTest {
    
    @Test
    void shouldUpdateMDCOnSpanStart() {
        // Test MDC update when span starts
    }
    
    @Test
    void shouldRestoreParentContextOnSpanStop() {
        // Test parent context restoration
    }
}
```

### 2. Integration Tests

#### DistributedTracingIntegrationTest.java
```java
@SpringBootTest
@AutoConfigureTestDatabase
class DistributedTracingIntegrationTest {
    
    @Test
    void shouldCreateSpanHierarchyForDepositOperation() {
        // Test complete span hierarchy creation
        // Verify traceId consistency
        // Verify spanId uniqueness
        // Verify parent-child relationships
    }
    
    @Test
    void shouldMaintainTraceContextAcrossLayers() {
        // Test trace context propagation
    }
}
```

### 3. Performance Tests

#### TracingPerformanceTest.java
```java
@SpringBootTest
class TracingPerformanceTest {
    
    @Test
    void shouldHaveMinimalPerformanceImpact() {
        // Measure overhead of span creation
        // Compare with current implementation
        // Ensure < 5ms additional overhead
    }
}
```

## 📈 Performance Impact

### Expected Overhead

- **Additional latency**: 2-5ms per request
- **Memory usage**: +50-100KB per request (span objects)
- **CPU usage**: +5-10% (span creation and context switching)
- **Log volume**: +20-30% (more detailed spans)

### Mitigation Strategies

1. **Sampling**: Implement trace sampling for high-volume endpoints
2. **Async processing**: Use async span export to reduce latency
3. **Span filtering**: Only create spans for critical operations
4. **Resource limits**: Set maximum spans per trace

## 🚀 Migration Plan

### Phase 1: Foundation (Day 1)
1. Add dependencies to pom.xml
2. Create TracingConfig and SpanContextUpdater
3. Update OpenTelemetryConfig
4. Test basic span creation

### Phase 2: Service Layer (Day 2)
1. Add @Observed annotations to critical services
2. Implement manual span creation for complex operations
3. Test span hierarchy in services
4. Verify MDC updates

### Phase 3: Repository Layer (Day 3)
1. Add @Observed annotations to repository methods
2. Test database operation spans
3. Verify performance impact
4. Optimize if necessary

### Phase 4: Controller Layer (Day 4)
1. Implement manual observations in controllers
2. Test complete request flow
3. Verify span relationships
4. Performance testing

### Phase 5: Testing & Documentation (Days 5-6)
1. Complete unit and integration tests
2. Performance benchmarking
3. Update documentation
4. Deployment preparation

## ⚠️ Risks and Mitigation

### Risks

1. **Performance degradation**: Multiple span creation overhead
2. **Memory leaks**: Improper span lifecycle management
3. **Context loss**: MDC not properly updated
4. **Complexity**: Increased debugging difficulty

### Mitigation

1. **Performance monitoring**: Continuous performance tracking
2. **Proper cleanup**: Ensure spans are always closed
3. **Context validation**: Automated tests for context propagation
4. **Documentation**: Comprehensive troubleshooting guides

## 🔄 Rollback Plan

If issues arise, rollback can be done incrementally:

1. **Remove @Observed annotations**: Revert to current behavior
2. **Disable ObservedAspect**: Keep dependencies but disable processing
3. **Remove dependencies**: Complete rollback to current implementation

## 📋 Checklist

### Pre-Implementation
- [ ] Review current implementation thoroughly
- [ ] Understand performance requirements
- [ ] Plan testing strategy
- [ ] Set up monitoring

### Implementation
- [ ] Add dependencies
- [ ] Create configuration classes
- [ ] Implement SpanContextUpdater
- [ ] Add service layer annotations
- [ ] Add repository layer annotations
- [ ] Implement controller observations
- [ ] Create utility classes

### Testing
- [ ] Unit tests for all new components
- [ ] Integration tests for span hierarchy
- [ ] Performance tests
- [ ] Load testing
- [ ] Memory leak testing

### Deployment
- [ ] Update documentation
- [ ] Create monitoring dashboards
- [ ] Prepare rollback procedures
- [ ] Deploy to staging
- [ ] Deploy to production

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Upgrade de Tracing Distribuído em Português](../pt/upgrade-tracing-distribuido.md)

---

*For more information, see the [main project documentation](../../../README.md).*
