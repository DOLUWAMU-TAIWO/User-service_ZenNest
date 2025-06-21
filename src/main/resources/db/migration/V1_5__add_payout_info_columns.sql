-- Add payout info columns to qorelabs_users
ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS account_holder_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS account_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bank_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS bank_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bvn VARCHAR(50),
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS email_for_payouts VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_updated TIMESTAMP,
    ADD COLUMN IF NOT EXISTS recipient_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payout_verified BOOLEAN;
