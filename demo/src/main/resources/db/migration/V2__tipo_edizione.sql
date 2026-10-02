ALTER TABLE user_volume DROP CONSTRAINT IF EXISTS unique_series_volume_edizione;
ALTER TABLE user_volume DROP CONSTRAINT IF EXISTS unique_series_volume;

ALTER TABLE user_volume
    ADD COLUMN edition_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    ADD COLUMN title VARCHAR(255);

UPDATE user_volume SET edition_type = 'VARIANT'
WHERE edizione IS NOT NULL AND edizione <> 'Normale';

ALTER TABLE user_volume ALTER COLUMN volume_number DROP NOT NULL;

ALTER TABLE user_volume
    ADD CONSTRAINT chk_regular_has_number
    CHECK (edition_type <> 'REGULAR' OR volume_number IS NOT NULL);

CREATE UNIQUE INDEX uq_regular_volume
    ON user_volume (series_id, volume_number)
    WHERE edition_type = 'REGULAR';