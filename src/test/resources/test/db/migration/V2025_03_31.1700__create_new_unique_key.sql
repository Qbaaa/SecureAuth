ALTER TABLE secureauth."user"
DROP CONSTRAINT user_email_key;

ALTER TABLE secureauth."user"
DROP CONSTRAINT user_username_key;

ALTER TABLE secureauth."user"
ADD CONSTRAINT unique_domain_username_key UNIQUE (domain_id, username);

ALTER TABLE secureauth."user"
ADD CONSTRAINT unique_domain_email_key UNIQUE (domain_id, email);

ALTER TABLE secureauth."role"
DROP CONSTRAINT role_name_key;

ALTER TABLE secureauth."role"
ADD CONSTRAINT unique_domain_name_key UNIQUE (domain_id, name);