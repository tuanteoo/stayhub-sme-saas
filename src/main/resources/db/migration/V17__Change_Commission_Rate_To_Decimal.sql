
ALTER TABLE subscription_plans
ALTER COLUMN commission_rate TYPE DECIMAL(5,2) USING commission_rate::numeric;

ALTER TABLE user_subscriptions
ALTER COLUMN current_commission_rate TYPE DECIMAL(5,2) USING current_commission_rate::numeric;

UPDATE subscription_plans SET commission_rate = commission_rate * 100 WHERE commission_rate < 1;
UPDATE user_subscriptions SET current_commission_rate = current_commission_rate * 100 WHERE current_commission_rate < 1;