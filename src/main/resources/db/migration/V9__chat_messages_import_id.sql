-- Existing messages remain intact; their import_id is NULL because their source import was not recorded.
ALTER TABLE chat_messages
  ADD COLUMN import_id UUID REFERENCES import_runs(id) ON DELETE SET NULL;

CREATE INDEX chat_messages_import_idx ON chat_messages(import_id);
