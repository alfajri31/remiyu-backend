-- Keep one primary source message directly addressable from each AI result.
-- source_message_ids remains the complete provenance list for results built from multiple messages.
ALTER TABLE tasks
  ADD COLUMN chat_message_id UUID REFERENCES chat_messages(id) ON DELETE SET NULL;

ALTER TABLE agenda_entries
  ADD COLUMN chat_message_id UUID REFERENCES chat_messages(id) ON DELETE SET NULL;

ALTER TABLE routines
  RENAME COLUMN source_message_id TO chat_message_id;

CREATE INDEX tasks_chat_message_idx ON tasks(chat_message_id);
CREATE INDEX agenda_entries_chat_message_idx ON agenda_entries(chat_message_id);
CREATE INDEX routines_chat_message_idx ON routines(chat_message_id);
