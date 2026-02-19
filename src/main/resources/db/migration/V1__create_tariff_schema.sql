CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'consumer_category') THEN
CREATE TYPE consumer_category AS ENUM ('COMMERCIAL', 'INDUSTRIAL', 'RESIDENTIAL', 'PUBLIC');
END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tariff_status') THEN
CREATE TYPE tariff_status AS ENUM ('ACTIVE', 'INACTIVE', 'DELETED');
END IF;
END $$;

CREATE TABLE IF NOT EXISTS tariff_table (
                                            id              BIGSERIAL PRIMARY KEY,
                                            name            VARCHAR(120) NOT NULL,
    valid_from      DATE NULL,
    valid_to        DATE NULL,
    status          tariff_status NOT NULL DEFAULT 'ACTIVE',
    deleted_at      TIMESTAMP NULL,

    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_tariff_validity
    CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to),

    CONSTRAINT ck_deleted_status
    CHECK ((status <> 'DELETED' AND deleted_at IS NULL)
    OR (status = 'DELETED' AND deleted_at IS NOT NULL))
    );

CREATE INDEX idx_tariff_table_status
    ON tariff_table(status);

-- LINK BETWEEN TARIFF TABLE AND CATEGORY
CREATE TABLE IF NOT EXISTS tariff_category (
                                               id              BIGSERIAL PRIMARY KEY,
                                               tariff_table_id BIGINT NOT NULL REFERENCES tariff_table(id) ON DELETE CASCADE,
    category        consumer_category NOT NULL,

    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_tariff_category UNIQUE (tariff_table_id, category)
    );

CREATE INDEX idx_tariff_category_table
    ON tariff_category(tariff_table_id);

CREATE TABLE IF NOT EXISTS consumption_range (
                                                 id                   BIGSERIAL PRIMARY KEY,
                                                 tariff_category_id   BIGINT NOT NULL REFERENCES tariff_category(id) ON DELETE CASCADE,

    range_start          INT NOT NULL,
    range_end            INT NOT NULL,
    unit_price           NUMERIC(12,2) NOT NULL,

    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_range_limits CHECK (range_start >= 0 AND range_start < range_end),
    CONSTRAINT ck_unit_price CHECK (unit_price >= 0)
    );

CREATE INDEX idx_consumption_range_category
    ON consumption_range(tariff_category_id);

ALTER TABLE consumption_range
    ADD CONSTRAINT ex_no_range_overlap
    EXCLUDE USING gist (
        tariff_category_id WITH =,
        int4range(range_start, range_end, '[]') WITH &&
    );
