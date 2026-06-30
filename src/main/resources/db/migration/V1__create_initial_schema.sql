-- =============================================================
-- MODULO: Mapa de Sala
-- Versao: V1
-- Descricao: Schema inicial completo do modulo seat_map
-- =============================================================

-- -------------------------------------------------------------
-- layout_template
-- Template reutilizavel de grid (dimensoes + posicoes).
-- -------------------------------------------------------------
CREATE TABLE layout_template (
    id              UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    dimension_x     INT          NOT NULL,
    dimension_y     INT          NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    removed_at      TIMESTAMP,
    removed_by      UUID,
    created_by      UUID,
    updated_by      UUID,

    CONSTRAINT pk_layout_template PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- layout_position
-- Posicao individual dentro de um template (coordenada + tipo).
-- -------------------------------------------------------------
CREATE TABLE layout_position (
    id                  UUID        NOT NULL,
    layout_template_id  UUID        NOT NULL,
    position_x          INT         NOT NULL,
    position_y          INT         NOT NULL,
    type                VARCHAR(50) NOT NULL,

    created_at          TIMESTAMP   NOT NULL,
    updated_at          TIMESTAMP   NOT NULL,
    removed_at          TIMESTAMP,
    removed_by          UUID,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_layout_position
        PRIMARY KEY (id),
    CONSTRAINT fk_layout_position_template
        FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id),
    CONSTRAINT ck_layout_position_type
        CHECK (type IN ('STUDENT', 'TEACHER', 'EQUIPMENT', 'OBSTACLE'))
);

-- -------------------------------------------------------------
-- room_layout
-- Vinculo entre sala fisica (Hub) e template de layout.
-- -------------------------------------------------------------
CREATE TABLE room_layout (
    id                  UUID      NOT NULL,
    room_id             UUID      NOT NULL,
    layout_template_id  UUID      NOT NULL,

    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    removed_at          TIMESTAMP,
    removed_by          UUID,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_layout
        PRIMARY KEY (id),
    CONSTRAINT fk_room_layout_template
        FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id)
);

-- -------------------------------------------------------------
-- room_map
-- Mapa de sala: vinculo turma + sala com snapshot do template.
-- -------------------------------------------------------------
CREATE TABLE room_map (
    id                  UUID      NOT NULL,
    class_id            UUID      NOT NULL,
    room_id             UUID      NOT NULL,
    layout_template_id  UUID      NOT NULL,

    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    removed_at          TIMESTAMP,
    removed_by          UUID,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_map
        PRIMARY KEY (id),
    CONSTRAINT fk_room_map_template
        FOREIGN KEY (layout_template_id)
        REFERENCES layout_template (id)
);

-- -------------------------------------------------------------
-- room_map_location
-- Alocacao: aprendiz (Hub) <-> posicao no mapa.
-- Constraints garantem: 1 aluno por mapa, 1 aluno por posicao.
-- -------------------------------------------------------------
CREATE TABLE room_map_location (
    id                  UUID      NOT NULL,
    room_map_id         UUID      NOT NULL,
    layout_position_id  UUID      NOT NULL,
    student_id          UUID      NOT NULL,

    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    removed_at          TIMESTAMP,
    removed_by          UUID,
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT pk_room_map_location
        PRIMARY KEY (id),
    CONSTRAINT fk_room_map_location_map
        FOREIGN KEY (room_map_id)
        REFERENCES room_map (id),
    CONSTRAINT fk_room_map_location_position
        FOREIGN KEY (layout_position_id)
        REFERENCES layout_position (id),
    CONSTRAINT uq_room_map_location_student
        UNIQUE (room_map_id, student_id),
    CONSTRAINT uq_room_map_location_position
        UNIQUE (room_map_id, layout_position_id)
);

-- -------------------------------------------------------------
-- mapa_sala_historico
-- Auditoria de alteracoes em mapas de sala (RN-MS11).
-- -------------------------------------------------------------
CREATE TABLE mapa_sala_historico (
    id          UUID        NOT NULL,
    room_map_id UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    action      VARCHAR(50) NOT NULL,
    details     TEXT,

    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    removed_at  TIMESTAMP,
    removed_by  UUID,
    created_by  UUID,
    updated_by  UUID,

    CONSTRAINT pk_mapa_sala_historico
        PRIMARY KEY (id),
    CONSTRAINT fk_historico_room_map
        FOREIGN KEY (room_map_id)
        REFERENCES room_map (id),
    CONSTRAINT ck_historico_action CHECK (action IN (
        'MAP_CREATION',
        'MAP_REPLICATED',
        'MAP_UPDATED',
        'MAP_DELETED',
        'MAP_ARCHIVED',
        'STUDENT_ASSIGNED',
        'STUDENT_MOVED',
        'STUDENT_UNASSIGNED',
        'STUDENTS_SWAPPED'
    ))
);
