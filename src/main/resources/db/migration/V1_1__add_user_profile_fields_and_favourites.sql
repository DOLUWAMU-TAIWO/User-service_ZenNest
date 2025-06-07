-- Add intention, profileDescription, and profilePicture columns to qorelabs_users
ALTER TABLE qorelabs_users
    ADD COLUMN intention VARCHAR(20),
    ADD COLUMN profile_description TEXT,
    ADD COLUMN profile_picture VARCHAR(255);

-- Create user_favourites table for storing user favourite listing references
CREATE TABLE user_favourites (
    user_id UUID NOT NULL,
    listing_id UUID NOT NULL,
    PRIMARY KEY (user_id, listing_id),
    CONSTRAINT fk_user_favourites_user FOREIGN KEY (user_id) REFERENCES qorelabs_users(id)
);

