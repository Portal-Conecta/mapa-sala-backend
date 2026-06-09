package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionItemResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;
import com.portal.conecta.mapa_de_sala.shared.security.exception.SecurityErrorResponseWriter;
import com.portal.conecta.mapa_de_sala.shared.security.token.JwtExtractToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private JwtExtractToken jwtExtractToken;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    private final UUID roomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID templateId = UUID.fromString("22222222-2222-2222-2222-222222222222");

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
}
