-- ===========================================
-- BOWLING POINTS - CREACION DE TABLAS
-- Version: 1.0
-- Compatible con entidades JPA
-- ===========================================

-- Tabla: roles
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_by INT,
    updated_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: person
CREATE TABLE person (
    person_id SERIAL PRIMARY KEY,
    document VARCHAR(50) UNIQUE NOT NULL,
    photo_url TEXT,
    full_name VARCHAR(100) NOT NULL,
    full_surname VARCHAR(100) NOT NULL,
    gender VARCHAR(20),
    birth_date DATE,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(30),
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    person_id INT NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    last_login_at TIMESTAMP,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    attempts_login INT DEFAULT 0,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_person FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Tabla: user_role
CREATE TABLE user_role (
    user_role_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Tabla: clubs
CREATE TABLE clubs (
    club_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    foundation_date DATE,
    city VARCHAR(100),
    description TEXT,
    "imageUrl" VARCHAR(255),
    status BOOLEAN DEFAULT TRUE,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: club_person
CREATE TABLE club_person (
    club_person_id SERIAL PRIMARY KEY,
    club_id INT NOT NULL,
    person_id INT,
    role_in_club VARCHAR(50),
    joined_at TIMESTAMP,
    status BOOLEAN DEFAULT TRUE,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_club_person_club FOREIGN KEY (club_id) REFERENCES clubs(club_id),
    CONSTRAINT fk_club_person_person FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Tabla: category
CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: modality
CREATE TABLE modality (
    modality_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: ambit
CREATE TABLE ambit (
    ambit_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    description TEXT,
    image_url VARCHAR(255),
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: branch (ramas: Masculino, Femenino, Mixto)
CREATE TABLE branch (
    branch_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: person_category
CREATE TABLE person_category (
    person_category_id SERIAL PRIMARY KEY,
    person_id INT NOT NULL,
    category_id INT NOT NULL,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_person_category_person FOREIGN KEY (person_id) REFERENCES person(person_id),
    CONSTRAINT fk_person_category_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- Tabla: team
CREATE TABLE team (
    team_id SERIAL PRIMARY KEY,
    name_team VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    status BOOLEAN DEFAULT TRUE,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: team_person
CREATE TABLE team_person (
    team_person_id SERIAL PRIMARY KEY,
    person_id INT,
    team_id INT,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_person_person FOREIGN KEY (person_id) REFERENCES person(person_id),
    CONSTRAINT fk_team_person_team FOREIGN KEY (team_id) REFERENCES team(team_id)
);

-- Tabla: tournament
CREATE TABLE tournament (
    tournament_id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    ambit_id INT,
    organizer VARCHAR(200),
    "imageUrl" VARCHAR(255),
    start_date DATE,
    end_date DATE,
    location VARCHAR(200),
    stage VARCHAR(50),
    status BOOLEAN DEFAULT TRUE,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_ambit FOREIGN KEY (ambit_id) REFERENCES ambit(ambit_id)
);

-- Tabla: tournament_modality
CREATE TABLE tournament_modality (
    tournament_modality_id SERIAL PRIMARY KEY,
    tournament_id INT NOT NULL,
    modality_id INT NOT NULL,
    CONSTRAINT fk_tournament_modality_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_tournament_modality_modality FOREIGN KEY (modality_id) REFERENCES modality(modality_id)
);

-- Tabla: tournament_category
CREATE TABLE tournament_category (
    tournament_category_id SERIAL PRIMARY KEY,
    tournament_id INT NOT NULL,
    category_id INT NOT NULL,
    CONSTRAINT fk_tournament_category_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_tournament_category_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- Tabla: tournament_branch
CREATE TABLE tournament_branch (
    tournament_branch_id SERIAL PRIMARY KEY,
    tournament_id INT NOT NULL,
    branch_id INT NOT NULL,
    CONSTRAINT fk_tournament_branch_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_tournament_branch_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

-- Tabla: tournament_team
CREATE TABLE tournament_team (
    tournament_team_id SERIAL PRIMARY KEY,
    tournament_id INT NOT NULL,
    team_id INT NOT NULL,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_team_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_tournament_team_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    CONSTRAINT uk_tournament_team UNIQUE (tournament_id, team_id)
);

-- Tabla: tournament_registration
CREATE TABLE tournament_registration (
    registration_id SERIAL PRIMARY KEY,
    person_id INT,
    tournament_id INT NOT NULL,
    category_id INT,
    modality_id INT,
    branch_id INT,
    team_id INT,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    registration_date TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registration_person FOREIGN KEY (person_id) REFERENCES person(person_id),
    CONSTRAINT fk_registration_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_registration_category FOREIGN KEY (category_id) REFERENCES category(category_id),
    CONSTRAINT fk_registration_modality FOREIGN KEY (modality_id) REFERENCES modality(modality_id),
    CONSTRAINT fk_registration_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    CONSTRAINT fk_registration_team FOREIGN KEY (team_id) REFERENCES team(team_id)
);

-- Tabla: result
CREATE TABLE result (
    result_id SERIAL PRIMARY KEY,
    person_id INT,
    team_id INT,
    tournament_id INT,
    round_number INT,
    category_id INT,
    modality_id INT,
    branch_id INT,
    lane_number INT,
    line_number INT,
    score INT NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_person FOREIGN KEY (person_id) REFERENCES person(person_id),
    CONSTRAINT fk_result_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    CONSTRAINT fk_result_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(tournament_id),
    CONSTRAINT fk_result_category FOREIGN KEY (category_id) REFERENCES category(category_id),
    CONSTRAINT fk_result_modality FOREIGN KEY (modality_id) REFERENCES modality(modality_id),
    CONSTRAINT fk_result_branch FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

-- Tabla: password_reset_tokens
CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiration_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Tabla: bowling_center
CREATE TABLE bowling_center (
    bowling_center_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    open_days VARCHAR(100),
    open_hours VARCHAR(100),
    social_links TEXT,
    status BOOLEAN DEFAULT TRUE NOT NULL,
    created_by INT,
    updated_by INT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices para mejorar rendimiento
CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_document ON person(document);
CREATE INDEX idx_users_nickname ON users(nickname);
CREATE INDEX idx_result_tournament ON result(tournament_id);
CREATE INDEX idx_result_person ON result(person_id);
CREATE INDEX idx_registration_tournament ON tournament_registration(tournament_id);
CREATE INDEX idx_registration_person ON tournament_registration(person_id);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
