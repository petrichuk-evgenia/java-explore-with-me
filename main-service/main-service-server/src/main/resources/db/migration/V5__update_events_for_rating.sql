-- Добавляем поля для кэширования рейтинга в таблицу событий
ALTER TABLE events ADD COLUMN IF NOT EXISTS likes_count BIGINT DEFAULT 0;
ALTER TABLE events ADD COLUMN IF NOT EXISTS dislikes_count BIGINT DEFAULT 0;
ALTER TABLE events ADD COLUMN IF NOT EXISTS rating INTEGER DEFAULT 0;

-- Функция для обновления рейтинга события
CREATE OR REPLACE FUNCTION update_event_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE events
    SET likes_count = (
        SELECT COUNT(*) FROM reactions
        WHERE event_id = NEW.event_id AND reaction_type = 'LIKE'
    ),
    dislikes_count = (
        SELECT COUNT(*) FROM reactions
        WHERE event_id = NEW.event_id AND reaction_type = 'DISLIKE'
    ),
    rating = CASE
        WHEN (SELECT COUNT(*) FROM reactions WHERE event_id = NEW.event_id) > 0 THEN
            (SELECT COUNT(*) FROM reactions WHERE event_id = NEW.event_id AND reaction_type = 'LIKE') * 100 /
            (SELECT COUNT(*) FROM reactions WHERE event_id = NEW.event_id)
        ELSE 0
    END
    WHERE id = NEW.event_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления рейтинга
DROP TRIGGER IF EXISTS update_event_rating_trigger ON reactions;
CREATE TRIGGER update_event_rating_trigger
AFTER INSERT OR UPDATE OR DELETE ON reactions
FOR EACH ROW
EXECUTE FUNCTION update_event_rating();