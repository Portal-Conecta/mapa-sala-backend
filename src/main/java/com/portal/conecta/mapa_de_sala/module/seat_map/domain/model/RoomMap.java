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
            locations.forEach(loc -> {
                loc.setRoomMap(roomMap);
                roomMap.getLocations().add(loc);
            });
        }

        RoomMapHistory history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(createdByUserId);
        history.setAction(RoomMapHistoryAction.MAP_CREATION);
        history.setDetails("Criação inicial do mapa");

        roomMap.getHistory().add(history);

        return roomMap;
    }
}