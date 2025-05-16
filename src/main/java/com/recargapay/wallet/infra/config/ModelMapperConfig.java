package com.recargapay.wallet.infra.config;

import com.recargapay.wallet.core.domain.TransactionType;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // Configuração para tornar o mapeamento mais restrito
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        
        // Converter para transformar enum TransactionType em String
        Converter<TransactionType, String> transactionTypeToStringConverter = 
            ctx -> ctx.getSource() == null ? null : ctx.getSource().toString();
        
        // Converter para transformar String em enum TransactionType
        Converter<String, TransactionType> stringToTransactionTypeConverter = 
            ctx -> ctx.getSource() == null ? null : TransactionType.valueOf(ctx.getSource());
            
        // Registrar os conversores
        modelMapper.createTypeMap(TransactionType.class, String.class)
                .setConverter(transactionTypeToStringConverter);
        modelMapper.createTypeMap(String.class, TransactionType.class)
                .setConverter(stringToTransactionTypeConverter);
                
        return modelMapper;
    }
}
