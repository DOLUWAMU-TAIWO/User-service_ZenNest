-- Add business_type column to qorelabs_users if it does not exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='business_type'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN business_type VARCHAR(32);
    END IF;
END $$;

