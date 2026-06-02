package com.portal.conecta.mapa_de_sala.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

/**
 * Configuração JPA do módulo Mapa de Sala.
 *
 * - Habilita JPA Auditing para popular createdAt, updatedAt, createdBy, updatedBy
 *   automaticamente via {@link BaseAuditEntity}.
 * - {@link AuditorAware} deve retornar o UUID do usuário autenticado; adapte
 *   a implementação ao mecanismo de autenticação do projeto (JWT, Spring Security, etc.).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(basePackages = "com.portal.conecta.mapa_de_sala.module.seat_map.domain.port")
public class JpaConfig {

    /**
     * Fornece o UUID do usuário autenticado para os campos @CreatedBy / @LastModifiedBy.
     *
     * TODO: substituir pela leitura real do SecurityContext quando a autenticação
     *       estiver integrada. Ex.:
     *       <pre>
     *           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     *           return Optional.ofNullable(UUID.fromString(auth.getName()));
     *       </pre>
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // Placeholder — retorna UUID fixo até integração com Spring Security
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}