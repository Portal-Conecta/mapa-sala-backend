package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;

import java.util.Optional;
import java.util.UUID;

public interface LayoutTemplateRepository extends JpaRepository<LayoutTemplate, UUID> {

    Optional<LayoutTemplate> findByIdAndActiveTrue(UUID id);
}
