CREATE TABLE IF NOT EXISTS users (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
email VARCHAR(100) NOT NULL,
login VARCHAR(100) NOT NULL,
name VARCHAR(100),
birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships (
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
friend_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
status VARCHAR(20) NOT NULL,

UNIQUE (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS films (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name VARCHAR(100) NOT NULL,
description VARCHAR(255) NOT NULL,
release_date DATE NOT NULL,
duration INT NOT NULL,
mpa_id INT REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS likes (
film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

UNIQUE(film_id, user_id)
);

CREATE TABLE IF NOT EXISTS films_genres (
film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,

UNIQUE(film_id, genre_id)
);