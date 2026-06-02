package com.portal.conecta.mapa_de_sala.domain.model;

import com.portal.conecta.mapa_de_sala.domain.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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
}