-- Миграция для добавления колонки pinned_at в таблицу user_pinned_threads
ALTER TABLE IF EXISTS user_pinned_threads 
ADD COLUMN IF NOT EXISTS pinned_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;
