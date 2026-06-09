package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionItemResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.shared.config.SecurityConfig;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.mapa_de_sala.shared.security.JwtAuthenticationFilter;
import com.portal.conecta.mapa_de_sala.shared.security.SecurityContextAccessor;
import com.portal.conecta.mapa_de_sala.shared.security.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.portal.conecta.mapa_de_sala.shared.security.JwtAuthenticationFilter.USER_ID_HEADER;
import static com.portal.conecta.mapa_de_sala.shared.security.JwtAuthenticationFilter.USER_PROFILE_HEADER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomLayoutController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        SecurityContextAccessor.class,
        GlobalExceptionHandler.class,
        RoomLayoutAuthorizationService.class
})
class RoomLayoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase;

    @MockitoBean
    private HubRoomPort hubRoomPort;

    private final UUID roomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID templateId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void getBySalaId_shouldReturn200WhenUserHasAccessAndLayoutExists() throws Exception {
        var response = new LayoutTemplateWithPositionsResponse(
                templateId,
                10,
                10,
                List.of(new LayoutPositionItemResponse(0, 1, LayoutPositionType.STUDENT))
        );

        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(true);
        when(getRoomLayoutByRoomIdUseCase.execute(roomId)).thenReturn(response);

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.APRENDIZ))
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

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.ADMINISTRADOR))
                .andExpect(status().isOk());
    }

    @Test
    void getBySalaId_shouldReturn404WhenRoomDoesNotExistInHub() throws Exception {
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(true);
        when(getRoomLayoutByRoomIdUseCase.execute(roomId))
                .thenThrow(new ResourceNotFoundException("Sala", roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.DOCENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBySalaId_shouldReturn404WhenRoomHasNoLayoutAssigned() throws Exception {
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(true);
        when(getRoomLayoutByRoomIdUseCase.execute(roomId))
                .thenThrow(new ResourceNotFoundException("Layout da sala", roomId));

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.DOCENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBySalaId_shouldReturn403WhenAprendizIsNotLinkedToRoom() throws Exception {
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(false);

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.APRENDIZ))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBySalaId_shouldReturn403WhenDocenteIsNotLinkedToRoom() throws Exception {
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(false);

        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId)
                        .header(USER_ID_HEADER, userId)
                        .header(USER_PROFILE_HEADER, UserProfile.DOCENTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBySalaId_shouldReturn403WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/layouts/salas/{salaId}", roomId))
                .andExpect(status().isForbidden());
    }
}
