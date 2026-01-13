-- ===========================================
-- BOWLING POINTS - DATOS INICIALES (SEED)
-- Version: 2.0
-- Datos base para catalogos del sistema
-- ===========================================

-- Insertar Roles base
INSERT INTO roles (name, created_at, updated_at) VALUES
    ('ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ENTRENADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JUGADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insertar Categorias base
INSERT INTO category (name, description, status, created_at, updated_at) VALUES
    ('Primera', 'Categoria Primera', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Segunda', 'Categoria Segunda', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Tercera', 'Categoria Tercera', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Juvenil', 'Categoria Juvenil', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Infantil', 'Categoria Infantil', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insertar Ramas (Branch)
INSERT INTO branch (name, description, status, created_at, updated_at) VALUES
    ('Masculino', 'Rama Masculina', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Femenino', 'Rama Femenina', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Mixto', 'Rama Mixta', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar Modalidades base
INSERT INTO modality (name, description, status, created_at, updated_at) VALUES
    ('Sencillos Masculino', 'Individual Masculino', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sencillos Femenino', 'Individual Femenino', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Equipos Masculino', 'Por equipos masculinos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Equipos Femenino', 'Por equipos femeninos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Dobles Masculino', 'Dobles Masculinos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Dobles Femenino', 'Dobles Femeninos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Dobles Mixto', 'Dobles Mixtos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('All Events', 'Todos los eventos combinados', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar Ambitos base
INSERT INTO ambit (name, description, status, created_at, updated_at) VALUES
    ('Nacional', 'Ambito nacional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Regional', 'Ambito regional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Departamental', 'Ambito departamental', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Municipal', 'Ambito municipal', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Internacional', 'Ambito internacional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
