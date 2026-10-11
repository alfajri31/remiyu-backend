-- Correct the temporary Channel-as-parent schema. Existing data becomes one initial Channel per Workspace.
ALTER TABLE channels RENAME TO workspaces;

CREATE TABLE channels (
  id UUID PRIMARY KEY,
  workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  owner_id UUID NOT NULL REFERENCES app_users(id),
  name VARCHAR(100) NOT NULL,
  channel_key VARCHAR(500) NOT NULL UNIQUE,
  platform VARCHAR(20) NOT NULL,
  account VARCHAR(254),
  external_group_id VARCHAR(255),
  connection_status VARCHAR(30) NOT NULL,
  billing_status VARCHAR(30) NOT NULL,
  invite_token UUID NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  UNIQUE (workspace_id, platform, external_group_id)
);

-- Preserve every current top-level channel as its own parent Workspace and first child Channel.
INSERT INTO channels (
  id, workspace_id, owner_id, name, channel_key, platform, account, external_group_id,
  connection_status, billing_status, invite_token, created_at
)
SELECT
  id,
  id,
  owner_id,
  name,
  id::text || ':' || lower(regexp_replace(btrim(name), '\\s+', ' ', 'g')),
  platform,
  account,
  NULL,
  connection_status,
  billing_status,
  invite_token,
  created_at
FROM workspaces;

ALTER TABLE workspaces
  DROP COLUMN platform,
  DROP COLUMN account,
  DROP COLUMN connection_status,
  DROP COLUMN billing_status,
  DROP COLUMN invite_token;

-- Existing channel_id values already equal the migrated Channel IDs. Rewire their foreign keys.
DO $$
DECLARE
  target_table TEXT;
  existing_constraint TEXT;
BEGIN
  FOREACH target_table IN ARRAY ARRAY[
    'channels_members', 'chat_messages', 'import_runs', 'tasks',
    'agenda_entries', 'routines', 'rag_chunks'
  ] LOOP
    SELECT tc.constraint_name
      INTO existing_constraint
      FROM information_schema.table_constraints tc
      JOIN information_schema.key_column_usage kcu
        ON kcu.constraint_name = tc.constraint_name
       AND kcu.table_schema = tc.table_schema
      WHERE tc.table_schema = current_schema()
        AND tc.table_name = target_table
        AND tc.constraint_type = 'FOREIGN KEY'
        AND kcu.column_name = 'channel_id'
      LIMIT 1;
    IF existing_constraint IS NOT NULL THEN
      EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', target_table, existing_constraint);
    END IF;
    EXECUTE format(
      'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE',
      target_table,
      target_table || '_channel_id_fkey'
    );
  END LOOP;
END $$;

CREATE INDEX channels_workspace_idx ON channels(workspace_id, created_at);
