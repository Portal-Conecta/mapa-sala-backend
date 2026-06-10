package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubUser;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubUserPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubMockProperties;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockHubUserAdapter implements HubUserPort {

    private final Set<UUID> userIds;
    private final Map<UUID, String> userNames;

    public MockHubUserAdapter(HubMockProperties properties) {
        this.userIds = properties.userIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());

        this.userNames = properties.studentsByClass().values().stream()
                .flatMap(students -> students.stream())
                .collect(Collectors.toMap(
                        student -> UUID.fromString(student.id()),
                        HubMockProperties.MockStudent::name,
                        (left, right) -> left
                ));
    }

    @Override
    public boolean existsById(UUID userId) {
        return userIds.isEmpty() || userIds.contains(userId) || userNames.containsKey(userId);
    }

    @Override
    public Optional<HubUser> findById(UUID userId) {
        if (!existsById(userId)) {
            return Optional.empty();
        }

        return Optional.of(new HubUser(userId, userNames.getOrDefault(userId, "Usuário mock " + userId)));
    }
}
