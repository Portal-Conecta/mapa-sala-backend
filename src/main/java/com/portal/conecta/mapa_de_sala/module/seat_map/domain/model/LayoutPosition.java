package com.portal.conecta.mapa_de_sala.domain.model;

import com.portal.conecta.mapa_de_sala.domain.base.BaseAuditEntity;
import com.portal.conecta.mapa_de_sala.domain.enums.LayoutPositionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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
}