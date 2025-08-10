-- RAG columns for inspection
ALTER TABLE inspection
  ADD COLUMN IF NOT EXISTS ai_suggestion TEXT,
  ADD COLUMN IF NOT EXISTS ai_suggestion_level VARCHAR(16),
  ADD COLUMN IF NOT EXISTS ai_suggestion_confidence DOUBLE PRECISION,
  ADD COLUMN IF NOT EXISTS ai_suggestion_sources TEXT;
