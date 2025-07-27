ALTER TABLE qorelabs_users
ADD COLUMN IF NOT EXISTS fcm_device_token VARCHAR(255);

