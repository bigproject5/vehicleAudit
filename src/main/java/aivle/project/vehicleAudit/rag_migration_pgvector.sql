-- Enable pgvector and create RAG table
CREATE EXTENSION IF NOT EXISTS vector;

-- Choose dimension per embedding model:
--   text-embedding-3-large => 3072
--   text-embedding-3-small => 1536
--   BAAI/bge-m3            => 1024
CREATE TABLE IF NOT EXISTS guide_chunks (
  id bigserial PRIMARY KEY,
  process_code text NOT NULL,
  doc_name text,
  section text,
  version text,
  content text NOT NULL,
  keywords text[],
  embedding vector(1536)  -- ★ embed 모델과 차원 일치!
);

CREATE INDEX IF NOT EXISTS idx_chunks_vec ON guide_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists=100);
CREATE INDEX IF NOT EXISTS idx_chunks_proc ON guide_chunks (process_code);
