package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.ArchiveRoomMapUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas de Sala", description = "Consulta read-only do mapa de sala")
public class RoomMapController {

    private final ArchiveRoomMapUseCase archiveRoomMapUseCase;

    @PatchMapping("/{id}")
    @Operation(summary = "Arquivar mapa de sala", description = "Arquiva um mapa de sala existente.")
    @ApiResponse(responseCode = "204", description = "Mapa de sala arquivado com sucesso")
    @ApiResponse(responseCode = "404", description = "Mapa de sala não encontrado")
    public ResponseEntity<Void> archive(
        @PathVariable UUID id
    ) {
        archiveRoomMapUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
