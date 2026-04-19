package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Common.Util.BalanceAffected;
import com.stayhub.backend.Module.Finance.Model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    Page<Transaction> findByWallet_IdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    Page<Transaction> findByWallet_IdAndBalanceAffectedOrderByCreatedAtDesc(Long walletId, BalanceAffected balanceAffected, Pageable pageable);
    Optional<Transaction> findByPayout_Id(Long payoutId);
}
