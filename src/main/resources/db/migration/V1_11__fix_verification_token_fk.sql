-- Fix foreign key constraint for verification_token to reference qorelabs_users
-- Drop the old constraint if it exists
ALTER TABLE verification_token
DROP CONSTRAINT IF EXISTS fk3asw9wnv76uxu3kr1ekq4i1ld;

-- Add the correct foreign key constraint
ALTER TABLE verification_token
ADD CONSTRAINT fk_verification_token_user
FOREIGN KEY (user_id) REFERENCES qorelabs_users(id);

