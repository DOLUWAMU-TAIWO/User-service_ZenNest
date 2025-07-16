-- Add new user settings fields to qorelabs_users
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='auto_accept_booking'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN auto_accept_booking BOOLEAN DEFAULT FALSE NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='auto_accept_visitation'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN auto_accept_visitation BOOLEAN DEFAULT FALSE NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='email_notifications_enabled'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN email_notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='sms_notifications_enabled'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN sms_notifications_enabled BOOLEAN DEFAULT FALSE NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='push_notifications_enabled'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN push_notifications_enabled BOOLEAN DEFAULT FALSE NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='buffer_time_hours'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN buffer_time_hours INT DEFAULT 0 NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='visit_duration'
    ) THEN
        ALTER TABLE qorelabs_users ADD COLUMN visit_duration VARCHAR(32) DEFAULT 'THIRTY_MINUTES' NOT NULL;
    END IF;
END $$;
