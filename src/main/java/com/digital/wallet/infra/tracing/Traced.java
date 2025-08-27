package com.digital.wallet.infra.tracing;

import io.micrometer.observation.annotation.Observed;
import java.lang.annotation.*;

/**
 * A convenience annotation for adding distributed tracing to methods.
 * This annotation combines Spring's Observed annotation with our standard naming convention.
 * 
 * Example usage:
 * @Traced(operation = "createWallet")
 * public Wallet createWallet(Wallet wallet) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Observed(name = "wallet.operation", 
          contextualName = "#{@class.simpleName}.#{@methodName}", 
          lowCardinalityKeyValues = {"operation", "#{#root.args[0]}"})
public @interface Traced {
    /**
     * The operation being traced, which will be added as a tag to the span.
     * @return operation name
     */
    String operation() default "";
}
