-- Flyway migration to add user status and subscription fields

ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS profile_completed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50);

ALTER TABLE qorelabs_users
    ADD COLUMN IF NOT EXISTS subscription_active BOOLEAN NOT NULL DEFAULT FALSE;
