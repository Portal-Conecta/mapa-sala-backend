package com.portal.conecta.mapa_de_sala.module.seat_map.domain.model;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.base.BaseAuditEntity;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "room_map")
public class RoomMap extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** FK → Hub Classes (sem entidade JPA local) */
    @Column(name = "class_id", nullable = false)
    private UUID classId;

    /** FK → Hub Room (sem entidade JPA local) */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "layout_template_id", nullable = false, insertable = false, updatable = false)
    private UUID layoutTemplateId;

    /**
     * Snapshot de segurança: referência ao template vigente no momento da criação (RN-MS01, RN-MS02).
     * Mantido mesmo que o template seja posteriormente desativado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_template_id", nullable = false)
    private LayoutTemplate layoutTemplate;

    @OneToMany(mappedBy = "roomMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMapLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "roomMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMapHistory> history = new ArrayList<>();

    public static RoomMap create(UUID classId, UUID roomId, LayoutTemplate template, List<RoomMapLocation> locations, UUID createdByUserId) {
        RoomMap roomMap = new RoomMap();
        roomMap.setClassId(classId);
        roomMap.setRoomId(roomId);
        roomMap.setLayoutTemplateId(template.getId());
        roomMap.setLayoutTemplate(template);

        if (locations != null) {
            locations.forEach(roomMap::addLocation);
        }

        roomMap.addHistory(RoomMapHistory.create(
                roomMap,
                createdByUserId,
                RoomMapHistoryAction.MAP_CREATION,
                "Cria\u00e7\u00e3o inicial do mapa"
        ));

        return roomMap;
    }

    public static RoomMap replicateFrom(RoomMap sourceMap, UUID targetRoomId, UUID userId) {
        RoomMap roomMap = new RoomMap();
        roomMap.setClassId(sourceMap.getClassId());
        roomMap.setRoomId(targetRoomId);
        roomMap.setLayoutTemplateId(sourceMap.getLayoutTemplateId());
        roomMap.setLayoutTemplate(sourceMap.getLayoutTemplate());

        sourceMap.getLocations().stream()
                .map(RoomMapLocation::replicateFrom)
                .forEach(roomMap::addLocation);

        roomMap.addHistory(RoomMapHistory.create(
                roomMap,
                userId,
                RoomMapHistoryAction.MAP_REPLICATED,
                "Mapa criado por replica\u00e7\u00e3o a partir do mapa " + sourceMap.getId() + "."
        ));

        return roomMap;
    }

    public void addLocation(RoomMapLocation location) {
        location.setRoomMap(this);
        locations.add(location);
    }

    public void addHistory(RoomMapHistory history) {
        history.setRoomMap(this);
        this.history.add(history);
    }
}
