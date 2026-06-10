package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;
import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;
import com.portal.conecta.mapa_de_sala.shared.security.exception.SecurityErrorResponseWriter;
import com.portal.conecta.mapa_de_sala.shared.security.token.JwtExtractToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomMapController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalHandlerException.class)
class RoomMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArchiveRoomMapUseCase archiveRoomMapUseCase;

    @MockitoBean
    private JwtExtractToken jwtExtractToken;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    private final UUID mapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void archive_shouldReturn204WhenSuccessful() throws Exception {
        doNothing().when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}", mapId))
                .andExpect(status().isNoContent());

        verify(archiveRoomMapUseCase).execute(mapId);
    }

    @Test
    void archive_shouldReturn404WhenMapNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Mapa de sala", mapId))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}", mapId))
                .andExpect(status().isNotFound());
    }

    @Test
    void archive_shouldReturn401WhenUserIsNotAuthorized() throws Exception {
        doThrow(new UnauthorizedUserException("Usuário não autorizado para arquivar mapa de sala"))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}", mapId))
                .andExpect(status().isUnauthorized());
    }
}
