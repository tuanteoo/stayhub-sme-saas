package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Module.Finance.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
}
