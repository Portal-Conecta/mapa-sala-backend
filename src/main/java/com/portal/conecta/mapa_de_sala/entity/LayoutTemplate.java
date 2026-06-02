package com.portal.conecta.mapa_de_sala.entity;

import com.portal.conecta.mapa_de_sala.entity.base.BaseAuditEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public LayoutTemplate() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDimensionX() { return dimensionX; }
    public void setDimensionX(Integer dimensionX) { this.dimensionX = dimensionX; }

    public Integer getDimensionY() { return dimensionY; }
    public void setDimensionY(Integer dimensionY) { this.dimensionY = dimensionY; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<LayoutPosition> getPositions() { return positions; }
    public void setPositions(List<LayoutPosition> positions) { this.positions = positions; }

    public List<RoomLayout> getRoomLayouts() { return roomLayouts; }
    public void setRoomLayouts(List<RoomLayout> roomLayouts) { this.roomLayouts = roomLayouts; }

    public List<RoomMap> getRoomMaps() { return roomMaps; }
    public void setRoomMaps(List<RoomMap> roomMaps) { this.roomMaps = roomMaps; }
}