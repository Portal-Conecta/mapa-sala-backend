package com.portal.conecta.mapa_de_sala.entity;

import com.portal.conecta.mapa_de_sala.entity.base.BaseAuditEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "room_layout")
public class RoomLayout extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** FK → Hub Room (sem entidade JPA local — bounded context isolado) */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "layout_template_id", nullable = false, insertable = false, updatable = false)
    private UUID layoutTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_template_id", nullable = false)
    private LayoutTemplate layoutTemplate;

    public RoomLayout() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public UUID getLayoutTemplateId() { return layoutTemplateId; }

    public LayoutTemplate getLayoutTemplate() { return layoutTemplate; }
    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
        this.layoutTemplateId = layoutTemplate != null ? layoutTemplate.getId() : null;
    }
}