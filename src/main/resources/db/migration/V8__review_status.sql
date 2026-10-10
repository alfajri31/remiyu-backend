-- Review is separate from each item's operational status.
-- Existing and newly generated AI results begin in PENDING until reviewed by the user.
ALTER TABLE tasks
  ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ADD CONSTRAINT tasks_review_status_check
    CHECK (review_status IN ('PENDING', 'APPROVED', 'REJECTED'));

ALTER TABLE agenda_entries
  ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ADD CONSTRAINT agenda_entries_review_status_check
    CHECK (review_status IN ('PENDING', 'APPROVED', 'REJECTED'));

ALTER TABLE routines
  ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ADD CONSTRAINT routines_review_status_check
    CHECK (review_status IN ('PENDING', 'APPROVED', 'REJECTED'));

CREATE INDEX tasks_review_status_idx ON tasks(workspace_id, review_status);
CREATE INDEX agenda_entries_review_status_idx ON agenda_entries(workspace_id, review_status);
CREATE INDEX routines_review_status_idx ON routines(workspace_id, review_status);
