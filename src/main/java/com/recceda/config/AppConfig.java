package com.recceda.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Application configuration class for environment variables and settings
 */
@Data
@Slf4j
public class AppConfig {
    
    private final String payFastMerchantId;
    private final String payFastMerchantKey;
    private final String payFastPassphrase;
    private final boolean isSandbox;
    private final String returnUrl;
    private final String cancelUrl;
    private final String notifyUrl;
    
    public AppConfig() {
        this.payFastMerchantId = getEnvVariable("PAYFAST_MERCHANT_ID");
        this.payFastMerchantKey = getEnvVariable("PAYFAST_MERCHANT_KEY");
        this.payFastPassphrase = getEnvVariable("PAYFAST_PASSPHRASE");
        this.isSandbox = Boolean.parseBoolean(getEnvVariable("IS_SANDBOX", "true"));
        
        // URLs can be overridden via environment variables
        this.returnUrl = getEnvVariable("RETURN_URL", "https://example.com/return");
        this.cancelUrl = getEnvVariable("CANCEL_URL", "https://example.com/cancel");
        this.notifyUrl = getEnvVariable("NOTIFY_URL", "https://example.com/notify");
        
        log.info("AppConfig initialized - Sandbox mode: {}", isSandbox);
    }
    
    private String getEnvVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable not set: " + name);
        }
        return value.trim();
    }
    
    private String getEnvVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}
