package com.example.demo.validator;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Set;

@Component
public class CCCDValidator {

    private static final Set<String> VALID_PROVINCE_CODES = Set.of(
            "001", "002", "004", "006", "008", "010", "011", "012", "014", "015", "017", "019", "020",
            "022", "024", "025", "026", "027", "030", "031", "033", "034", "035", "036", "037", "038",
            "040", "042", "044", "045", "046", "048", "049", "051", "052", "054", "056", "058", "060",
            "062", "064", "066", "067", "068", "070", "072", "074", "075", "077", "079", "080", "082",
            "083", "084", "086", "087", "089", "091", "092", "093", "094", "095", "096"
    );

    public boolean isValidCCCD(String cccd) {
        if (cccd == null || !cccd.matches("\\d{12}")) {
            return false;
        }

        String provinceCode = cccd.substring(0, 3);
        if (!VALID_PROVINCE_CODES.contains(provinceCode)) {
            return false;
        }

        int genderCenturyCode = cccd.charAt(3) - '0';
        if (genderCenturyCode < 0 || genderCenturyCode > 9) {
            return false;
        }

        int yearSuffix = Integer.parseInt(cccd.substring(4, 6));
        Integer century = inferCentury(genderCenturyCode);
        if (century == null) {
            return false;
        }

        int birthYear = century + yearSuffix;
        int currentYear = Year.now().getValue();
        return birthYear >= 1900 && birthYear <= currentYear;
    }

    private Integer inferCentury(int code) {
        return switch (code) {
            case 0, 1 -> 1900;
            case 2, 3 -> 2000;
            case 4, 5 -> 2100;
            case 6, 7 -> 2200;
            case 8, 9 -> 1800;
            default -> null;
        };
    }
}
