CREATE TABLE manga_series (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    publisher_it VARCHAR(100),
    status_it VARCHAR(50),
    latest_volume_it INT DEFAULT 0,
    status_jp VARCHAR(50),
    latest_volume_jp INT DEFAULT 0,
    animeclick_url TEXT,
    anilist_id INT,
    last_scraped_at TIMESTAMP,
    cover_url TEXT,
    animeclick_titolo_ricerca VARCHAR(255)   -- nome da verificare, vedi sotto
);

CREATE TABLE user_volume (
    id BIGSERIAL PRIMARY KEY,
    series_id BIGINT REFERENCES manga_series(id) ON DELETE CASCADE,
    volume_number INT NOT NULL,
    price_paid DECIMAL(5,2),
    acquired_at DATE,
    cover_price DECIMAL(5,2),
    notes TEXT,
    edizione VARCHAR(100) NOT NULL DEFAULT 'Normale',
    CONSTRAINT unique_series_volume_edizione UNIQUE (series_id, volume_number, edizione)
);