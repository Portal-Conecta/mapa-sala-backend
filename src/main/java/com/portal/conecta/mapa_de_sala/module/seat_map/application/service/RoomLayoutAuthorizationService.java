package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RoomLayoutAuthorizationService {

    /**
     * Autoriza a leitura do layout físico (grid e posições) de uma sala.
     *
     * <p>O layout não é dado sensível por turma — não expõe alocações nem alunos —,
     * então a checagem exige apenas que o usuário tenha algum vínculo de turma real
     * (extraído do próprio token, já validado pelo Hub), sem depender de já existir
     * um {@code RoomMap} salvo para essa sala. Exigir um RoomMap pré-existente
     * impediria a primeira criação de mapa para qualquer sala nova.</p>
     */
    public void checkReadAccess(RequestContext user, UUID roomId) {
        if (user == null) {
            throw new AccessDeniedException("Acesso negado à sala solicitada");
        }
        if (isGlobalProfile(user.userType())) {
            return;
        }

        List<UUID> classIds = user.classes().stream()
                .map(ContextClass::classId)
                .filter(Objects::nonNull)
                .toList();

        if (classIds.isEmpty()) {
            throw new AccessDeniedException("Acesso negado à sala solicitada");
        }
    }

    /**
     * Restringe a criação de vínculo sala-layout aos perfis globais que também
     * criam salas no Hub (ADMIN, SENAI, WEG).
     */
    public void checkWriteAccess(RequestContext user) {
        if (user == null || !isGlobalProfile(user.userType())) {
            throw new AccessDeniedException("Perfil sem permissão para vincular layout à sala.");
        }
    }

    private boolean isGlobalProfile(TypeUser type) {
        return type == TypeUser.SENAI || type == TypeUser.WEG || type == TypeUser.ADMIN;
    }
}