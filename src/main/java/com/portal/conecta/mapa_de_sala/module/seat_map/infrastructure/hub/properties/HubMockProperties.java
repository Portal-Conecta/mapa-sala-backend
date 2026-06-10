package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "hub.mock")
public record HubMockProperties(
        List<String> classIds,
        List<String> userIds,
        List<String> roomIds,
        List<String> userRoomLinks,
        Map<String, List<MockStudent>> studentsByClass
) {

    public HubMockProperties {
        classIds = classIds == null ? List.of() : List.copyOf(classIds);
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        roomIds = roomIds == null ? List.of() : List.copyOf(roomIds);
        userRoomLinks = userRoomLinks == null ? List.of() : List.copyOf(userRoomLinks);
        studentsByClass = studentsByClass == null ? Map.of() : Map.copyOf(studentsByClass);
    }

    public record MockStudent(
            String id,
            String name
    ) {
    }
}
