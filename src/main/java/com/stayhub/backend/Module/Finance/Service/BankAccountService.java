package com.stayhub.backend.Module.Finance.Service;

import com.stayhub.backend.Module.Finance.DTO.Request.BankAccountRequest;
import com.stayhub.backend.Module.Finance.DTO.Response.BankAccountResponse;

import java.util.List;

public interface BankAccountService {
    void addBankAccount(Long userId, BankAccountRequest request);
    List<BankAccountResponse> getMyBankAccounts(Long userId);
    void deleteBankAccount(Long userId, Integer bankAccountId);
}
