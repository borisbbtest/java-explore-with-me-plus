-- Таблица категорий
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

-- Таблица локаций
CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    UNIQUE (lat, lon)
);
CREATE INDEX IF NOT EXISTS idx_locations_lat_lon ON locations(lat, lon);

-- Таблица состояний событий
--CREATE TABLE IF NOT EXISTS event_states (
--    id SERIAL PRIMARY KEY,
--    name VARCHAR(50) UNIQUE NOT NULL
--);
--CREATE INDEX IF NOT EXISTS idx_event_states_name ON event_states(name);

-- Таблица событий
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    annotation TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    paid BOOLEAN NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES users(id),
    description TEXT NOT NULL,
    participant_limit INT NOT NULL DEFAULT 0,
    state VARCHAR(255),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    location_id INT NOT NULL REFERENCES locations(id),
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    confirmed_requests INTEGER NOT NULL
);

-- Индексы для ускорения поиска событий
CREATE INDEX IF NOT EXISTS idx_events_category_id ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_initiator_id ON events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_state ON events(state);
CREATE INDEX IF NOT EXISTS idx_events_location_id ON events(location_id);
CREATE INDEX IF NOT EXISTS idx_events_paid ON events(paid);

-- Таблица подборок
CREATE TABLE IF NOT EXISTS compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_compilations_pinned ON compilations(pinned);

-- Таблица связи подборки и события
CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);
CREATE INDEX IF NOT EXISTS idx_compilation_events_compilation_id ON compilation_events(compilation_id);
CREATE INDEX IF NOT EXISTS idx_compilation_events_event_id ON compilation_events(event_id);


CREATE TABLE IF NOT EXISTS request_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id),
    status_id INT NOT NULL REFERENCES request_statuses(id),
    created TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_request UNIQUE (requester_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON participation_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_event_id ON participation_requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_status_id ON participation_requests(status_id);
CREATE INDEX IF NOT EXISTS idx_requests_created ON participation_requests(created);

INSERT INTO request_statuses (name) VALUES
('PENDING'),
('CONFIRMED'),
('REJECTED'),
('CANCELED')ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL,
    event_id BIGINT NOT NULL REFERENCES events (id),
    author_id BIGINT NOT NULL REFERENCES users (id),
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_comments_text ON comments(text);
CREATE INDEX IF NOT EXISTS idx_comments_event_id ON comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_created ON comments(created);
