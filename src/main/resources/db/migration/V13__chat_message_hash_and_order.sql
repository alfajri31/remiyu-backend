ALTER TABLE chat_messages RENAME COLUMN message_key TO message_hash;
ALTER TABLE chat_messages ALTER COLUMN sent_at DROP NOT NULL;
ALTER TABLE chat_messages ADD COLUMN message_order BIGINT NOT NULL DEFAULT 0;

CREATE INDEX chat_messages_channel_order_idx
  ON chat_messages(channel_id, message_order);
