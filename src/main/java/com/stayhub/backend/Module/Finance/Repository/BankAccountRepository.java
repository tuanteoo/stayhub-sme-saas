package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Module.Finance.Model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    List<BankAccount> findByUser_Id(Long userId);
    Optional<BankAccount> findByIdAndUser_Id(Integer id, Long userId);
}
