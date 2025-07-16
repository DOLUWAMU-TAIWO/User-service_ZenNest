 -- Set all existing null version values to 0
UPDATE qorelabs_users SET version = 0 WHERE version IS NULL;

-- Alter the version column to be NOT NULL and set default to 0
ALTER TABLE qorelabs_users ALTER COLUMN version SET NOT NULL;
ALTER TABLE qorelabs_users ALTER COLUMN version SET DEFAULT 0;
