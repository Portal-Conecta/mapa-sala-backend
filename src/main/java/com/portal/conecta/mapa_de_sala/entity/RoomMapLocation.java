package com.portal.conecta.mapa_de_sala.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Vincula um aprendiz a uma posição dentro de um RoomMap.
 *
 * Constraints de unicidade (definidas na migration, refletidas aqui):
 *  - RN-MS10: UNIQUE (room_map_id, student_id)   → um aprendiz não ocupa duas posições no mesmo mapa
 *  - Implícita: UNIQUE (room_map_id, layout_position_id) → dois aprendizes não na mesma posição
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
public class RoomMapLocation {

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

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    public RoomMapLocation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoomMapId() {
        return roomMapId;
    }

    public RoomMap getRoomMap() {
        return roomMap;
    }

    public void setRoomMap(RoomMap roomMap) {
        this.roomMap = roomMap;
        this.roomMapId = roomMap != null ? roomMap.getId() : null;
    }

    public UUID getLayoutPositionId() {
        return layoutPositionId;
    }

    public LayoutPosition getLayoutPosition() {
        return layoutPosition;
    }

    public void setLayoutPosition(LayoutPosition layoutPosition) {
        this.layoutPosition = layoutPosition;
        this.layoutPositionId = layoutPosition != null ? layoutPosition.getId() : null;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }
}