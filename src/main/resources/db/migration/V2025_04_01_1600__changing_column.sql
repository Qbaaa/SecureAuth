ALTER TABLE secureauth.session
ADD COLUMN session_token_uuid UUID;

UPDATE secureauth.session
SET session_token_uuid = session_token::UUID;

ALTER TABLE secureauth.session DROP COLUMN session_token;
ALTER TABLE secureauth.session RENAME COLUMN session_token_uuid TO session_token;
ALTER TABLE secureauth.session ALTER COLUMN session_token SET NOT NULL;