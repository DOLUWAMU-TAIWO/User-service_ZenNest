-- Add openVisitations column to qorelabs_users table if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='open_visitations') THEN
        ALTER TABLE qorelabs_users ADD COLUMN open_visitations BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Add totalEarnings column to qorelabs_users table if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='total_earnings') THEN
        ALTER TABLE qorelabs_users ADD COLUMN total_earnings DOUBLE PRECISION NOT NULL DEFAULT 0.0;
    END IF;
END $$;

-- Add payoutInfo fields to qorelabs_users table if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_account_number') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_account_number VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_bank_code') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_bank_code VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_bank_name') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_bank_name VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_account_holder_name') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_account_holder_name VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_recipient_code') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_recipient_code VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_bvn') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_bvn VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_email_for_payouts') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_email_for_payouts VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_verified') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_verified BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_last_updated') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_last_updated TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payoutinfo_currency') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payoutinfo_currency VARCHAR(10);
    END IF;
END $$;

-- Add paymentVerified column to qorelabs_users table if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='payment_verified') THEN
        ALTER TABLE qorelabs_users ADD COLUMN payment_verified BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;
