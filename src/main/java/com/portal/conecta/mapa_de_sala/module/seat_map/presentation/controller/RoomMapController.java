package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ListRoomMapsUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/mapas")
@Tag(name = "Mapas de Sala", description = "Consulta de mapas de sala")
public class RoomMapController {

    private final ListRoomMapsUseCase listRoomMapsUseCase;
    private final RequestContextProvider requestContextProvider;

    public RoomMapController(ListRoomMapsUseCase listRoomMapsUseCase, RequestContextProvider requestContextProvider) {
        this.listRoomMapsUseCase = listRoomMapsUseCase;
        this.requestContextProvider = requestContextProvider;
    }

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
}
