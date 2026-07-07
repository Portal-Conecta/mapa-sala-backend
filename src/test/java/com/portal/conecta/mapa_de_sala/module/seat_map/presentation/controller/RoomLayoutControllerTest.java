package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomLayoutCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.CreateRoomLayoutUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionItemResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomLayoutResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomLayoutMapper;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;
import com.portal.conecta.mapa_de_sala.shared.security.exception.SecurityErrorResponseWriter;
import com.portal.conecta.mapa_de_sala.shared.security.token.JwtExtractToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomLayoutController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalHandlerException.class
})
class RoomLayoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase;

    @MockitoBean
    private RoomLayoutAuthorizationService roomLayoutAuthorizationService;

    @MockitoBean
    private CreateRoomLayoutUseCase createRoomLayoutUseCase;

    @MockitoBean
    private RoomLayoutMapper roomLayoutMapper;

    @MockitoBean
    private JwtExtractToken jwtExtractToken;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    private final UUID roomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID templateId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // ---------- GET /api/layouts/salas/{salaId} ----------

    @Test
    void getBySalaId_shouldReturn200WhenUserHasAccessAndLayoutExists() throws Exception {
        var response = new LayoutTemplateWithPositionsResponse(
                templateId, 10, 10,
                List.of(new LayoutPositionItemResponse(0, 1, LayoutPositionType.STUDENT))
        );

        when(getRoomLayoutByRoomIdUseCase.execute(roomId)).thenReturn(response);

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.layoutTemplateId").value(templateId.toString()))
                .andExpect(jsonPath("$.dimensionX").value(10))
                .andExpect(jsonPath("$.dimensionY").value(10))
                .andExpect(jsonPath("$.positions[0].positionX").value(0))
                .andExpect(jsonPath("$.positions[0].positionY").value(1))
                .andExpect(jsonPath("$.positions[0].type").value("STUDENT"));

        verify(getRoomLayoutByRoomIdUseCase).execute(roomId);
    }

    @Test
    void getBySalaId_shouldReturn200ForGlobalProfileWithoutClassLink() throws Exception {
        var response = new LayoutTemplateWithPositionsResponse(templateId, 5, 5, List.of());
        when(getRoomLayoutByRoomIdUseCase.execute(roomId)).thenReturn(response);

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isOk());
    }

    @Test
    void getBySalaId_shouldReturn404WhenRoomDoesNotExistInHub() throws Exception {
        when(getRoomLayoutByRoomIdUseCase.execute(roomId))
                .thenThrow(new ResourceNotFoundException("Sala", roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBySalaId_shouldReturn404WhenRoomHasNoLayoutAssigned() throws Exception {
        when(getRoomLayoutByRoomIdUseCase.execute(roomId))
                .thenThrow(new ResourceNotFoundException("Layout da sala", roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBySalaId_shouldReturn403WhenAprendizIsNotLinkedToRoom() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado à sala solicitada"))
                .when(roomLayoutAuthorizationService)
                .checkReadAccess(nullable(RequestContext.class), eq(roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBySalaId_shouldReturn403WhenDocenteIsNotLinkedToRoom() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado à sala solicitada"))
                .when(roomLayoutAuthorizationService)
                .checkReadAccess(nullable(RequestContext.class), eq(roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBySalaId_shouldReturn403WhenNotAuthenticated() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado à sala solicitada"))
                .when(roomLayoutAuthorizationService)
                .checkReadAccess(nullable(RequestContext.class), eq(roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /api/layouts/salas ----------

    @Test
    void create_shouldReturn201WhenPayloadIsValidAndUserIsAuthorized() throws Exception {
        RoomLayout savedLayout = mock(RoomLayout.class);
        when(savedLayout.getRoomId()).thenReturn(roomId);

        RoomLayoutResponse response = new RoomLayoutResponse(UUID.randomUUID(), roomId, templateId, Instant.now());

        when(createRoomLayoutUseCase.execute(any(CreateRoomLayoutCommand.class))).thenReturn(savedLayout);
        when(roomLayoutMapper.toResponse(savedLayout)).thenReturn(response);

        String payload = """
                {"roomId": "%s", "layoutTemplateId": "%s"}
                """.formatted(roomId, templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.layoutTemplateId").value(templateId.toString()));

        verify(roomLayoutAuthorizationService).checkWriteAccess(nullable(RequestContext.class));
        verify(createRoomLayoutUseCase).execute(any(CreateRoomLayoutCommand.class));
    }

    @Test
    void create_shouldReturn400WhenRoomIdIsMissing() throws Exception {
        String payload = """
                {"layoutTemplateId": "%s"}
                """.formatted(templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenLayoutTemplateIdIsMissing() throws Exception {
        String payload = """
                {"roomId": "%s"}
                """.formatted(roomId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn403WhenProfileIsNotAllowedToWrite() throws Exception {
        doThrow(new AccessDeniedException("Perfil sem permissão para vincular layout à sala."))
                .when(roomLayoutAuthorizationService)
                .checkWriteAccess(nullable(RequestContext.class));

        String payload = """
                {"roomId": "%s", "layoutTemplateId": "%s"}
                """.formatted(roomId, templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        verifyNoInteractions(createRoomLayoutUseCase);
    }

    @Test
    void create_shouldReturn403WhenNotAuthenticated() throws Exception {
        doThrow(new AccessDeniedException("Perfil sem permissão para vincular layout à sala."))
                .when(roomLayoutAuthorizationService)
                .checkWriteAccess(nullable(RequestContext.class));

        String payload = """
                {"roomId": "%s", "layoutTemplateId": "%s"}
                """.formatted(roomId, templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn404WhenRoomOrTemplateNotFound() throws Exception {
        when(createRoomLayoutUseCase.execute(any(CreateRoomLayoutCommand.class)))
                .thenThrow(new ResourceNotFoundException("Sala", roomId));

        String payload = """
                {"roomId": "%s", "layoutTemplateId": "%s"}
                """.formatted(roomId, templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409WhenRoomAlreadyLinked() throws Exception {
        when(createRoomLayoutUseCase.execute(any(CreateRoomLayoutCommand.class)))
                .thenThrow(new ConflictException("Sala já possui layout vinculado."));

        String payload = """
                {"roomId": "%s", "layoutTemplateId": "%s"}
                """.formatted(roomId, templateId);

        mockMvc.perform(post("/api/layouts/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }
}