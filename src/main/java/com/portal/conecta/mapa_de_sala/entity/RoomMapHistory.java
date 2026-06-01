package com.portal.conecta.mapa_de_sala.entity;

import com.portal.conecta.mapa_de_sala.entity.enums.RoomMapHistoryAction;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Registro de auditoria de alterações no RoomMap (RN-MS11).
 */
@Entity
@Table(name = "mapa_sala_historico")
public class RoomMapHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_map_id", nullable = false, insertable = false, updatable = false)
    private UUID roomMapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_map_id", nullable = false)
    private RoomMap roomMap;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RoomMapHistoryAction action;

    /**
     * Descrição livre da alteração. Armazenado como texto longo.
     * Ex.: "João movido da posição A3 para B5".
     * Não usar jsonb — campo texto simples conforme especificação.
     */
    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    public RoomMapHistory() {
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public RoomMapHistoryAction getAction() {
        return action;
    }

    public void setAction(RoomMapHistoryAction action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}