-- Channel is the current top-level collaboration scope.
ALTER TABLE workspace_members RENAME TO channels_members;
ALTER TABLE workspaces RENAME TO channels;

ALTER INDEX workspace_members_user_idx RENAME TO channels_members_user_idx;
