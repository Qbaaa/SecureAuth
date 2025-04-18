CREATE TABLE IF NOT EXISTS secureauth.shedlock
(
    name       VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255) NULL,
    PRIMARY KEY (name)
);