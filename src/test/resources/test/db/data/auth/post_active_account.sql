INSERT INTO secureauth."domain" (id, "name", is_enabled_register, is_enabled_verified_email,
                                 access_token_validity, refresh_token_validity, email_token_validity, session_validity,
                                 created_at, updated_at)
VALUES (100, 'test-domain-001', true, true,
        300, 600, 180, 3600,
        '2025-04-15 16:00:00', '2025-04-15 16:00:00')
;


INSERT INTO secureauth."role" (id, domain_id, "name", description, is_default, created_at, updated_at)
VALUES (201, 100, 'ADMIN','Role Test 001',  false,
        '2025-04-15 16:05:00', '2025-04-15 16:05:00')
     , (202, 100, 'USER','Role Test 002',  true,
        '2025-04-15 16:06:00', '2025-04-15 16:06:00')
;

INSERT INTO secureauth."key" (id, domain_id, algorithm, public_key, private_key, created_at, updated_at)
VALUES (102, 100, 'RSA',
        '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',
        '2025-04-15 16:01:00', '2025-04-15 16:01:00');
;

INSERT INTO secureauth."user" (id, domain_id, username, email, is_active, created_at, updated_at)
VALUES (401, 100, 'user001', 'user001@test.com', false,
        '2025-03-25 22:30:00', '2025-03-25 22:30:00')
;

INSERT INTO secureauth."password" (id, user_id, "password", created_at, updated_at)
VALUES  (501, 401, '$2a$10$Di3k1j1pr9LaAPvQi2y5XekxS0SB1ZxSp.rMSP129deGSqDQamf12', -- secretUser001
         '2025-03-31 16:35:00', '2025-03-31 22:35:00')
;