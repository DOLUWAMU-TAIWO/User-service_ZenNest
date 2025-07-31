-- Add tenant-specific preference fields to qorelabs_users table (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='search_radius') THEN
        ALTER TABLE qorelabs_users ADD COLUMN search_radius INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='price_alerts') THEN
        ALTER TABLE qorelabs_users ADD COLUMN price_alerts BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='new_listing_alerts') THEN
        ALTER TABLE qorelabs_users ADD COLUMN new_listing_alerts BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='visit_reminders') THEN
        ALTER TABLE qorelabs_users ADD COLUMN visit_reminders BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='auto_save_searches') THEN
        ALTER TABLE qorelabs_users ADD COLUMN auto_save_searches BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='qorelabs_users' AND column_name='max_budget') THEN
        ALTER TABLE qorelabs_users ADD COLUMN max_budget DOUBLE PRECISION;
    END IF;
END $$;

-- Add preferred property types and amenities as separate tables for list storage (idempotent)
CREATE TABLE IF NOT EXISTS user_preferred_property_types (
    user_id UUID NOT NULL,
    property_type VARCHAR(255),
    PRIMARY KEY (user_id, property_type),
    CONSTRAINT fk_user_property_type FOREIGN KEY (user_id) REFERENCES qorelabs_users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_preferred_amenities (
    user_id UUID NOT NULL,
    amenity VARCHAR(255),
    PRIMARY KEY (user_id, amenity),
    CONSTRAINT fk_user_amenity FOREIGN KEY (user_id) REFERENCES qorelabs_users(id) ON DELETE CASCADE
);
