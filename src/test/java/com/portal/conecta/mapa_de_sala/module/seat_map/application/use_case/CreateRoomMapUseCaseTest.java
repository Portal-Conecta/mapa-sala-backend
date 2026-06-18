package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

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
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapCreationValidator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapPositionResolver;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

class CreateRoomMapUseCaseTest {

    private RequestContextProvider requestContextProvider;
    private RoomMapRepository roomMapRepository;
    private LayoutTemplateRepository templateRepository;
    private LayoutPositionRepository positionRepository;

    private RoomMapCreationValidator creationValidator;
    private RoomMapAllocationValidator allocationValidator;
    private SeatNumberCalculator seatNumberCalculator;
    private RoomMapPositionResolver positionResolver;

    private CreateRoomMapUseCase useCase;

    private UUID classId;
    private UUID roomId;
    private UUID templateId;
    private LayoutTemplate template;

    @BeforeEach
    void setUp() {
        requestContextProvider = mock(RequestContextProvider.class);
        roomMapRepository = mock(RoomMapRepository.class);
        templateRepository = mock(LayoutTemplateRepository.class);
        positionRepository = mock(LayoutPositionRepository.class);

        creationValidator = mock(RoomMapCreationValidator.class);

        allocationValidator = new RoomMapAllocationValidator();
        seatNumberCalculator = new SeatNumberCalculator();
        positionResolver = new RoomMapPositionResolver();

        useCase = new CreateRoomMapUseCase(
                requestContextProvider,
                roomMapRepository,
                templateRepository,
                positionRepository,
                creationValidator,
                allocationValidator,
                seatNumberCalculator,
                positionResolver
        );

        classId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        templateId = UUID.randomUUID();

        template = mock(LayoutTemplate.class);
        lenient().when(template.getId()).thenReturn(templateId);
        lenient().when(template.getDimensionX()).thenReturn(6);
        lenient().when(template.getDimensionY()).thenReturn(5);

        lenient().when(templateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));

        lenient().when(roomMapRepository.save(any())).thenAnswer(invocation -> {
            RoomMap map = invocation.getArgument(0);
            RoomMap spiedMap = org.mockito.Mockito.spy(map);
            lenient().when(spiedMap.getId()).thenReturn(UUID.randomUUID());
            lenient().when(spiedMap.getLayoutTemplateId()).thenReturn(templateId);
            return spiedMap;
        });

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
        lenient().when(position1.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        lenient().when(position1.getPositionX()).thenReturn(0);
        lenient().when(position1.getPositionY()).thenReturn(0);
        lenient().when(position1.getType()).thenReturn(LayoutPositionType.STUDENT);

        LayoutPosition position2 = mock(LayoutPosition.class);
        lenient().when(position2.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        lenient().when(position2.getPositionX()).thenReturn(1);
        lenient().when(position2.getPositionY()).thenReturn(0);
        lenient().when(position2.getType()).thenReturn(LayoutPositionType.STUDENT);

        return List.of(position1, position2);
    }


    @Test
    void deveRepassarExceptionQuandoCreationValidatorNegarPreCondicoes() {
        doThrow(new AccessDeniedException("Docente não vinculado à turma."))
                .when(creationValidator).validatePreConditions(any(), any(), any());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Docente não vinculado à turma.");
    }

    @Test
    void deveRepassarConflictQuandoCreationValidatorIdentificarMapaAtivo() {
        doThrow(new ConflictException("Já existe um mapa ativo para esta turma e sala."))
                .when(creationValidator).validatePreConditions(any(), any(), any());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Já existe um mapa ativo para esta turma e sala.");
    }


    @Test
    void deveLancarResourceNotFoundQuandoTemplateNaoExiste() {
        when(templateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.empty());

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }


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
    void deveCriarMapaSemAlocacoesERetornarIdDoMapa() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        UUID generatedId = useCase.execute(command);

        assertThat(generatedId).isNotNull();
    }

    @Test
    void deveCriarMapaComAlocacoesViaLayoutPositionId() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        UUID studentId = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(studentId, null, positions.get(0).getId()))
        );

        UUID generatedId = useCase.execute(command);

        assertThat(generatedId).isNotNull();
    }

    @Test
    void deveResolverSeatNumberParaLayoutPositionIdCorretamenteECriarMapa() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        UUID studentId = UUID.randomUUID();

        CreateRoomMapCommand command = new CreateRoomMapCommand(
                classId, roomId, templateId,
                List.of(new CreateRoomMapInitialAllocationCommand(studentId, 1, null))
        );

        UUID generatedId = useCase.execute(command);

        assertThat(generatedId).isNotNull();
    }

    @Test
    void deveMarcarTemplateESalvarMapaComLayoutTemplateIdSnapshot() {
        List<LayoutPosition> positions = twoStudentPositionsTemplate();
        when(positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(positions);

        CreateRoomMapCommand command = new CreateRoomMapCommand(classId, roomId, templateId, List.of());

        useCase.execute(command);

        org.mockito.Mockito.verify(roomMapRepository).save(any(RoomMap.class));
    }
}