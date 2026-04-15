package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Module.Finance.Model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {
        Optional<Wallet> findByUser_Id(Long userId);
        boolean existsByUser_Id(Long userId);
}
