ALTER TABLE secureauth."domain"
ADD COLUMN is_enabled_register BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE secureauth."domain"
ADD COLUMN is_enabled_verified_email BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE secureauth."role"
ADD COLUMN is_default BOOLEAN DEFAULT FALSE NOT NULL;