CREATE TABLE app_users (
 id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE auth_sessions (
 token_hash VARCHAR(64) PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_users(id),
 expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX auth_sessions_user_idx ON auth_sessions(user_id);
CREATE TABLE workspaces (
 id UUID PRIMARY KEY, owner_id UUID NOT NULL REFERENCES app_users(id), name VARCHAR(100) NOT NULL,
 platform VARCHAR(20) NOT NULL, account VARCHAR(254), connection_status VARCHAR(30) NOT NULL,
 billing_status VARCHAR(30) NOT NULL, invite_token UUID NOT NULL UNIQUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE workspace_members (
 workspace_id UUID NOT NULL REFERENCES workspaces(id), user_id UUID NOT NULL REFERENCES app_users(id),
 role VARCHAR(20) NOT NULL, PRIMARY KEY(workspace_id,user_id)
);
CREATE INDEX workspace_members_user_idx ON workspace_members(user_id);
CREATE TABLE chat_messages (
 id UUID PRIMARY KEY, workspace_id UUID NOT NULL REFERENCES workspaces(id),
 message_key VARCHAR(64) NOT NULL, sender VARCHAR(254) NOT NULL,
 sent_at TIMESTAMP WITH TIME ZONE NOT NULL, content TEXT NOT NULL,
 UNIQUE(workspace_id,message_key)
);
CREATE TABLE import_runs (
 id UUID PRIMARY KEY, workspace_id UUID NOT NULL REFERENCES workspaces(id),
 filename VARCHAR(255) NOT NULL, new_messages INTEGER NOT NULL, duplicate_messages INTEGER NOT NULL,
 ai_provider VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE tasks (
 id UUID PRIMARY KEY, workspace_id UUID NOT NULL REFERENCES workspaces(id),
 import_id UUID REFERENCES import_runs(id), title VARCHAR(200) NOT NULL, description TEXT,
 assignee_name VARCHAR(100), due_at TIMESTAMP WITH TIME ZONE, priority VARCHAR(20) NOT NULL,
 status VARCHAR(20) NOT NULL, source_message_ids TEXT NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX tasks_workspace_idx ON tasks(workspace_id,created_at);
CREATE TABLE agenda_entries (
 id UUID PRIMARY KEY, workspace_id UUID NOT NULL REFERENCES workspaces(id),
 import_id UUID NOT NULL REFERENCES import_runs(id), title VARCHAR(200) NOT NULL,
 starts_at TIMESTAMP WITH TIME ZONE, status VARCHAR(20) NOT NULL,
 source_message_ids TEXT NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX agenda_workspace_idx ON agenda_entries(workspace_id,created_at);

