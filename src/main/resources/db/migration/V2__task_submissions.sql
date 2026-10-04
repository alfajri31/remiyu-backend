CREATE TABLE task_submissions (
 id UUID PRIMARY KEY,
 task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES app_users(id),
 link VARCHAR(2000), note VARCHAR(4000), filename VARCHAR(255), content BYTEA,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX submissions_task_idx ON task_submissions(task_id,created_at);
