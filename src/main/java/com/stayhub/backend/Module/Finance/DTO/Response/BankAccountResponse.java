package com.stayhub.backend.Module.Finance.DTO.Response;

import lombok.Builder;

@Builder
public record BankAccountResponse(
        Integer id,
        String bankName,
        String accountNumber,
        String accountHolderName,
        String branchBank,
        Boolean isDefault
) {
}
