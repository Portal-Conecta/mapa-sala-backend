package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.CreateRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomMapViewUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapHistoryUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapsUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapInitialAllocationRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapGridResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;

/*
 * NOTA: assim como em RoomLayoutControllerTest, addFilters = false desliga
 * os filtros de segurança reais -- 401/403 aqui testam apenas a tradução de
 * exceções lançadas pelo use case (via GlobalHandlerException), não a
 * autenticação/autorização de fato. O 401 "sem autenticação" (critério de
 * aceite da issue #87) não é exercitável neste nível de teste; deve ser
 * coberto em teste de integração com filtros de segurança habilitados,
 * caso o projeto tenha essa camada.
 */
@WebMvcTest(RoomMapController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalHandlerException.class
})
class RoomMapControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateRoomMapUseCase createRoomMapUseCase;

    @MockitoBean
    private ListRoomMapsUseCase listRoomMapsUseCase;

    @MockitoBean
    private ListRoomMapHistoryUseCase listRoomMapHistoryUseCase;

    @MockitoBean
    private ArchiveRoomMapUseCase archiveRoomMapUseCase;

    @MockitoBean
    private GetRoomMapViewUseCase getRoomMapViewUseCase;

    @MockitoBean
    private RequestContextProvider requestContextProvider;

    private final UUID classId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID roomId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID templateId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private CreateRoomMapRequest validRequestWithoutLocations() {
        return new CreateRoomMapRequest(classId, roomId, templateId, List.of());
    }

    private RoomMapViewResponse savedMapResponse(UUID mapId) {
        RoomMapResponse map = new RoomMapResponse(mapId, classId, roomId, templateId);
        RoomMapGridResponse grid = new RoomMapGridResponse(5, 6, 25, List.of());
        return new RoomMapViewResponse(false, map, grid, List.of(), List.of());
    }

    @Test
    void create_shouldReturn201WhenTeacherLinkedToClassCreatesMapWithoutAllocations() throws Exception {
        UUID mapId = UUID.randomUUID();
        when(createRoomMapUseCase.execute(any())).thenReturn(savedMapResponse(mapId));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.suggested").value(false))
                .andExpect(jsonPath("$.map.id").value(mapId.toString()))
                .andExpect(jsonPath("$.map.classId").value(classId.toString()));

        verify(createRoomMapUseCase).execute(any());
    }

    @Test
    void create_shouldReturn201WhenAllocationIsProvidedViaSeatNumber() throws Exception {
        when(createRoomMapUseCase.execute(any())).thenReturn(savedMapResponse(UUID.randomUUID()));

        CreateRoomMapRequest request = new CreateRoomMapRequest(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationRequest(UUID.randomUUID(), 1, null))
        );

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn201WhenAllocationIsProvidedViaLayoutPositionId() throws Exception {
        when(createRoomMapUseCase.execute(any())).thenReturn(savedMapResponse(UUID.randomUUID()));

        CreateRoomMapRequest request = new CreateRoomMapRequest(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationRequest(UUID.randomUUID(), null, UUID.randomUUID()))
        );

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn400WhenClassIdIsMissing() throws Exception {
        CreateRoomMapRequest request = new CreateRoomMapRequest(null, roomId, templateId, List.of());

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenLocationStudentIdIsMissing() throws Exception {
        CreateRoomMapRequest request = new CreateRoomMapRequest(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationRequest(null, 1, null))
        );

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenUseCaseRejectsAllocation() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new BadRequestException("A posição não pertence ao template"));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn403WhenUserIsNotATeacher() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new AccessDeniedException("Apenas docentes podem criar mapas."));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn403WhenTeacherIsNotLinkedToClass() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new AccessDeniedException("Docente não vinculado à turma."));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn404WhenClassDoesNotExist() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new ResourceNotFoundException("Turma", classId));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn404WhenTemplateDoesNotExist() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new ResourceNotFoundException("Template", templateId));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409WhenActiveMapAlreadyExistsForClassAndRoom() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new ConflictException("Já existe um mapa ativo para esta turma e sala."));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn409WhenDuplicatedStudentIdInAllocations() throws Exception {
        when(createRoomMapUseCase.execute(any()))
                .thenThrow(new ConflictException("O mesmo aprendiz não pode ocupar mais de uma posição"));

        mockMvc.perform(post("/api/mapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithoutLocations())))
                .andExpect(status().isConflict());
    }
}