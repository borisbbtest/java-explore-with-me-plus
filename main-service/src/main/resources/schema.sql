-- Таблица категорий
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX idx_categories_name ON categories(name);

-- Таблица пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_name ON users(name);

-- Таблица локаций
CREATE TABLE locations (
    id SERIAL PRIMARY KEY,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    UNIQUE (lat, lon)
);
CREATE INDEX idx_locations_lat_lon ON locations(lat, lon);

-- Таблица состояний событий
CREATE TABLE event_states (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
CREATE INDEX idx_event_states_name ON event_states(name);

-- Таблица событий
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    annotation TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    paid BOOLEAN NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES users(id),
    description TEXT NOT NULL,
    participant_limit INT NOT NULL DEFAULT 0,
    state_id INT NOT NULL REFERENCES event_states(id),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    location_id INT NOT NULL REFERENCES locations(id),
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE
);

-- Индексы для ускорения поиска событий
CREATE INDEX idx_events_category_id ON events(category_id);
CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_events_initiator_id ON events(initiator_id);
CREATE INDEX idx_events_state_id ON events(state_id);
CREATE INDEX idx_events_location_id ON events(location_id);
CREATE INDEX idx_events_paid ON events(paid);

-- Таблица подборок
CREATE TABLE compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_compilations_pinned ON compilations(pinned);

-- Таблица связи подборки и события
CREATE TABLE compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);
CREATE INDEX idx_compilation_events_compilation_id ON compilation_events(compilation_id);
CREATE INDEX idx_compilation_events_event_id ON compilation_events(event_id);
