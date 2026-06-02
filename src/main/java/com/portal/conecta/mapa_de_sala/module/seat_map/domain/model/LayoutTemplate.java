package com.portal.conecta.mapa_de_sala.domain.model;

import com.portal.conecta.mapa_de_sala.domain.base.BaseAuditEntity;
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
@Table(name = "layout_template")
public class LayoutTemplate extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dimension_x", nullable = false)
    private Integer dimensionX;

    @Column(name = "dimension_y", nullable = false)
    private Integer dimensionY;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "layoutTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LayoutPosition> positions = new ArrayList<>();

    @OneToMany(mappedBy = "layoutTemplate", cascade = CascadeType.ALL)
    private List<RoomLayout> roomLayouts = new ArrayList<>();

    @OneToMany(mappedBy = "layoutTemplate", cascade = CascadeType.ALL)
    private List<RoomMap> roomMaps = new ArrayList<>();
}