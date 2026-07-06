package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapAllocationsCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.PaginationPolicy;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.MoveStudentRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapAllocationsRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapLocationMapper;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapMapper;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.exception.ApiReponseException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final CreateRoomMapUseCase createRoomMapUseCase;
    private final RoomMapMapper roomMapMapper;
    private final RoomMapLocationMapper roomMapLocationMapper;
    private final MoveStudentUseCase moveStudentUseCase;
    private final UpdateRoomMapAllocationsUseCase updateRoomMapAllocationsUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Listar mapas de sala",
            description = "Lista mapas de sala paginados, com escopo determinado pelo perfil do usuário autenticado.",
            security = @SecurityRequirement(name = "bearerAuth"))
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
        PaginationPolicy.validate(page, size);
        RequestContext user = requestContextProvider.getRequestContext();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(listRoomMapsUseCase.execute(user.userId(), user.userType(), salaId, pageable));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Consultar histórico do mapa de sala",
            description = "Retorna o histórico de alterações do mapa em ordem decrescente por data.",
            security = @SecurityRequirement(name = "bearerAuth"))
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
        PaginationPolicy.validate(page, size);
        RequestContext user = requestContextProvider.getRequestContext();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(listRoomMapHistoryUseCase.execute(user.userId(), user.userType(), id, pageable));
    }

    @GetMapping("/salas/{salaId}/turmas/{turmaId}")
    @PreAuthorize("hasAnyRole('STUDENT','REPRESENTATIVE','TEACHER','SENAI','WEG','ADMIN')")
    @Operation(summary = "Visualizar mapa por sala e turma",
            description = "Retorna o mapa salvo da turma na sala ou uma sugestão alfabética caso ainda não exista (RN-MS05).",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Criar mapa de sala",
            description = "Cria o vínculo entre turma e sala com snapshot do layout escolhido.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação do mapa de sala.",
                    required = true
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Mapa criado com sucesso",
                    content = @Content(schema = @Schema(implementation = RoomMapViewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campos obrigatórios ausentes, formato inválido, posição fora "
                            + "do template ou posição não é do tipo STUDENT",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Requisição sem autenticação ou token inválido",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Executor não é docente ou não está vinculado à turma",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Turma, sala ou template não encontrados",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe mapa ativo para a combinação de turma e sala, "
                            + "ou alocações com studentId ou posição duplicados",
                    content = @Content(schema = @Schema(implementation = ApiReponseException.class))
            )
    })
    public ResponseEntity<RoomMapViewResponse> create(@Valid @RequestBody CreateRoomMapRequest request) {

        CreateRoomMapCommand command = roomMapMapper.toCommand(request);

        UUID newMapId = createRoomMapUseCase.execute(command);

        RoomMapViewResponse responseView = getRoomMapViewUseCase.execute(request.roomId(), request.classId());

        return ResponseEntity.created(URI.create("/api/mapas/" + newMapId)).body(responseView);
    }

    @PatchMapping("/{id}/arquivar")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @Operation(summary = "Arquivar mapa de sala", 
               description = "Arquiva um mapa de sala existente.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Mapa de sala arquivado com sucesso")
    @ApiResponse(responseCode = "400", description = "Mapa já está arquivado")
    @ApiResponse(responseCode = "401", description = "Requisição sem autenticação")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão para arquivar mapas")
    @ApiResponse(responseCode = "404", description = "Mapa de sala não encontrado")
    public ResponseEntity<Void> archive(
        @PathVariable UUID id
    ) {
        archiveRoomMapUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/locations/move")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Mover aprendiz no mapa",
            description = "Move um aprendiz para outra posição no mapa. Em caso de conflito, aplica DISPLACE (padrão) ou SWAP.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados de movimentação do aprendiz no mapa.",
                    required = true
            ))
    @ApiResponse(responseCode = "204", description = "Aprendiz movido com sucesso")
    @ApiResponse(responseCode = "400", description = "Posição alvo inválida ou mapa arquivado")
    @ApiResponse(responseCode = "401", description = "Requisição sem autenticação")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão ou docente sem vínculo com a turma")
    @ApiResponse(responseCode = "404", description = "Mapa, posição ou alocação não encontrados")
    public ResponseEntity<Void> moveStudent(
            @PathVariable UUID id,
            @Valid @RequestBody MoveStudentRequest request
    ) {
        moveStudentUseCase.execute(roomMapLocationMapper.toMoveCommand(id, request));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/allocations")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Atualizar alocações do mapa de sala",
            description = "Substitui atomicamente todas as alocações do mapa pelo estado recebido. ",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novo estado completo das alocações do mapa.",
                    required = true
            ))
    @ApiResponse(responseCode = "200", description = "Alocações atualizadas com sucesso")
    @ApiResponse(responseCode = "400", description = "Validação de alocações falhou (duplicatas, posição inválida, aluno ausente ou fora da turma)")
    @ApiResponse(responseCode = "401", description = "Requisição sem autenticação")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão ou docente sem vínculo com a turma")
    @ApiResponse(responseCode = "404", description = "Mapa não encontrado ou arquivado")
    public ResponseEntity<RoomMapViewResponse> updateAllocations(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomMapAllocationsRequest request
    ) {
        UpdateRoomMapAllocationsCommand command = new UpdateRoomMapAllocationsCommand(id, request);
        RoomMapViewResponse response = updateRoomMapAllocationsUseCase.execute(command);
        return ResponseEntity.ok(response);
    }
}