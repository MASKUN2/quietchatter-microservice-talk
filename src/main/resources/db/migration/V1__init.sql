-- Talk 테이블 생성
CREATE TABLE IF NOT EXISTS talk
(
    id               UUID PRIMARY KEY,
    created_at       TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL,
    book_id          UUID NOT NULL,
    member_id        UUID NOT NULL,
    nickname         VARCHAR(255) NOT NULL,
    content          VARCHAR(250) NOT NULL,
    date_to_hidden   DATE,
    is_hidden        BOOLEAN NOT NULL DEFAULT FALSE,
    like_count       BIGINT NOT NULL DEFAULT 0,
    support_count    BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_talk_book_id ON talk (book_id);
CREATE INDEX IF NOT EXISTS idx_talk_member_id ON talk (member_id);
CREATE INDEX IF NOT EXISTS idx_talk_created_at ON talk (created_at);
CREATE INDEX IF NOT EXISTS idx_talk_date_to_hidden_is_hidden ON talk (date_to_hidden, is_hidden);

-- Reaction 테이블 생성
CREATE TABLE IF NOT EXISTS reaction
(
    id               UUID PRIMARY KEY,
    talk_id          UUID NOT NULL,
    member_id        UUID NOT NULL,
    type             VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_reaction_talk_member_type UNIQUE (talk_id, member_id, type)
);

CREATE INDEX IF NOT EXISTS idx_reaction_talk_id ON reaction (talk_id);
CREATE INDEX IF NOT EXISTS idx_reaction_member_id ON reaction (member_id);
CREATE INDEX IF NOT EXISTS idx_reaction_type ON reaction (type);
