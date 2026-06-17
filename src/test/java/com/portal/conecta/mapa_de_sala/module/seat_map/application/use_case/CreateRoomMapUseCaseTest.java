package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.access.AccessDeniedException;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.RoomMapViewAssembler;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapInitialAllocationCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapAllocationValidator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

class CreateRoomMapUseCaseTest {

    private RequestContextProvider requestContextProvider;
    private HubClassPort hubClassPort;
    private HubRoomPort hubRoomPort;
    private LayoutTemplateRepository templateRepository;
    private RoomMapRepository roomMapRepository;
    private LayoutPositionRepository positionRepository;
    private SeatNumberCalculator seatNumberCalculator;
    private RoomMapAllocationValidator allocationValidator;
    private RoomMapViewAssembler assembler;

    private CreateRoomMapUseCase useCase;

    private UUID classId;
    private UUID roomId;
    private UUID templateId;
    private LayoutTemplate template;

    @BeforeEach
    void setUp() {
        requestContextProvider = mock(RequestContextProvider.class);
        hubClassPort = mock(HubClassPort.class);
        hubRoomPort = mock(HubRoomPort.class);
        templateRepository = mock(LayoutTemplateRepository.class);
        roomMapRepository = mock(RoomMapRepository.class);
        positionRepository = mock(LayoutPositionRepository.class);
        seatNumberCalculator = new SeatNumberCalculator();
        allocationValidator = new RoomMapAllocationValidator();
        assembler = mock(RoomMapViewAssembler.class);

        useCase = new CreateRoomMapUseCase(
                requestContextProvider,
                hubClassPort,
                hubRoomPort,
                templateRepository,
                roomMapRepository,
                positionRepository,
                seatNumberCalculator,
                allocationValidator,
                assembler
        );

        classId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        templateId = UUID.randomUUID();

        template = mock(LayoutTemplate.class);
        lenient().when(template.getId()).thenReturn(templateId);
        lenient().when(template.getDimensionX()).thenReturn(6);
        lenient().when(template.getDimensionY()).thenReturn(5);

        lenient().when(hubClassPort.existsById(classId)).thenReturn(true);
        lenient().when(hubRoomPort.existsById(roomId)).thenReturn(true);
        lenient().when(templateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));
        lenient().when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId))
                .thenReturn(Optional.empty());
        lenient().when(roomMapRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(assembler.assembleFromSavedMap(
                any(), anyInt(), anyInt(), any(), any(), any(), any()
        )).thenReturn(mock(RoomMapViewResponse.class));

        givenRequestContext(TypeUser.TEACHER, classId);
    }

    private void givenRequestContext(TypeUser userType, UUID linkedClassId) {
        List<ContextClass> classes = linkedClassId == null
                ? List.of()
                : List.of(new ContextClass(linkedClassId, "DEFAULT"));
        RequestContext context = new RequestContext(UUID.randomUUID(), userType, classes);
        when(requestContextProvider.getRequestContext()).thenReturn(context);
    }

    private List<LayoutPosition> twoStudentPositionsTemplate() {
        LayoutPosition position1 = mock(LayoutPosition.class);
        lenient().when(position1.getId()).thenReturn(UUID.randomUUID());
        lenient().when(position1.getPositionX()).thenReturn(0);
        lenient().when(position1.getPositionY()).thenReturn(0);
        lenient().when(position1.getType()).thenReturn(LayoutPositionType.STUDENT);

        LayoutPosition position2 = mock(LayoutPosition.class);
        lenient().when(position2.getId()).thenReturn(UUID.randomUUID());
        lenient().when(position2.getPositionX()).thenReturn(1);
        lenient().when(position2.getPositionY()).thenReturn(0);
        lenient().when(position2.getType()).thenReturn(LayoutPositionType.STUDENT);

        return List.of(position1, position2);
    }

    // --- Permissão -----------------------------------------------------

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE", "SENAI", "WEG", "ADMIN"})
    void deveLancarAccessDeniedQuandoPerfilNaoEDocente(TypeUser userType) {
        givenRequestContext(userType, classId);

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveLancarAccessDeniedQuandoDocenteNaoVinculadoATurma() {
        givenRequestContext(TypeUser.TEACHER, UUID.randomUUID());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- Existência dos recursos (404) ----------------------------------

    @Test
    void deveLancarResourceNotFoundQuandoTurmaNaoExiste() {
        when(hubClassPort.existsById(classId)).thenReturn(false);

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarResourceNotFoundQuandoSalaNaoExiste() {
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarResourceNotFoundQuandoTemplateNaoExiste() {
        when(templateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.empty());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Mapa duplicado (409) -------------------------------------------

    @Test
    void deveLancarConflictQuandoJaExisteMapaAtivo() {
        RoomMap existingMap = mock(RoomMap.class);
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId))
                .thenReturn(Optional.of(existingMap));

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class);
    }

    // --- RN-MS10: integridade das alocações -----------------------------

    @Test
    void deveLancarBadRequestQuandoPosicaoNaoPertenceAoTemplate() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        UUID studentId = UUID.randomUUID();
        UUID positionForaDoTemplate = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(studentId, null, positionForaDoTemplate))
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deveLancarConflictQuandoStudentIdDuplicado() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        UUID studentId = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(
                        new CreateRoomMapInitialAllocationCommand(studentId, null, positions.get(0).getId()),
                        new CreateRoomMapInitialAllocationCommand(studentId, null, positions.get(1).getId())
                )
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveLancarConflictQuandoPosicaoDuplicada() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        UUID mesmaPosicao = positions.get(0).getId();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(
                        new CreateRoomMapInitialAllocationCommand(UUID.randomUUID(), null, mesmaPosicao),
                        new CreateRoomMapInitialAllocationCommand(UUID.randomUUID(), null, mesmaPosicao)
                )
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deveLancarBadRequestQuandoPosicaoNaoEDoTipoStudent() {
        LayoutPosition obstaclePosition = mock(LayoutPosition.class);
        lenient().when(obstaclePosition.getId()).thenReturn(UUID.randomUUID());
        lenient().when(obstaclePosition.getPositionX()).thenReturn(2);
        lenient().when(obstaclePosition.getPositionY()).thenReturn(0);
        lenient().when(obstaclePosition.getType()).thenReturn(LayoutPositionType.OBSTACLE);

        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(List.of(obstaclePosition));

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(UUID.randomUUID(), null, obstaclePosition.getId()))
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BadRequestException.class);
    }

    // --- Resolução seatNumber -> layoutPositionId ------------------------

    @Test
    void deveLancarBadRequestQuandoSeatNumberNaoExisteNoTemplate() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(UUID.randomUUID(), 999, null))
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deveLancarBadRequestQuandoNenhumIdentificadorDePosicaoEInformado() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(UUID.randomUUID(), null, null))
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deveResolverSeatNumberParaLayoutPositionIdCorretamente() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(List.of());

        UUID studentId = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(studentId, 1, null))
        );

        RoomMapViewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
    }

    // --- Cenários de sucesso --------------------------------------------

    @Test
    void deveCriarMapaSemAlocacoes() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(List.of());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        RoomMapViewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
    }

    @Test
    void deveCriarMapaComAlocacoesViaLayoutPositionId() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(List.of());

        UUID studentId = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(studentId, null, positions.get(0).getId()))
        );

        RoomMapViewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
    }

    @Test
    void deveMarcarTemplateESalvarMapaComLayoutTemplateIdSnapshot() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(List.of());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        useCase.execute(command);

        org.mockito.Mockito.verify(roomMapRepository).save(
                org.mockito.ArgumentMatchers.argThat(roomMap -> templateId.equals(roomMap.getLayoutTemplateId()))
        );
    }
}