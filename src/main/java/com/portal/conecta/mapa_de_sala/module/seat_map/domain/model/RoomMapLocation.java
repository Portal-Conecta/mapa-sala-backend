package com.portal.conecta.mapa_de_sala.module.seat_map.domain.model;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Vínculo aprendiz ↔ posição no mapa (RN-MS04).
 *
 * Constraints de unicidade:
 *  - RN-MS10   : UNIQUE (room_map_id, student_id)
 *  - Implícita : UNIQUE (room_map_id, layout_position_id)
 */
@Getter
@Setter
@NoArgsConstructor
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

    public static RoomMapLocation create(UUID studentId, LayoutPosition layoutPosition) {
        RoomMapLocation location = new RoomMapLocation();
        location.setStudentId(studentId);
        location.setLayoutPositionId(layoutPosition.getId());
        location.setLayoutPosition(layoutPosition);
        return location;
    }

    public static RoomMapLocation replicateFrom(RoomMapLocation sourceLocation) {
        return create(sourceLocation.getStudentId(), sourceLocation.getLayoutPosition());
    }
}
