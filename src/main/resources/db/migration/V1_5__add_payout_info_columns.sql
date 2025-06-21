-- Add payout info columns to qorelabs_users
ALTER TABLE qorelabs_users
    ADD COLUMN account_holder_name VARCHAR(255),
    ADD COLUMN account_number VARCHAR(255),
    ADD COLUMN bank_code VARCHAR(50),
    ADD COLUMN bank_name VARCHAR(255),
    ADD COLUMN bvn VARCHAR(50),
    ADD COLUMN currency VARCHAR(10),
    ADD COLUMN email_for_payouts VARCHAR(255),
    ADD COLUMN last_updated TIMESTAMP,
    ADD COLUMN recipient_code VARCHAR(255),
    ADD COLUMN payout_verified BOOLEAN;

