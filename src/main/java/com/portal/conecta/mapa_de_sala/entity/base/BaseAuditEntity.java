package com.portal.conecta.mapa_de_sala.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Superclasse de auditoria herdada por todas as entidades do módulo Mapa de Sala.
 *
 * Campos populados automaticamente pelo Spring Data JPA Auditing (RN-MS11):
 *  - createdAt  / created_at  : data/hora de criação
 *  - updatedAt  / updated_at  : data/hora da última atualização
 *  - removedAt  / removed_at  : soft delete (preenchido manualmente no service)
 *  - createdBy  / created_by  : UUID do usuário que criou (via AuditorAware)
 *  - updatedBy  / updated_by  : UUID do usuário que atualizou por último
 *
 * Convenção de nomenclatura:
 *  - Atributos Java : camelCase
 *  - Colunas SQL    : snake_case em inglês, explicitado em @Column(name = "...")
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete: preenchido manualmente pelo service ao invés de deletar o registro.
     * Null indica registro ativo.
     */
    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    // ── Getters e Setters ────────────────────────────────────────────────

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Utilitário: indica se o registro está ativo (não soft-deletado).
     */
    public boolean isActive() {
        return removedAt == null;
    }
}