-- Create table for onboarding features completed by users
CREATE TABLE IF NOT EXISTS user_onboarding_features (
    user_id UUID NOT NULL,
    feature VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, feature),
    CONSTRAINT fk_user_onboarding_user FOREIGN KEY (user_id) REFERENCES qorelabs_users(id) ON DELETE CASCADE
);

