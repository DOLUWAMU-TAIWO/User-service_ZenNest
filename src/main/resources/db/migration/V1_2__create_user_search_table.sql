-- Create user_search table for storing user search history
CREATE TABLE user_search (
    id BIGSERIAL PRIMARY KEY,
    location VARCHAR(255),
    address VARCHAR(255),
    property_type VARCHAR(100),
    transaction_type VARCHAR(20),
    min_price DOUBLE PRECISION,
    max_price DOUBLE PRECISION,
    min_bedrooms INTEGER,
    max_bedrooms INTEGER,
    min_bathrooms INTEGER,
    max_bathrooms INTEGER,
    amenities TEXT,
    keywords TEXT,
    searched_at TIMESTAMP,
    user_id UUID,
    CONSTRAINT fk_user_search_user FOREIGN KEY (user_id) REFERENCES qorelabs_users(id)
);
