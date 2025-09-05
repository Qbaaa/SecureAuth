ALTER TABLE secureauth."user"
    ADD COLUMN is_multifactor_auth_enabled BOOLEAN NOT NULL;

CREATE TYPE secureauth.multifactor_auth_type AS ENUM ('EMAIL', 'SMS');

ALTER TABLE secureauth."user"
    ADD COLUMN multifactor_auth_type secureauth.multifactor_auth_type;

CREATE TYPE secureauth.operation_type AS ENUM ('MFA', 'CHANGE_PASSWORD', 'RESET_PASSWORD');

CREATE TABLE secureauth.otp
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    secret         VARCHAR(255) NOT NULL,
    operation_type secureauth.operation_type,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE
);