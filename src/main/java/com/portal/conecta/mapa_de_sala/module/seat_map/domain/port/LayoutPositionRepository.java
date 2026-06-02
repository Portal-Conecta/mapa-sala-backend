package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LayoutPositionRepository extends JpaRepository<LayoutPosition, UUID> {

    List<LayoutPosition> findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(UUID layoutTemplateId);

    List<LayoutPosition> findByLayoutTemplateIdAndType(UUID layoutTemplateId, LayoutPositionType type);
}