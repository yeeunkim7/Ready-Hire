-- Align subscriptions / payments with PortOne V2 + PRO subscription flow

-- Subscriptions
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS chk_subscriptions_plan_type;
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS chk_subscriptions_status;

UPDATE subscriptions SET plan_type = 'PRO' WHERE plan_type IS NOT NULL;

UPDATE subscriptions SET status = 'CANCELLED' WHERE status = 'CANCELED';

ALTER TABLE subscriptions RENAME COLUMN ended_at TO expires_at;
ALTER TABLE subscriptions ALTER COLUMN expires_at DROP NOT NULL;

ALTER TABLE subscriptions DROP COLUMN IF EXISTS canceled_at;

ALTER TABLE subscriptions
    ADD CONSTRAINT chk_subscriptions_plan_type CHECK (plan_type IN ('PRO'));

ALTER TABLE subscriptions
    ADD CONSTRAINT chk_subscriptions_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED'));

-- Payments
ALTER TABLE payments DROP CONSTRAINT IF EXISTS chk_payments_status;
ALTER TABLE payments DROP CONSTRAINT IF EXISTS chk_payments_payment_type;

UPDATE payments SET status = 'PAID' WHERE status = 'SUCCESS';
UPDATE payments SET status = 'CANCELLED' WHERE status = 'CANCELED';
UPDATE payments SET status = 'FAILED' WHERE status = 'PENDING';

ALTER TABLE payments DROP COLUMN IF EXISTS payment_type;

ALTER TABLE payments RENAME COLUMN provider_payment_id TO portone_payment_id;
ALTER TABLE payments ALTER COLUMN portone_payment_id TYPE VARCHAR(255);
ALTER TABLE payments ALTER COLUMN currency TYPE VARCHAR(10);
ALTER TABLE payments ALTER COLUMN amount TYPE INTEGER USING ROUND(amount::numeric)::integer;
ALTER TABLE payments ALTER COLUMN subscription_id DROP NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status CHECK (status IN ('PAID', 'FAILED', 'CANCELLED'));

DELETE FROM payments WHERE portone_payment_id IS NULL OR portone_payment_id = '';

ALTER TABLE payments ALTER COLUMN portone_payment_id SET NOT NULL;
