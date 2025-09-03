ALTER TABLE secureauth."user"
    ADD COLUMN failed_login_attempts INT DEFAULT 0 NOT NULL;

ALTER TABLE secureauth."user"
    ADD COLUMN last_failed_login_time TIMESTAMP;