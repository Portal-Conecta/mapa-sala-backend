package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubMockProperties;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockHubPermissionAdapter implements HubPermissionPort {

    private final List<UUID> defaultClassIds;
    private final Map<UUID, List<UUID>> accessibleClassIdsByUser;

    public MockHubPermissionAdapter(HubMockProperties properties) {
        this.defaultClassIds = properties.classIds().stream()
                .map(UUID::fromString)
                .toList();
        this.accessibleClassIdsByUser = properties.accessibleClassIdsByUser().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> entry.getValue().stream()
                                .map(UUID::fromString)
                                .toList()
                ));
    }

    @Override
    public List<UUID> getAccessibleClassIds(UUID userId, TypeUser userType) {
        List<UUID> classIds = accessibleClassIdsByUser.get(userId);

        if (classIds != null) {
            return classIds;
        }

        if (accessibleClassIdsByUser.isEmpty()) {
            return defaultClassIds;
        }

        return List.of();
    }
}
