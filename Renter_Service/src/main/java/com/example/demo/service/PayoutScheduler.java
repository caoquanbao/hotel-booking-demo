package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutScheduler {

    private final PayoutService payoutService;

    /**
     * Run payout batch at 02:00 AM on day 10 every month.
     */
    @Scheduled(cron = "0 0 2 10 * ?")
    public void runMonthlyPayout() {
        log.info("Starting monthly payout batch...");
        payoutService.processMonthlyPayoutBatch();
        log.info("Monthly payout batch done.");
    }
}
