CREATE TABLE secureauth.domain
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL UNIQUE,
    access_token_validity  INT          NOT NULL,
    refresh_token_validity INT          NOT NULL,
    email_token_validity   INT          NOT NULL,
    session_validity       INT          NOT NULL,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE secureauth.key
(
    id          SERIAL PRIMARY KEY,
    domain_id   INT         NOT NULL,
    algorithm   VARCHAR(50) NOT NULL, -- Np. "RS256"
    public_key  TEXT        NOT NULL, -- Klucz publiczny (base64)
    private_key TEXT        NOT NULL, -- Klucz prywatny (base64)
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (domain_id) REFERENCES secureauth.domain (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.user
(
    id          BIGSERIAL PRIMARY KEY,
    domain_id   BIGINT       NOT NULL,
    username    VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    is_active   BOOLEAN   DEFAULT TRUE,
    is_verified BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (domain_id) REFERENCES secureauth.domain (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.password
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    password   TEXT   NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.role
(
    id          BIGSERIAL PRIMARY KEY,
    domain_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (domain_id) REFERENCES secureauth.domain (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.user_role
(
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES secureauth.role (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.refresh_token
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    token      TEXT      NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.email_verification_token
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    token      TEXT      NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE
);

CREATE TABLE secureauth.session
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    NOT NULL,
    session_token TEXT      NOT NULL,
    ip_address    VARCHAR(45),
    user_agent    TEXT,
    expires_at    TIMESTAMP NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES secureauth.user (id) ON DELETE CASCADE
);

