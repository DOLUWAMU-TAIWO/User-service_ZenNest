-- Add intention, profileDescription, and profilePicture if not exist
ALTER TABLE qorelabs_users ADD COLUMN IF NOT EXISTS intention VARCHAR(20);
ALTER TABLE qorelabs_users ADD COLUMN IF NOT EXISTS profile_description TEXT;
ALTER TABLE qorelabs_users ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(255);

-- Create user_favourites table only if missing
CREATE TABLE IF NOT EXISTS user_favourites (
    user_id UUID NOT NULL,
    listing_id UUID NOT NULL,
    PRIMARY KEY (user_id, listing_id),
    CONSTRAINT fk_user_favourites_user FOREIGN KEY (user_id) REFERENCES qorelabs_users(id)
);
