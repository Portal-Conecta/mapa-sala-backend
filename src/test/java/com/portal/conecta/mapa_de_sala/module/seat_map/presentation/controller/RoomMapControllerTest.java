package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapsUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;
import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;
import com.portal.conecta.mapa_de_sala.shared.security.exception.SecurityErrorResponseWriter;
import com.portal.conecta.mapa_de_sala.shared.security.token.JwtExtractToken;

@WebMvcTest(RoomMapController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalHandlerException.class
})
class RoomMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListRoomMapsUseCase listRoomMapsUseCase;

    @MockitoBean
    private RequestContextProvider requestContextProvider;

    @MockitoBean
    private ArchiveRoomMapUseCase archiveRoomMapUseCase;

    @MockitoBean
    private JwtExtractToken jwtExtractToken;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void list_shouldReturn200ForAprendiz() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.STUDENT, List.of()));
        when(listRoomMapsUseCase.execute(any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/mapas"))
                .andExpect(status().isOk());
    }

    @Test
    void list_shouldReturn200ForPerfilSenai() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.SENAI, List.of()));
        when(listRoomMapsUseCase.execute(any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/mapas"))
                .andExpect(status().isOk());
    }

    @Test
    void list_shouldReturn200ForDocente() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
        when(listRoomMapsUseCase.execute(any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/api/mapas"))
                .andExpect(status().isOk());
    }

    @Test
    void list_shouldReturn401WhenNotAuthenticated() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenThrow(new UnauthorizedUserException("Authentication is required."));

        mockMvc.perform(get("/api/mapas"))
                .andExpect(status().isUnauthorized());
    }

    private Page<RoomMapSummaryResponse> emptyPage() {
        return new PageImpl<>(List.of());
    }

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



