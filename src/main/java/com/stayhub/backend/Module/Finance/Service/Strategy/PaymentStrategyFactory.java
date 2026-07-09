package com.stayhub.backend.Module.Finance.Service.Strategy;

import com.stayhub.backend.Common.Util.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        strategies = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.getPaymentMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Không hỗ trợ phương thức thanh toán: " + method);
        }
        return strategy;
    }
}
