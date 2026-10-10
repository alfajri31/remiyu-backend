-- Fondasi RAG hanya untuk PostgreSQL.
-- Dimensi 1536 sesuai text-embedding-3-small; jika model embedding diganti,
-- lakukan re-embedding melalui migration baru, jangan mencampur dimensi dalam index ini.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE rag_chunks (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  source_message_id UUID REFERENCES chat_messages(id) ON DELETE SET NULL,
  source_type VARCHAR(30) NOT NULL,
  source_id UUID,
  content TEXT NOT NULL,
  content_hash VARCHAR(64) NOT NULL,
  embedding vector(1536),
  embedding_model VARCHAR(100) NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT rag_chunks_source_type_check CHECK (source_type IN ('CHAT_MESSAGE', 'TASK', 'AGENDA', 'DOCUMENT')),
  CONSTRAINT rag_chunks_workspace_content_hash_unique UNIQUE (workspace_id, content_hash)
);

CREATE INDEX rag_chunks_user_workspace_idx ON rag_chunks(user_id, workspace_id, created_at DESC);
CREATE INDEX rag_chunks_workspace_idx ON rag_chunks(workspace_id, created_at DESC);
CREATE INDEX rag_chunks_embedding_hnsw_idx
  ON rag_chunks USING hnsw (embedding vector_cosine_ops)
  WITH (m = 16, ef_construction = 64);
