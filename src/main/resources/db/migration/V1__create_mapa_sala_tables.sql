-- ============================================================
-- V1__create_mapa_sala_tables.sql
-- Módulo Mapa de Sala — criação de todas as tabelas e constraints
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. layout_template
--    Molde de sala cadastrado via equipe técnica (RN-MS-LT01, LT03)
-- ────────────────────────────────────────────────────────────
CREATE TABLE layout_template (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    dimension_x  INTEGER     NOT NULL,
    dimension_y  INTEGER     NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,

    -- Auditoria
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    removed_at   TIMESTAMP,
    created_by   UUID,
    updated_by   UUID,

    CONSTRAINT pk_layout_template PRIMARY KEY (id)
);

-- ────────────────────────────────────────────────────────────
-- 2. layout_position
--    Assento/espaço físico dentro de um template (RN-MS-LT03)
-- ────────────────────────────────────────────────────────────
CREATE TABLE layout_position (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    layout_template_id  UUID        NOT NULL,
    position_x          INTEGER     NOT NULL,
    position_y          INTEGER     NOT NULL,
    type                VARCHAR(20) NOT NULL,   -- STUDENT | TEACHER | EQUIPMENT | OBSTACLE

    -- Auditoria
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    removed_at          TIMESTAMP,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_layout_position            PRIMARY KEY (id),
    CONSTRAINT fk_layout_position_template   FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id),
    CONSTRAINT chk_layout_position_type      CHECK (type IN ('STUDENT','TEACHER','EQUIPMENT','OBSTACLE')),
    -- Duas posições não podem ter a mesma coordenada dentro do mesmo template
    CONSTRAINT uq_layout_position_coord      UNIQUE (layout_template_id, position_x, position_y)
);

-- ────────────────────────────────────────────────────────────
-- 3. room_layout
--    Associa sala física (Hub) a um template (RN-MS-LT02, LT04)
-- ────────────────────────────────────────────────────────────
CREATE TABLE room_layout (
    id                  UUID  NOT NULL DEFAULT gen_random_uuid(),
    room_id             UUID  NOT NULL,   -- FK → Hub Room (UUID, sem entidade local)
    layout_template_id  UUID  NOT NULL,

    -- Auditoria
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    removed_at          TIMESTAMP,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_layout           PRIMARY KEY (id),
    CONSTRAINT fk_room_layout_template  FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id)
);

-- ────────────────────────────────────────────────────────────
-- 4. room_map
--    Cabeçalho do mapa criado pelo docente (RN-MS01, MS02)
-- ────────────────────────────────────────────────────────────
CREATE TABLE room_map (
    id                  UUID  NOT NULL DEFAULT gen_random_uuid(),
    class_id            UUID  NOT NULL,   -- FK → Hub Classes
    room_id             UUID  NOT NULL,   -- FK → Hub Room
    layout_template_id  UUID  NOT NULL,   -- snapshot de segurança (RN-MS01)

    -- Auditoria / soft delete
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    removed_at          TIMESTAMP,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_map           PRIMARY KEY (id),
    CONSTRAINT fk_room_map_template  FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id)
);

-- ────────────────────────────────────────────────────────────
-- 5. room_map_location
--    Vínculo aprendiz ↔ posição no mapa (RN-MS04, MS10)
-- ────────────────────────────────────────────────────────────
CREATE TABLE room_map_location (
    id                  UUID  NOT NULL DEFAULT gen_random_uuid(),
    room_map_id         UUID  NOT NULL,
    layout_position_id  UUID  NOT NULL,
    student_id          UUID  NOT NULL,   -- FK → Hub User (aprendiz)

    -- Auditoria
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    removed_at          TIMESTAMP,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_map_location          PRIMARY KEY (id),
    CONSTRAINT fk_room_map_location_map      FOREIGN KEY (room_map_id)
        REFERENCES room_map (id),
    CONSTRAINT fk_room_map_location_position FOREIGN KEY (layout_position_id)
        REFERENCES layout_position (id),

    -- RN-MS10: um aprendiz não ocupa duas posições no mesmo mapa
    CONSTRAINT uq_room_map_location_student  UNIQUE (room_map_id, student_id),
    -- Constraint implícita: dois aprendizes não na mesma posição
    CONSTRAINT uq_room_map_location_position UNIQUE (room_map_id, layout_position_id)
);

-- ────────────────────────────────────────────────────────────
-- 6. mapa_sala_historico
--    Auditoria de alterações no mapa (RN-MS11)
-- ────────────────────────────────────────────────────────────
CREATE TABLE mapa_sala_historico (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    room_map_id UUID        NOT NULL,
    user_id     UUID        NOT NULL,   -- FK → Hub User (docente/operador)
    action      VARCHAR(30) NOT NULL,   -- MAP_CREATION | STUDENT_MOVED | MAP_REPLICATED
    details     TEXT,

    -- Auditoria
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    removed_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,

    CONSTRAINT pk_mapa_sala_historico        PRIMARY KEY (id),
    CONSTRAINT fk_mapa_sala_historico_map    FOREIGN KEY (room_map_id)
        REFERENCES room_map (id),
    CONSTRAINT chk_mapa_sala_historico_action CHECK (
        action IN ('MAP_CREATION','STUDENT_MOVED','MAP_REPLICATED')
    )
);