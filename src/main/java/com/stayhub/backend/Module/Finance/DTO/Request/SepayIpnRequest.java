package com.stayhub.backend.Module.Finance.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SepayIpnRequest(

        @JsonProperty("notification_type")
        String notificationType,

        OrderData order
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderData(
            @JsonProperty("order_invoice_number")
            String orderInvoiceNumber
    ) {}
}



