package com.stayhub.backend.Module.Finance.Model;

public record PayoutValidationResult(
        Wallet wallet,
        BankAccount bankAccount
) {
}
