package com.portal.conecta.mapa_de_sala.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "room_map")
public class RoomMap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "layout_template_id", nullable = false, insertable = false, updatable = false)
    private UUID layoutTemplateId;

    /**
     * Snapshot de segurança: referência ao template vigente no momento da criação do mapa (RN-MS01, RN-MS02).
     * Mantido mesmo que o template seja posteriormente desativado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_template_id", nullable = false)
    private LayoutTemplate layoutTemplate;

    @OneToMany(mappedBy = "roomMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMapLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "roomMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMapHistory> history = new ArrayList<>();

    public RoomMap() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClassId() {
        return classId;
    }

    public void setClassId(UUID classId) {
        this.classId = classId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getLayoutTemplateId() {
        return layoutTemplateId;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
        this.layoutTemplateId = layoutTemplate != null ? layoutTemplate.getId() : null;
    }

    public List<RoomMapLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<RoomMapLocation> locations) {
        this.locations = locations;
    }

    public List<RoomMapHistory> getHistory() {
        return history;
    }

    public void setHistory(List<RoomMapHistory> history) {
        this.history = history;
    }
}