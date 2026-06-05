-- Таблица для хранения реакций (лайков/дизлайков)
CREATE TABLE IF NOT EXISTS reactions (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reaction_type VARCHAR(10) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (event_id, user_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_reactions_event_id ON reactions(event_id);
CREATE INDEX IF NOT EXISTS idx_reactions_user_id ON reactions(user_id);
CREATE INDEX IF NOT EXISTS idx_reactions_type ON reactions(reaction_type);
CREATE INDEX IF NOT EXISTS idx_reactions_created ON reactions(created_date);

-- Индекс для поиска топ-событий по рейтингу
CREATE INDEX IF NOT EXISTS idx_reactions_event_type ON reactions(event_id, reaction_type);