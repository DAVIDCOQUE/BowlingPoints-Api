-- Insertar persona admin
INSERT INTO person (document, full_name, full_surname, email, status, created_at, updated_at)
VALUES ('102938', 'Admin', 'Sistema', 'admin@bowlingpoints.com', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar usuario admin con la persona creada
INSERT INTO users (person_id, nickname, password, status, created_at, updated_at)
VALUES (
    (SELECT person_id FROM person WHERE document = '102938'),
    'admin',
    '$2a$10$EIX2gTVCFmYlQ2SnBpCXBeYvXAOBpXuLtkE5S2HWpCr8Xcqfzqaay',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Asignar rol ADMIN al usuario
INSERT INTO user_role (user_id, role_id, status, created_at, updated_at)
VALUES (
    (SELECT user_id FROM users WHERE nickname = 'admin'),
    (SELECT role_id FROM roles WHERE name = 'ADMIN'),
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
