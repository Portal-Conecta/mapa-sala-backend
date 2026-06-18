package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.CreateRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomMapViewUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapHistoryUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapsUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.RoomMapAlreadyArchivedException;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapGridResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapMapper;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import com.portal.conecta.mapa_de_sala.shared.exception.GlobalHandlerException;
import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;
import com.portal.conecta.mapa_de_sala.shared.security.exception.SecurityErrorResponseWriter;
import com.portal.conecta.mapa_de_sala.shared.security.token.JwtExtractToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomMapController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalHandlerException.class
})
class RoomMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArchiveRoomMapUseCase archiveRoomMapUseCase;

    @MockitoBean
    private CreateRoomMapUseCase createRoomMapUseCase;

    @MockitoBean
    private ListRoomMapsUseCase listRoomMapsUseCase;

    @MockitoBean
    private ListRoomMapHistoryUseCase listRoomMapHistoryUseCase;

    @MockitoBean
    private GetRoomMapViewUseCase getRoomMapViewUseCase;

    @MockitoBean
    private RequestContextProvider requestContextProvider;

    @MockitoBean
    private JwtExtractToken jwtExtractToken;

    @MockitoBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @MockitoBean
    private RoomMapMapper roomMapMapper;

    private final UUID mapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID roomMapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    // --- archive ---

    @Test
    void archive_shouldReturn204WhenSuccessful() throws Exception {
        doNothing().when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}/arquivar", mapId))
                .andExpect(status().isNoContent());

        verify(archiveRoomMapUseCase).execute(mapId);
    }

    @Test
    void archive_shouldReturn400WhenMapIsAlreadyArchived() throws Exception {
        doThrow(new RoomMapAlreadyArchivedException(mapId))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}/arquivar", mapId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void archive_shouldReturn401WhenUserIsNotAuthorized() throws Exception {
        doThrow(new UnauthorizedUserException("Usuário não autorizado para arquivar mapa de sala"))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}/arquivar", mapId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void archive_shouldReturn403WhenUserHasNoPermission() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado"))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}/arquivar", mapId))
                .andExpect(status().isForbidden());
    }

    @Test
    void archive_shouldReturn404WhenMapNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Mapa de sala", mapId))
                .when(archiveRoomMapUseCase).execute(mapId);

        mockMvc.perform(patch("/api/mapas/{id}/arquivar", mapId))
                .andExpect(status().isNotFound());
    }

    // --- listHistory ---

    @Test
    void listHistory_shouldReturn200ForAprendiz() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.STUDENT, List.of()));
        when(listRoomMapHistoryUseCase.execute(eq(userId), eq(TypeUser.STUDENT), eq(roomMapId), any(Pageable.class)))
                .thenReturn(emptyHistoryPage());

        mockMvc.perform(get("/api/mapas/{id}/history", roomMapId))
                .andExpect(status().isOk());
    }

    @Test
    void listHistory_shouldReturn200ForPerfilSenai() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.SENAI, List.of()));
        when(listRoomMapHistoryUseCase.execute(eq(userId), eq(TypeUser.SENAI), eq(roomMapId), any(Pageable.class)))
                .thenReturn(emptyHistoryPage());

        mockMvc.perform(get("/api/mapas/{id}/history", roomMapId))
                .andExpect(status().isOk());
    }

    @Test
    void listHistory_shouldReturn200ForDocente() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
        when(listRoomMapHistoryUseCase.execute(eq(userId), eq(TypeUser.TEACHER), eq(roomMapId), any(Pageable.class)))
                .thenReturn(emptyHistoryPage());

        mockMvc.perform(get("/api/mapas/{id}/history", roomMapId))
                .andExpect(status().isOk());
    }

    @Test
    void listHistory_shouldReturn403WhenUserHasNoAccess() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.STUDENT, List.of()));
        doThrow(new AccessDeniedException("Acesso negado ao mapa solicitado"))
                .when(listRoomMapHistoryUseCase)
                .execute(eq(userId), eq(TypeUser.STUDENT), eq(roomMapId), any(Pageable.class));

        mockMvc.perform(get("/api/mapas/{id}/history", roomMapId))
                .andExpect(status().isForbidden());
    }

    @Test
    void listHistory_shouldReturn404WhenMapNotFound() throws Exception {
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.SENAI, List.of()));
        doThrow(new ResourceNotFoundException("Mapa de sala", roomMapId))
                .when(listRoomMapHistoryUseCase)
                .execute(eq(userId), eq(TypeUser.SENAI), eq(roomMapId), any(Pageable.class));

        mockMvc.perform(get("/api/mapas/{id}/history", roomMapId))
                .andExpect(status().isNotFound());
    }

    // --- list ---

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

    // --- getView ---

    @Test
    void getView_shouldReturn200WithSavedMapForAprendiz() throws Exception {
        UUID salaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.STUDENT, List.of()));
        when(getRoomMapViewUseCase.execute(salaId, turmaId))
                .thenReturn(savedMapView());

        mockMvc.perform(get("/api/mapas/salas/{salaId}/turmas/{turmaId}", salaId, turmaId))
                .andExpect(status().isOk());
    }

    @Test
    void getView_shouldReturn200WithSuggestionWhenNoSavedMap() throws Exception {
        UUID salaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
        when(getRoomMapViewUseCase.execute(salaId, turmaId))
                .thenReturn(suggestedMapView());

        mockMvc.perform(get("/api/mapas/salas/{salaId}/turmas/{turmaId}", salaId, turmaId))
                .andExpect(status().isOk());
    }

    @Test
    void getView_shouldReturn403WhenUserHasNoAccessToClass() throws Exception {
        UUID salaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.STUDENT, List.of()));
        doThrow(new AccessDeniedException("Acesso negado ao mapa solicitado"))
                .when(getRoomMapViewUseCase).execute(salaId, turmaId);

        mockMvc.perform(get("/api/mapas/salas/{salaId}/turmas/{turmaId}", salaId, turmaId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getView_shouldReturn404WhenRoomOrClassNotFound() throws Exception {
        UUID salaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.SENAI, List.of()));
        doThrow(new ResourceNotFoundException("Sala", salaId))
                .when(getRoomMapViewUseCase).execute(salaId, turmaId);

        mockMvc.perform(get("/api/mapas/salas/{salaId}/turmas/{turmaId}", salaId, turmaId))
                .andExpect(status().isNotFound());
    }

    // --- helpers ---

    private RoomMapViewResponse savedMapView() {
        RoomMapResponse map = new RoomMapResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        RoomMapGridResponse grid = new RoomMapGridResponse(1, 1, 0, List.of());
        return new RoomMapViewResponse(false, map, grid, List.of(), List.of());
    }

    private RoomMapViewResponse suggestedMapView() {
        RoomMapGridResponse grid = new RoomMapGridResponse(1, 1, 0, List.of());
        return new RoomMapViewResponse(true, null, grid, List.of(), List.of());
    }

    private Page<RoomMapSummaryResponse> emptyPage() {
        return new PageImpl<>(List.of());
    }

    private Page<RoomMapHistoryResponse> emptyHistoryPage() {
        return new PageImpl<>(List.of());
    }
}