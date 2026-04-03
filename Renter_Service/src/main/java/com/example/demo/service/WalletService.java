package com.example.demo.service;

import com.example.demo.entity.CampaignWallet;
import com.example.demo.exception.AdsAuctionException;
import com.example.demo.exception.WalletOperationException;
import com.example.demo.repository.CampaignWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private static final String BUDGET_KEY_PREFIX = "ads:wallet:";
    private static final String DEDUCT_BUDGET_LUA = """
            local current = redis.call('GET', KEYS[1])
            if (not current) then
              return -1
            end

            current = tonumber(current)
            local price = tonumber(ARGV[1])
            if (not current or not price) then
              return -2
            end

            if (current < price) then
              return 0
            end

            redis.call('INCRBYFLOAT', KEYS[1], -price)
            return 1
            """;

    /**
     * Lua script for atomic budget deduction in Redis:
     * - return 1 if deducted
     * - return 0 if insufficient budget
     * - return -1 if budget key missing
     * - return -2 if cached value or requested price is invalid
     */
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT = new DefaultRedisScript<>(DEDUCT_BUDGET_LUA, Long.class);

    private final CampaignWalletRepository campaignWalletRepository;
    private final StringRedisTemplate redisTemplate;

    public boolean checkBudget(Long campaignId, BigDecimal requiredAmount) {
        BigDecimal remaining = getRemainingBudget(campaignId);
        return remaining.compareTo(requiredAmount) >= 0;
    }

    @Transactional
    public boolean deductBudget(Long campaignId, BigDecimal price) {
        String key = redisKey(campaignId);
        log.info("Deducting wallet budget for campaignId={} key={} amount={}", campaignId, key, price);
        Long result = executeDeductScript(key, price, campaignId);
        log.info("Redis wallet script result for campaignId={} key={} amount={} result={}",
                campaignId, key, price, result);

        if (result != null && result == -1L) {
            CampaignWallet wallet = campaignWalletRepository.findById(campaignId)
                    .orElseThrow(() -> new AdsAuctionException("Campaign wallet not found: " + campaignId));
            warmupBudgetCache(campaignId, wallet.getRemainingBudget());
            log.info("Wallet cache miss for campaignId={}, warmed Redis key {} with budget={}",
                    campaignId, key, wallet.getRemainingBudget());
            result = executeDeductScript(key, price, campaignId);
            log.info("Redis wallet script result after warmup for campaignId={} key={} amount={} result={}",
                    campaignId, key, price, result);
        }

        if (result != null && result == -2L) {
            throw new WalletOperationException("Wallet cache contains invalid numeric value for campaignId=" + campaignId);
        }

        if (result == null || result == 0L) {
            return false;
        }

        if (result != 1L) {
            throw new WalletOperationException("Unexpected wallet script result " + result + " for campaignId=" + campaignId);
        }

        // Sync Redis deduction back to DB with pessimistic lock.
        CampaignWallet wallet = campaignWalletRepository.findByCampaignIdForUpdate(campaignId)
                .orElseThrow(() -> new AdsAuctionException("Campaign wallet not found: " + campaignId));

        BigDecimal nextBudget = wallet.getRemainingBudget().subtract(price);
        if (nextBudget.compareTo(BigDecimal.ZERO) < 0) {
            wallet.setIsOutOfBudget(true);
            campaignWalletRepository.save(wallet);
            return false;
        }

        wallet.setRemainingBudget(nextBudget);
        wallet.setIsOutOfBudget(nextBudget.compareTo(BigDecimal.ZERO) == 0);
        campaignWalletRepository.save(wallet);
        return true;
    }

    public void warmupBudgetCache(Long campaignId, BigDecimal budget) {
        redisTemplate.opsForValue().set(redisKey(campaignId), budget.toPlainString());
    }

    public BigDecimal getRemainingBudget(Long campaignId) {
        String cached = redisTemplate.opsForValue().get(redisKey(campaignId));
        if (cached != null) {
            return new BigDecimal(cached);
        }

        CampaignWallet wallet = campaignWalletRepository.findById(campaignId)
                .orElseThrow(() -> new AdsAuctionException("Campaign wallet not found: " + campaignId));
        warmupBudgetCache(campaignId, wallet.getRemainingBudget());
        return wallet.getRemainingBudget();
    }

    private String redisKey(Long campaignId) {
        return BUDGET_KEY_PREFIX + campaignId;
    }

    private Long executeDeductScript(String key, BigDecimal price, Long campaignId) {
        try {
            return redisTemplate.execute(DEDUCT_SCRIPT, List.of(key), price.toPlainString());
        } catch (RedisSystemException ex) {
            log.error("Redis wallet script execution failed for campaignId={} key={} amount={}: {}",
                    campaignId, key, price, ex.getMessage(), ex);
            throw new WalletOperationException("Wallet script execution failed for campaignId=" + campaignId, ex);
        } catch (RuntimeException ex) {
            log.error("Unexpected wallet execution failure for campaignId={} key={} amount={}: {}",
                    campaignId, key, price, ex.getMessage(), ex);
            throw new WalletOperationException("Wallet execution failed for campaignId=" + campaignId, ex);
        }
    }
}
