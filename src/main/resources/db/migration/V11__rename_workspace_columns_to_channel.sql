-- Channel is the top-level collaboration scope; rename dependent foreign-key columns.
ALTER TABLE channels_members RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE chat_messages RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE import_runs RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE tasks RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE agenda_entries RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE routines RENAME COLUMN workspace_id TO channel_id;
ALTER TABLE rag_chunks RENAME COLUMN workspace_id TO channel_id;
