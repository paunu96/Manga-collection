-- 1. Tabella Anagrafica della Serie (Opera)
CREATE TABLE manga_series (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),               -- es. "Eiichiro Oda"
    publisher_it VARCHAR(100),         -- es. "Star Comics", "J-Pop"
    status_it VARCHAR(50),             -- es. "In corso", "Concluso"
    latest_volume_it INT DEFAULT 0,    -- Ultimo volume uscito in Italia (da AnimeClick)
    status_jp VARCHAR(50),             -- es. "In corso", "Concluso"
    latest_volume_jp INT DEFAULT 0,    -- Ultimo volume uscito in Giappone (da Anilist)
    
    -- Puntatori per l'automazione dello scraping/API
    animeclick_url TEXT,
    anilist_id INT,
    last_scraped_at TIMESTAMP
);

-- 2. Tabella Dettaglio Volumi Posseduti (Fisici)
CREATE TABLE user_volume (
    id SERIAL PRIMARY KEY,
    series_id INT REFERENCES manga_series(id) ON DELETE CASCADE,
    volume_number INT NOT NULL,
    price_paid DECIMAL(5,2),           -- Quanto l'hai pagato tu (può differire dal prezzo di copertina)
    acquired_at DATE,                  -- Data d'acquisto
    cover_price DECIMAL(5,2),          -- Prezzo di copertina di listino
    notes TEXT,                        -- es. "Edizione Variant", "Preso alla Fiera"
    
    -- Un vincolo per evitare di registrare due volte lo stesso volume per la stessa serie
    CONSTRAINT unique_series_volume UNIQUE(series_id, volume_number)
);