INSERT INTO secureauth."domain" (id, "name", is_enabled_register, is_enabled_verified_email,
                                 access_token_validity, refresh_token_validity, email_token_validity, session_validity,
                                 created_at, updated_at)
VALUES (100, 'master', false, false,
        300, 600, 180, 3600,
        '2025-03-25 22:15:00', '2025-03-25 22:15:00')
     , (101, 'test-domain', true, true,
        300, 600, 180, 3600,
        '2025-04-15 16:00:00', '2025-04-15 16:00:00')
;

INSERT INTO secureauth."user" (id, domain_id, username, email, is_active, is_multifactor_auth_enabled, created_at, updated_at)
VALUES (101, 100, 'masterUser001', 'user001@test.com', true, false,
        '2025-03-25 22:30:00', '2025-03-25 22:30:00')
     , (102, 101, 'testDomainUser002', 'user002@test.com', true, false,
        '2025-03-31 15:00:00', '2025-03-31 15:00:00')
;

INSERT INTO secureauth."session" (id, user_id, expires_at, created_at, updated_at, session_token)
VALUES (301, 101, '2025-04-01 17:00:00',
       '2025-04-01 16:00:00', '2025-04-01 16:00:00', '27275de1-98f2-4dfe-9044-8ee5aa92cc91')
     , (302, 101, '2025-04-02 18:00:00',
       '2025-04-02 17:00:00', '2025-04-02 16:00:00', '27275de1-98f2-4dfe-9044-8ee5aa92cc92')
     , (303, 102, '2025-04-03 07:00:00',
       '2025-04-03 06:00:00', '2025-04-03 06:00:00', '27275de1-98f2-4dfe-9044-8ee5aa92cc93')
     , (304, 102, '2025-04-17 10:00:00',
       '2025-04-17 09:00:00', '2025-04-17 09:00:00', '27275de1-98f2-4dfe-9044-8ee5aa92cc94')
;

INSERT INTO secureauth.email_verification_token (id, user_id, "token", expires_at, created_at)
VALUES (401, 101, 'TOKEN_001', '2025-04-05 15:00:00', '2025-04-16 14:00:00')
     , (402, 101, 'TOKEN_002', '2025-04-07 17:00:00', '2025-04-16 16:00:00')
     , (403, 101, 'TOKEN_003', '2025-04-21 21:00:00', '2025-04-21 20:00:00')

     , (404, 102, 'TOKEN_004', '2025-04-19 22:00:00', '2025-04-21 21:00:00')
     , (405, 102, 'TOKEN_005', '2025-04-02 07:00:00', '2025-04-02 06:00:00')
;

INSERT INTO secureauth.refresh_token (id, user_id, "token", expires_at, created_at)
VALUES (501, 101, 'TOKEN_001', '2025-04-05 15:00:00', '2025-04-16 14:00:00')
     , (502, 101, 'TOKEN_002', '2025-04-07 17:00:00', '2025-04-16 16:00:00')
     , (503, 101, 'TOKEN_003', '2025-04-21 21:00:00', '2025-04-21 20:00:00')

     , (504, 102, 'TOKEN_004', '2025-04-19 22:00:00', '2025-04-21 21:00:00')
     , (505, 102, 'TOKEN_005', '2025-04-02 07:00:00', '2025-04-02 06:00:00')
;