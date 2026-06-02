package com.portal.conecta.mapa_de_sala.entity;

import com.portal.conecta.mapa_de_sala.entity.base.BaseAuditEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Vincula um aprendiz a uma posição dentro de um RoomMap (RN-MS04).
 *
 * Constraints de unicidade (definidas na migration, declaradas aqui para documentação):
 *  - RN-MS10 : UNIQUE (room_map_id, student_id)       → aprendiz em no máximo uma posição por mapa
 *  - Implícita: UNIQUE (room_map_id, layout_position_id) → posição ocupada por no máximo um aprendiz
 */
@Entity
@Table(
        name = "room_map_location",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_room_map_location_student",
                        columnNames = {"room_map_id", "student_id"}
                ),
                @UniqueConstraint(
                        name = "uq_room_map_location_position",
                        columnNames = {"room_map_id", "layout_position_id"}
                )
        }
)
public class RoomMapLocation extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_map_id", nullable = false, insertable = false, updatable = false)
    private UUID roomMapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_map_id", nullable = false)
    private RoomMap roomMap;

    @Column(name = "layout_position_id", nullable = false, insertable = false, updatable = false)
    private UUID layoutPositionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_position_id", nullable = false)
    private LayoutPosition layoutPosition;

    /** FK → Hub User (aprendiz) — sem entidade JPA local */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    public RoomMapLocation() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomMapId() { return roomMapId; }

    public RoomMap getRoomMap() { return roomMap; }
    public void setRoomMap(RoomMap roomMap) {
        this.roomMap = roomMap;
        this.roomMapId = roomMap != null ? roomMap.getId() : null;
    }

    public UUID getLayoutPositionId() { return layoutPositionId; }

    public LayoutPosition getLayoutPosition() { return layoutPosition; }
    public void setLayoutPosition(LayoutPosition layoutPosition) {
        this.layoutPosition = layoutPosition;
        this.layoutPositionId = layoutPosition != null ? layoutPosition.getId() : null;
    }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
}