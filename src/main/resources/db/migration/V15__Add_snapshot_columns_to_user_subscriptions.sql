ALTER TABLE user_subscriptions
ADD COLUMN current_max_listings INT,
ADD COLUMN current_credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0;

UPDATE user_subscriptions us
SET current_max_listings = sp.max_listings,
    current_credit_limit = sp.credit_limit
FROM subscription_plans sp
WHERE us.plan_id = sp.id;