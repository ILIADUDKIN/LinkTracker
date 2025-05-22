--liquibase formatted sql

--changeset steshabolk:create-links-id-seq
CREATE SEQUENCE IF NOT EXISTS links_id_seq START WITH 1 INCREMENT BY 1;

--changeset steshabolk:create-links-table
CREATE TABLE IF NOT EXISTS links
(
    id
    BIGINT
    DEFAULT
    nextval
(
    'links_id_seq'
) PRIMARY KEY,
    link_type SMALLINT NOT NULL,
    url VARCHAR
(
    255
) UNIQUE NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    status SMALLINT NOT NULL
    );
