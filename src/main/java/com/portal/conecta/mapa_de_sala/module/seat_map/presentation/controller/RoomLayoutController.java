package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.shared.security.SecurityContextAccessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/layouts/salas")
@Tag(name = "Layouts de Sala", description = "Consulta read-only do layout físico de salas")
public class RoomLayoutController {

    private final GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase;
    private final RoomLayoutAuthorizationService roomLayoutAuthorizationService;
    private final SecurityContextAccessor securityContextAccessor;

    public RoomLayoutController(
            GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase,
            RoomLayoutAuthorizationService roomLayoutAuthorizationService,
            SecurityContextAccessor securityContextAccessor
    ) {
        this.getRoomLayoutByRoomIdUseCase = getRoomLayoutByRoomIdUseCase;
        this.roomLayoutAuthorizationService = roomLayoutAuthorizationService;
        this.securityContextAccessor = securityContextAccessor;
    }

    @GetMapping("/{salaId}")
    @Operation(
            summary = "Obter layout físico da sala",
            description = "Retorna dimensões do grid e posições de carteiras, professor, portas etc. (read-only)."
    )
    @ApiResponse(responseCode = "200", description = "Layout encontrado")
    @ApiResponse(responseCode = "403", description = "Usuário sem vínculo com a turma da sala")
    @ApiResponse(responseCode = "404", description = "Sala ou layout não encontrado")
    public ResponseEntity<LayoutTemplateWithPositionsResponse> getBySalaId(@PathVariable UUID salaId) {
        var user = securityContextAccessor.getCurrentUser();
        roomLayoutAuthorizationService.checkReadAccess(user, salaId);
        return ResponseEntity.ok(getRoomLayoutByRoomIdUseCase.execute(salaId));
    }
}
