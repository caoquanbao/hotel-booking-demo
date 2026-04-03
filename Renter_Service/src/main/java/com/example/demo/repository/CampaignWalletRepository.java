package com.example.demo.repository;

import com.example.demo.entity.CampaignWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CampaignWalletRepository extends JpaRepository<CampaignWallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from CampaignWallet w where w.campaignId = :campaignId")
    Optional<CampaignWallet> findByCampaignIdForUpdate(@Param("campaignId") Long campaignId);
}
