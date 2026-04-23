CREATE TABLE IF NOT EXISTS folder (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    parent_folder_id BIGINT NULL,
    tenant_id VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_folder_tenant_id ON folder (tenant_id);
CREATE INDEX IF NOT EXISTS idx_folder_parent_id ON folder (parent_folder_id);

ALTER TABLE note
    ADD COLUMN IF NOT EXISTS folder_id BIGINT NULL;

CREATE INDEX IF NOT EXISTS idx_note_folder_id ON note (folder_id);
