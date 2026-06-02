package com.portal.conecta.mapa_de_sala.entity;

import com.portal.conecta.mapa_de_sala.entity.base.BaseAuditEntity;
import com.portal.conecta.mapa_de_sala.entity.enums.LayoutPositionType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "layout_position")
public class LayoutPosition extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "layout_template_id", nullable = false, insertable = false, updatable = false)
    private UUID layoutTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_template_id", nullable = false)
    private LayoutTemplate layoutTemplate;

    @Column(name = "position_x", nullable = false)
    private Integer positionX;

    @Column(name = "position_y", nullable = false)
    private Integer positionY;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LayoutPositionType type;

    public LayoutPosition() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLayoutTemplateId() { return layoutTemplateId; }

    public LayoutTemplate getLayoutTemplate() { return layoutTemplate; }
    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
        this.layoutTemplateId = layoutTemplate != null ? layoutTemplate.getId() : null;
    }

    public Integer getPositionX() { return positionX; }
    public void setPositionX(Integer positionX) { this.positionX = positionX; }

    public Integer getPositionY() { return positionY; }
    public void setPositionY(Integer positionY) { this.positionY = positionY; }

    public LayoutPositionType getType() { return type; }
    public void setType(LayoutPositionType type) { this.type = type; }
}