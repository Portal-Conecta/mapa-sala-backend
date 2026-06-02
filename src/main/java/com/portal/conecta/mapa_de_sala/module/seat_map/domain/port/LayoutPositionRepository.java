package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;

import java.util.List;
import java.util.UUID;

public interface LayoutPositionRepository extends JpaRepository<LayoutPosition, UUID> {

    List<LayoutPosition> findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(UUID layoutTemplateId);

    List<LayoutPosition> findByLayoutTemplateIdAndType(UUID layoutTemplateId, LayoutPositionType type);
}