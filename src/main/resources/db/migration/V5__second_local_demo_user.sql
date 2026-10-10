-- Akun demo kedua dengan Workspace/data sendiri agar isolasi data per user dapat diuji.
-- Seluruh data hanya dibuat di profile local ketika seed_enabled=true.

INSERT INTO app_users (id, name, email, password_hash, created_at)
SELECT CAST('20000000-0000-0000-0000-000000000001' AS UUID),
       'Nadia Demo',
       'nadia.demo@remiyu.local',
       '$2a$10$dt7/AYqV0C7Ob142Rqa6Rev3vJRJsg9902K1779uMFguSuyqfo.Ai',
       CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true'
  AND NOT EXISTS (SELECT 1 FROM app_users WHERE email = 'nadia.demo@remiyu.local');

INSERT INTO workspaces (id, owner_id, name, platform, account, connection_status, billing_status, invite_token, created_at)
SELECT CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       (SELECT id FROM app_users WHERE email = 'nadia.demo@remiyu.local'),
       'Konten Kreatif Nadia',
       'TELEGRAM',
       '@nadia_kreatif',
       'CONNECTED',
       'FREE',
       CAST('20000000-0000-0000-0000-000000000011' AS UUID),
       CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM app_users WHERE email = 'nadia.demo@remiyu.local')
  AND NOT EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID));

INSERT INTO workspace_members (workspace_id, user_id, role)
SELECT CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       (SELECT id FROM app_users WHERE email = 'nadia.demo@remiyu.local'),
       'ADMIN'
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID))
  AND NOT EXISTS (
    SELECT 1 FROM workspace_members
    WHERE workspace_id = CAST('20000000-0000-0000-0000-000000000010' AS UUID)
      AND user_id = (SELECT id FROM app_users WHERE email = 'nadia.demo@remiyu.local')
  );

INSERT INTO import_runs (id, workspace_id, filename, new_messages, duplicate_messages, ai_provider, status, created_at)
SELECT CAST('20000000-0000-0000-0000-000000000020' AS UUID),
       CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       'telegram-konten-mingguan.txt', 3, 0, 'mockoon', 'NEEDS_REVIEW', CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID));

INSERT INTO chat_messages (id, workspace_id, message_key, sender, sent_at, content)
SELECT CAST('20000000-0000-0000-0000-000000000031' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       'nadia-konten-001', 'Nadia', CURRENT_TIMESTAMP - INTERVAL '3' HOUR, 'Tolong siapkan kalender konten minggu depan.'
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID))
UNION ALL
SELECT CAST('20000000-0000-0000-0000-000000000032' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       'nadia-konten-002', 'Ayu', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'Saya buatkan desain carousel untuk hari Senin.'
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID))
UNION ALL
SELECT CAST('20000000-0000-0000-0000-000000000033' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID),
       'nadia-konten-003', 'Nadia', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'Kita review draft konten setiap pagi pukul sembilan.'
WHERE '${seed_enabled}' = 'true'
  AND EXISTS (SELECT 1 FROM workspaces WHERE id = CAST('20000000-0000-0000-0000-000000000010' AS UUID));

INSERT INTO tasks (id, workspace_id, import_id, title, description, assignee_name, due_at, priority, status, source_message_ids, created_at, updated_at)
SELECT CAST('20000000-0000-0000-0000-000000000101' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID), CAST('20000000-0000-0000-0000-000000000020' AS UUID),
       'Susun kalender konten', 'Tentukan tema, format, dan jadwal publikasi untuk minggu depan.', 'Nadia', CURRENT_TIMESTAMP + INTERVAL '1' DAY, 'HIGH', 'IN_PROGRESS', '["20000000-0000-0000-0000-000000000031"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true'
UNION ALL
SELECT CAST('20000000-0000-0000-0000-000000000102' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID), CAST('20000000-0000-0000-0000-000000000020' AS UUID),
       'Desain carousel Senin', 'Buat rancangan carousel untuk konten pembuka minggu depan.', 'Ayu', CURRENT_TIMESTAMP + INTERVAL '2' DAY, 'MEDIUM', 'TODO', '["20000000-0000-0000-0000-000000000032"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true'
UNION ALL
SELECT CAST('20000000-0000-0000-0000-000000000103' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID), CAST('20000000-0000-0000-0000-000000000020' AS UUID),
       'Cek caption dan CTA', 'Pastikan caption dan ajakan tindakan konsisten dengan kalender konten.', 'Nadia', CURRENT_TIMESTAMP + INTERVAL '3' DAY, 'LOW', 'DRAFT', '["20000000-0000-0000-0000-000000000033"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true';

INSERT INTO agenda_entries (id, workspace_id, import_id, title, starts_at, status, source_message_ids, created_at)
SELECT CAST('20000000-0000-0000-0000-000000000201' AS UUID), CAST('20000000-0000-0000-0000-000000000010' AS UUID), CAST('20000000-0000-0000-0000-000000000020' AS UUID),
       'Review draft konten pagi', CURRENT_TIMESTAMP + INTERVAL '1' DAY, 'CONFIRMED', '["20000000-0000-0000-0000-000000000033"]', CURRENT_TIMESTAMP
WHERE '${seed_enabled}' = 'true';
