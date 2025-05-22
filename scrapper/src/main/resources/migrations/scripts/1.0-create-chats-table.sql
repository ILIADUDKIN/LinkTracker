--liquibase formatted sql

--changeset steshabolk:create-chats-id-seq
CREATE SEQUENCE IF NOT EXISTS chats_id_seq START WITH 1 INCREMENT BY 1;

--changeset steshabolk:create-chats-table
CREATE TABLE IF NOT EXISTS chats
(
    id
    BIGINT
    DEFAULT
    nextval
(
    'chats_id_seq'
) PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL
    );
