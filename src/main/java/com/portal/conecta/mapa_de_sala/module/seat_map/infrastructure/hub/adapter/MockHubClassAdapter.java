package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubMockProperties;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockHubClassAdapter implements HubClassPort {

    private final Set<UUID> classIds;
    private final HubMockProperties properties;

    public MockHubClassAdapter(HubMockProperties properties) {
        this.properties = properties;
        this.classIds = properties.classIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean existsById(UUID classId) {
        return classIds.isEmpty() || classIds.contains(classId);
    }

    @Override
    public List<HubStudent> findStudentsByClassId(UUID classId) {
        List<HubMockProperties.MockStudent> students = properties.studentsByClass().get(classId.toString());

        if (students == null) {
            return List.of();
        }

        return students.stream()
                .map(student -> new HubStudent(UUID.fromString(student.id()), student.name()))
                .sorted(Comparator.comparing(HubStudent::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public boolean belongsToClass(UUID userId, UUID classId) {
        List<HubMockProperties.MockStudent> students = properties.studentsByClass().get(classId.toString());

        if (students == null) {
            return false;
        }

        return students.stream()
                .anyMatch(student -> student.id().equals(userId.toString()));
    }

    @Override
    public List<UUID> getClassIdsForUser(UUID userId) {
        return properties.studentsByClass().entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(student -> student.id().equals(userId.toString())))
                .map(entry -> UUID.fromString(entry.getKey()))
                .toList();
    }
}