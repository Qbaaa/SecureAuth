INSERT INTO secureauth."domain" (id, "name", access_token_validity, refresh_token_validity,
                                 email_token_validity, session_validity, created_at, updated_at)
VALUES (100, 'test-domain', 300, 600, 180, 3600,
        '2025-03-25 22:15:00', '2025-03-25 22:15:00')
;

INSERT INTO secureauth."user" (id, domain_id, username, email, is_active, created_at, updated_at)
VALUES (101, 100, 'user001', 'user001@test.com', true,
        '2025-03-25 22:30:00', '2025-03-25 22:30:00')
     , (102, 100, 'user002', 'user002@test.com', true,
        '2025-03-25 22:35:00', '2025-03-25 22:35:00');
;

INSERT INTO secureauth."password" (id, user_id, "password", created_at, updated_at)
VALUES (1, 101, '$2a$10$Di3k1j1pr9LaAPvQi2y5XekxS0SB1ZxSp.rMSP129deGSqDQamf12', -- secretUser001
        '2025-03-25 22:30:00', '2025-03-25 22:30:00')
     , (2, 102, '$2a$10$DmZD6S035DEW3nsbJYb3zOdnHWohNzL8bq60auttuwQu/X36pdLky', -- secretUser002
        '2025-03-25 22:35:00', '2025-03-25 22:35:00')
;

INSERT INTO secureauth."role" (id, domain_id, "name", description, created_at, updated_at)
VALUES (101, 100, 'TEST001', 'Role Test 001',
        '2025-03-25 22:16:00', '2025-03-25 22:16:00')
     , (102, 100, 'TEST002', 'Role Test 002',
        '2025-03-25 22:17:00', '2025-03-25 22:17:00')
;

INSERT INTO secureauth."user_role" (user_id, role_id, assigned_at)
VALUES (101, 101,'2025-03-25 22:32:00')
     , (101, 102,'2025-03-25 22:33:00')

     , (102, 102,'2025-03-25 22:36:00')
;

INSERT INTO secureauth."key" (id, domain_id, algorithm, public_key, private_key, created_at, updated_at)
VALUES(100, 100, 'RSA',
       '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',
       '2025-03-25 22:17:00', '2025-03-25 22:17:00');