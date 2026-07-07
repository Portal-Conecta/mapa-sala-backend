package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapHistoryCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapHistoryRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapHistoryMapperTest {

    private final RoomMapHistoryMapper mapper = new RoomMapHistoryMapperImpl();

    @Test
    void toResponse_shouldExtractRoomMapId() {
        var roomMap = new RoomMap();
        ReflectionTestUtils.setField(roomMap, "id", UUID.randomUUID());

        var entity = new RoomMapHistory();

        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "roomMap", roomMap);

        RoomMapHistoryAction acaoDinamica = RoomMapHistoryAction.values()[0];
        ReflectionTestUtils.setField(entity, "action", acaoDinamica);

        RoomMapHistoryResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.roomMapId()).isEqualTo(roomMap.getId());

        assertThat(response.action()).isEqualTo(acaoDinamica.name());
    }

    @Test
    void toCommand_shouldCombineRequestAndUserId() {
        var userId = UUID.randomUUID();
        var request = new CreateRoomMapHistoryRequest(UUID.randomUUID(), "CREATE", "Detalhes");

        CreateRoomMapHistoryCommand command = mapper.toCommand(request, userId);

        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.data()).isEqualTo(request);
    }
}