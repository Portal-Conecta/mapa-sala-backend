package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "false")
public class JwtHubPermissionAdapter implements HubPermissionPort {

    private final RequestContextProvider contextProvider;

    public JwtHubPermissionAdapter(RequestContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public List<UUID> getAccessibleClassIds(UUID userId, TypeUser userType) {
        return contextProvider.getRequestContext().classes().stream()
                .filter(c -> TypeUser.TEACHER.name().equals(c.role()))
                .map(c -> c.classId())
                .toList();
    }
}
