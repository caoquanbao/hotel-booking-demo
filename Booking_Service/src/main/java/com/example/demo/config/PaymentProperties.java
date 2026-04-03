package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    private String mode = "mock";
    private String callbackUrl;
    private String returnUrl;

    private final Momo momo = new Momo();
    private final Vnpay vnpay = new Vnpay();

    @Data
    public static class Momo {
        private String mode = "mock";
        private String partnerCode;
        private String accessKey;
        private String secretKey;
        private String createUrl;
    }

    @Data
    public static class Vnpay {
        private String mode = "mock";
        private String tmnCode;
        private String secretKey;
        private String payUrl;
    }
}
