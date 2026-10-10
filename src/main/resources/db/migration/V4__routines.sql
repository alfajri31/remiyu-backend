CREATE TABLE routines (
 id UUID PRIMARY KEY, workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
 title VARCHAR(200) NOT NULL, schedule VARCHAR(200) NOT NULL, source VARCHAR(200) NOT NULL,
 quote VARCHAR(1000) NOT NULL, source_message_id UUID REFERENCES chat_messages(id),
 status VARCHAR(20) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX routines_workspace_idx ON routines(workspace_id,created_at);
