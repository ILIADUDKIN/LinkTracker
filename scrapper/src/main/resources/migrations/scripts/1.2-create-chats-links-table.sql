--liquibase formatted sql

--changeset steshabolk:create-chats-links-table
CREATE TABLE IF NOT EXISTS chats_links
(
    chat_id
    BIGINT
    REFERENCES
    chats
(
    id
) ON DELETE CASCADE,
    link_id BIGINT REFERENCES links
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    chat_id,
    link_id
)
    );
