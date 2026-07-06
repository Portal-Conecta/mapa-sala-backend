package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/layouts/salas")
@RequiredArgsConstructor
@Tag(name = "Layouts de Sala", description = "Consulta read-only do layout físico de salas")
public class RoomLayoutController {

    private final GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase;
    private final RoomLayoutAuthorizationService roomLayoutAuthorizationService;


    @GetMapping("/{salaId}")
    @Operation(summary = "Obter layout físico da sala",
            description = "Retorna dimensões do grid e posições (read-only).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Layout encontrado")
    @ApiResponse(responseCode = "403", description = "Usuário sem vínculo com a turma da sala")
    @ApiResponse(responseCode = "404", description = "Sala ou layout não encontrado")
    public ResponseEntity<LayoutTemplateWithPositionsResponse> getBySalaId(
            @AuthenticationPrincipal RequestContext user,
            @PathVariable UUID salaId
    ) {
        roomLayoutAuthorizationService.checkReadAccess(user, salaId);
        return ResponseEntity.ok(getRoomLayoutByRoomIdUseCase.execute(salaId));
    }
}