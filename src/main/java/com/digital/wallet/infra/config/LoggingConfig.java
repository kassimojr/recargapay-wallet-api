package com.digital.wallet.infra.config;

import com.digital.wallet.core.ports.out.DomainLogger;
import com.digital.wallet.core.services.DepositService;
import com.digital.wallet.core.services.TransferFundsService;
import com.digital.wallet.core.services.WithdrawService;
import com.digital.wallet.infra.logging.JsonDomainLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application logging components
 */
@Configuration
public class LoggingConfig {
    
    @Bean
    @Qualifier("depositLogger")
    public DomainLogger depositServiceLogger() {
        return new JsonDomainLogger(DepositService.class);
    }
    
    @Bean
    @Qualifier("withdrawLogger")
    public DomainLogger withdrawServiceLogger() {
        return new JsonDomainLogger(WithdrawService.class);
    }
    
    @Bean
    @Qualifier("transferLogger")
    public DomainLogger transferFundsServiceLogger() {
        return new JsonDomainLogger(TransferFundsService.class);
    }
}
