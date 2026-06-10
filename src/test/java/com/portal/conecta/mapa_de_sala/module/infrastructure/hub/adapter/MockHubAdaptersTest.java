package com.portal.conecta.mapa_de_sala.module.infrastructure.hub.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter.MockHubClassAdapter;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter.MockHubRoomAdapter;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter.MockHubUserAdapter;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubMockProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockHubAdaptersTest {

    private static final UUID ROOM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLASS_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID STUDENT_A = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID STUDENT_B = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    private HubMockProperties properties;
    private MockHubRoomAdapter roomAdapter;
    private MockHubClassAdapter classAdapter;
    private MockHubUserAdapter userAdapter;

    @BeforeEach
    void setUp() {
        properties = new HubMockProperties(
                List.of(CLASS_ID.toString()),
                List.of(USER_ID.toString()),
                List.of(ROOM_ID.toString()),
                List.of(USER_ID + ":" + ROOM_ID),
                Map.of(
                        CLASS_ID.toString(),
                        List.of(
                                new HubMockProperties.MockStudent(STUDENT_B.toString(), "Bruno Costa"),
                                new HubMockProperties.MockStudent(STUDENT_A.toString(), "Ana Silva")
                        )
                )
        );

        roomAdapter = new MockHubRoomAdapter(properties);
        classAdapter = new MockHubClassAdapter(properties);
        userAdapter = new MockHubUserAdapter(properties);
    }

    @Test
    void roomAdapter_shouldValidateKnownRoomAndUserLink() {
        assertThat(roomAdapter.existsById(ROOM_ID)).isTrue();
        assertThat(roomAdapter.existsById(UUID.randomUUID())).isFalse();
        assertThat(roomAdapter.isUserLinkedToRoom(USER_ID, ROOM_ID)).isTrue();
        assertThat(roomAdapter.isUserLinkedToRoom(USER_ID, UUID.randomUUID())).isFalse();
    }

    @Test
    void classAdapter_shouldReturnStudentsOrderedAlphabetically() {
        assertThat(classAdapter.existsById(CLASS_ID)).isTrue();

        var students = classAdapter.findStudentsByClassId(CLASS_ID);

        assertThat(students).hasSize(2);
        assertThat(students.get(0).name()).isEqualTo("Ana Silva");
        assertThat(students.get(1).name()).isEqualTo("Bruno Costa");
    }

    @Test
    void userAdapter_shouldResolveKnownUser() {
        assertThat(userAdapter.existsById(USER_ID)).isTrue();

        var user = userAdapter.findById(USER_ID);

        assertThat(user).isPresent();
        assertThat(user.get().id()).isEqualTo(USER_ID);
    }
}
