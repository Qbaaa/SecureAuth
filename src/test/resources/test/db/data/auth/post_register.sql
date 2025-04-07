INSERT INTO secureauth."domain" (id, "name", is_enabled_register, is_enabled_verified_email,
                                 access_token_validity, refresh_token_validity, email_token_validity, session_validity,
                                 created_at, updated_at)
VALUES (100, 'master', false, false,
        300, 600, 180, 3600,
        '2025-03-25 22:15:00', '2025-03-25 22:15:00')
     , (101, 'test-domain',true, false,
        300, 600, 180, 3600,
        '2025-03-31 15:00:00', '2025-03-31 15:00:00')
;


INSERT INTO secureauth."role" (id, domain_id, "name", description, is_default, created_at, updated_at)
VALUES (201, 100, 'ADMIN','Role Test ADMIN', false,
        '2025-03-25 22:16:00', '2025-03-25 22:16:00')
     , (202, 101, 'ADMIN','Role Test ADMIN',  false,
        '2025-03-31 16:17:00', '2025-03-31 16:17:00')
     , (203, 101, 'ROLE_001','Role Test 001',  true,
        '2025-03-31 16:18:00', '2025-03-31 16:18:00')
     , (204, 101, 'ROLE_002','Role Test 002',  true,
        '2025-03-31 16:19:00', '2025-03-31 16:19:00')
;

INSERT INTO secureauth."key" (id, domain_id, algorithm, public_key, private_key, created_at, updated_at)
VALUES(100, 100, 'RSA',
       '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',
       '2025-03-25 22:17:00', '2025-03-25 22:17:00')
     , (101, 101, 'RSA',
        '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',
        '2025-03-31 16:00:00', '2025-03-31 16:00:00')
;

INSERT INTO secureauth."user" (id, domain_id, username, email, is_active, created_at, updated_at)
VALUES (401, 101, 'user001', 'user001@test.com', true,
        '2025-03-25 22:30:00', '2025-03-25 22:30:00')
;

INSERT INTO secureauth."password" (id, user_id, "password", created_at, updated_at)
VALUES  (501, 401, '$2a$10$DmZD6S035DEW3nsbJYb3zOdnHWohNzL8bq60auttuwQu/X36pdLky', -- secretUser002
        '2025-03-31 16:35:00', '2025-03-31 22:35:00')
;