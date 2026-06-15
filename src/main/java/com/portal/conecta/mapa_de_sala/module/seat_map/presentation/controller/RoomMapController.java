package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomMapViewUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapHistoryUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapsUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas de Sala", description = "Consulta de mapas de sala")
public class RoomMapController {

    private final ListRoomMapsUseCase listRoomMapsUseCase;
    private final ListRoomMapHistoryUseCase listRoomMapHistoryUseCase;
    private final RequestContextProvider requestContextProvider;
    private final ArchiveRoomMapUseCase archiveRoomMapUseCase;
    private final GetRoomMapViewUseCase getRoomMapViewUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Listar mapas de sala",
            description = "Lista mapas de sala paginados, com escopo determinado pelo perfil do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Página de mapas de sala")
    @ApiResponse(responseCode = "403", description = "Perfil sem acesso")
    public ResponseEntity<Page<RoomMapSummaryResponse>> list(
            @Parameter(description = "Identificador da turma (não altera o escopo, que é definido pelo perfil)")
            @RequestParam(required = false) UUID turmaId,
            @Parameter(description = "Identificador da sala")
            @RequestParam(required = false) UUID salaId,
            @Parameter(description = "Número da página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int size
    ) {
        RequestContext user = requestContextProvider.getRequestContext();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(listRoomMapsUseCase.execute(user.userId(), user.userType(), salaId, pageable));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Consultar histórico do mapa de sala",
            description = "Retorna o histórico de alterações do mapa em ordem decrescente por data.")
    @ApiResponse(responseCode = "200", description = "Página do histórico do mapa")
    @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao mapa")
    @ApiResponse(responseCode = "404", description = "Mapa de sala não encontrado")
    public ResponseEntity<Page<RoomMapHistoryResponse>> listHistory(
            @Parameter(description = "Identificador do mapa de sala")
            @PathVariable UUID id,
            @Parameter(description = "Número da página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int size
    ) {
        RequestContext user = requestContextProvider.getRequestContext();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(listRoomMapHistoryUseCase.execute(user.userId(), user.userType(), id, pageable));
    }

    @GetMapping("/salas/{salaId}/turmas/{turmaId}")
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Visualizar mapa por sala e turma",
            description = "Retorna o mapa salvo da turma na sala ou uma sugestão alfabética caso ainda não exista (RN-MS05).")
    @ApiResponse(responseCode = "200", description = "Mapa salvo ou sugestão alfabética")
    @ApiResponse(responseCode = "403", description = "Usuário sem acesso à turma")
    @ApiResponse(responseCode = "404", description = "Sala ou turma não encontrada")
    public ResponseEntity<RoomMapViewResponse> getView(
            @Parameter(description = "Identificador da sala")
            @PathVariable UUID salaId,
            @Parameter(description = "Identificador da turma")
            @PathVariable UUID turmaId
    ) {
        return ResponseEntity.ok(getRoomMapViewUseCase.execute(salaId, turmaId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Arquivar mapa de sala", 
               description = "Arquiva um mapa de sala existente.")
    @ApiResponse(responseCode = "204", description = "Mapa de sala arquivado com sucesso")
    @ApiResponse(responseCode = "404", description = "Mapa de sala não encontrado")
    public ResponseEntity<Void> archive(
        @PathVariable UUID id
    ) {
        archiveRoomMapUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}


