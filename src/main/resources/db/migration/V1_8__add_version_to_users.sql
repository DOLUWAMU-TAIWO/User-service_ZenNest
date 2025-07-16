-- Add version column to qorelabs_users if it does not exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='version'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN version BIGINT;
    END IF;
END $$;

