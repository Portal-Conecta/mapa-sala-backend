package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.net.URI;
import java.util.UUID;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomLayoutCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomLayoutAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.CreateRoomLayoutUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.GetRoomLayoutByRoomIdUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomLayoutRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomLayoutResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomLayoutMapper;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.exception.ApiReponseException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para consulta e vinculação de layout físico de salas.
 *
 * <p>A consulta (GET) é read-only e a criação (POST) vincula uma sala do Hub
 * a um layout_template já cadastrado. Ambas as operações delegam a autorização
 * ao {@link RoomLayoutAuthorizationService}, mantendo a checagem de perfil fora
 * do use case.</p>
 */
@RestController
@RequestMapping("/api/layouts/salas")
@Tag(name = "Layouts de Sala", description = "Consulta e vinculação do layout físico de salas")
public class RoomLayoutController {

    private final GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase;
    private final RoomLayoutAuthorizationService roomLayoutAuthorizationService;
    private final CreateRoomLayoutUseCase createRoomLayoutUseCase;
    private final RoomLayoutMapper roomLayoutMapper;

    public RoomLayoutController(
            GetRoomLayoutByRoomIdUseCase getRoomLayoutByRoomIdUseCase,
            RoomLayoutAuthorizationService roomLayoutAuthorizationService,
            CreateRoomLayoutUseCase createRoomLayoutUseCase,
            RoomLayoutMapper roomLayoutMapper
    ) {
        this.getRoomLayoutByRoomIdUseCase = getRoomLayoutByRoomIdUseCase;
        this.roomLayoutAuthorizationService = roomLayoutAuthorizationService;
        this.createRoomLayoutUseCase = createRoomLayoutUseCase;
        this.roomLayoutMapper = roomLayoutMapper;
    }

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

    @PostMapping
    @Operation(summary = "Vincular sala a um layout",
            description = "Vincula uma sala real do Hub a um layout_template já existente. Restrito a ADMIN, SENAI e WEG.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Identificador da sala e do template a vincular.",
                    required = true
            ))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Layout vinculado com sucesso",
                    content = @Content(schema = @Schema(implementation = RoomLayoutResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Requisição sem autenticação",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Perfil sem permissão para vincular layout à sala",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sala ou template não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Sala já possui layout vinculado",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            )
    })
    public ResponseEntity<RoomLayoutResponse> create(
            @AuthenticationPrincipal RequestContext user,
            @Valid @RequestBody CreateRoomLayoutRequest request
    ) {
        roomLayoutAuthorizationService.checkWriteAccess(user);

        CreateRoomLayoutCommand command = new CreateRoomLayoutCommand(request.roomId(), request.layoutTemplateId());

        RoomLayout roomLayout = createRoomLayoutUseCase.execute(command);
        RoomLayoutResponse response = roomLayoutMapper.toResponse(roomLayout);

        return ResponseEntity.created(URI.create("/api/layouts/salas/" + roomLayout.getRoomId())).body(response);
    }
}