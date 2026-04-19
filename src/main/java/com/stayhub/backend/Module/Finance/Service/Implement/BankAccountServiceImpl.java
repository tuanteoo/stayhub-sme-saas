package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Module.Finance.DTO.Request.BankAccountRequest;
import com.stayhub.backend.Module.Finance.DTO.Response.BankAccountResponse;
import com.stayhub.backend.Module.Finance.Model.BankAccount;
import com.stayhub.backend.Module.Finance.Repository.BankAccountRepository;
import com.stayhub.backend.Module.Finance.Service.BankAccountService;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBankAccount(Long userId, BankAccountRequest request) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (Boolean.TRUE.equals(request.isDefault())) {
            List<BankAccount> existing = bankAccountRepository.findByUser_Id(userId);
            existing.forEach(b -> b.setIsDefault(false));
            bankAccountRepository.saveAll(existing);
        }

        BankAccount bankAccount = BankAccount.builder()
                .user(user)
                .bankName(request.bankName())
                .accountNumber(request.accountNumber())
                .accountHolderName(request.accountHolderName())
                .branch(request.bankName())
                .isDefault(request.isDefault() != null ? request.isDefault() : false)
                .isVerified(true)
                .build();

        bankAccountRepository.save(bankAccount);
    }

    @Override
    public List<BankAccountResponse> getMyBankAccounts(Long userId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByUser_Id(userId);

        return bankAccounts.stream()
                .map(bank -> BankAccountResponse.builder()
                        .id(bank.getId())
                        .bankName(bank.getBankName())
                        .accountNumber(bank.getAccountNumber())
                        .accountHolderName(bank.getAccountHolderName())
                        .branchBank(bank.getBranch())
                        .isDefault(bank.getIsDefault())
                        .build())
                .toList();
    }

    @Override
    public void deleteBankAccount(Long userId, Integer bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findByIdAndUser_Id(bankAccountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng hoặc bạn không có quyền xóa."));

        bankAccountRepository.delete(bankAccount);

        if (Boolean.TRUE.equals(bankAccount.getIsDefault())) {
            List<BankAccount> remainingAccounts = bankAccountRepository.findByUser_Id(userId);
            if (!remainingAccounts.isEmpty()) {
                BankAccount newDefault = remainingAccounts.get(0);
                newDefault.setIsDefault(true);
                bankAccountRepository.save(newDefault);
            }
        }
    }
}
