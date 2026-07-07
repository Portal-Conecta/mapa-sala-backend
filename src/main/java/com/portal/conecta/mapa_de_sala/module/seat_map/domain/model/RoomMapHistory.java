package com.portal.conecta.mapa_de_sala.module.seat_map.domain.model;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.base.BaseAuditEntity;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Auditoria de alterações no mapa (RN-MS11).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mapa_sala_historico")
public class RoomMapHistory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_map_id", nullable = false, insertable = false, updatable = false)
    private UUID roomMapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_map_id", nullable = false)
    private RoomMap roomMap;

    /** FK → Hub User (docente/operador) — sem entidade JPA local */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RoomMapHistoryAction action;

    /**
     * Descrição livre da alteração em texto longo.
     * Não usar jsonb — campo texto simples conforme especificação.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    public static RoomMapHistory create(
            RoomMap roomMap,
            UUID userId,
            RoomMapHistoryAction action,
            String details
    ) {
        RoomMapHistory history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(userId);
        history.setAction(action);
        history.setDetails(details);
        return history;
    }
}
