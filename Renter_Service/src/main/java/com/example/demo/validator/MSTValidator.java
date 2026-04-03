package com.example.demo.validator;

import org.springframework.stereotype.Component;

@Component
public class MSTValidator {

    private static final int[] WEIGHTS = {31, 29, 23, 19, 17, 13, 7, 5, 3};

    public boolean isValidMST(String mst) {
        if (mst == null || mst.isBlank()) {
            return false;
        }

        String normalized = mst.trim();
        if (normalized.matches("\\d{10}")) {
            return isValidMainTaxCode(normalized);
        }

        if (normalized.matches("\\d{10}-\\d{3}")) {
            String base = normalized.substring(0, 10);
            return isValidMainTaxCode(base);
        }

        return false;
    }

    private boolean isValidMainTaxCode(String tenDigits) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            int digit = tenDigits.charAt(i) - '0';
            sum += digit * WEIGHTS[i];
        }

        int mod = sum % 11;
        int checkDigit = 10 - mod;
        if (checkDigit == 10 || checkDigit == 11) {
            checkDigit = 0;
        }

        int actual = tenDigits.charAt(9) - '0';
        return actual == checkDigit;
    }
}
