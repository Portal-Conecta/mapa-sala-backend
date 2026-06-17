package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.RoomMapViewAssembler;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapInitialAllocationCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapAllocationValidator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;

@Service
public class CreateRoomMapUseCase {

    private final RequestContextProvider requestContextProvider;
    private final HubClassPort hubClassPort;
    private final HubRoomPort hubRoomPort;
    private final LayoutTemplateRepository templateRepository;
    private final RoomMapRepository roomMapRepository;
    private final LayoutPositionRepository positionRepository;
    private final SeatNumberCalculator seatNumberCalculator;
    private final RoomMapAllocationValidator allocationValidator;
    private final RoomMapViewAssembler assembler;

    public CreateRoomMapUseCase(
            RequestContextProvider requestContextProvider,
            HubClassPort hubClassPort,
            HubRoomPort hubRoomPort,
            LayoutTemplateRepository templateRepository,
            RoomMapRepository roomMapRepository,
            LayoutPositionRepository positionRepository,
            SeatNumberCalculator seatNumberCalculator,
            RoomMapAllocationValidator allocationValidator,
            RoomMapViewAssembler assembler) {
        this.requestContextProvider = requestContextProvider;
        this.hubClassPort = hubClassPort;
        this.hubRoomPort = hubRoomPort;
        this.templateRepository = templateRepository;
        this.roomMapRepository = roomMapRepository;
        this.positionRepository = positionRepository;
        this.seatNumberCalculator = seatNumberCalculator;
        this.allocationValidator = allocationValidator;
        this.assembler = assembler;
    }

    @Transactional
    public RoomMapViewResponse execute(CreateRoomMapCommand command) {
        RequestContext context = requestContextProvider.getRequestContext();

        if (!"TEACHER".equals(context.userType().name())) {
            throw new AccessDeniedException("Apenas docentes podem criar mapas.");
        }

        if (!hubClassPort.existsById(command.classId())) {
            throw new ResourceNotFoundException("Turma", command.classId());
        }
        if (!hubRoomPort.existsById(command.roomId())) {
            throw new ResourceNotFoundException("Sala", command.roomId());
        }

        LayoutTemplate template = templateRepository.findByIdAndActiveTrue(command.layoutTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template", command.layoutTemplateId()));

        boolean isLinkedToClass = context.classes().stream()
                .anyMatch(c -> c.classId().equals(command.classId()));
        if (!isLinkedToClass) {
            throw new AccessDeniedException("Docente não vinculado à turma.");
        }

        boolean activeMapExists = roomMapRepository
                .findByClassIdAndRoomIdAndRemovedAtIsNull(command.classId(), command.roomId())
                .isPresent();
        if (activeMapExists) {
            throw new ConflictException("Já existe um mapa ativo para esta turma e sala.");
        }

        List<LayoutPosition> templatePositions =
                positionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId());

        SeatNumbering numbering = seatNumberCalculator.calculate(templatePositions);

        List<UUID> studentIds = new ArrayList<>();
        List<UUID> positionIds = new ArrayList<>();
        List<RoomMapLocation> locations = new ArrayList<>();

        if (command.locations() != null && !command.locations().isEmpty()) {
            for (CreateRoomMapInitialAllocationCommand locationCommand : command.locations()) {
                UUID positionId = resolvePositionId(locationCommand, templatePositions, numbering);
                studentIds.add(locationCommand.studentId());
                positionIds.add(positionId);

                RoomMapLocation location = new RoomMapLocation();
                location.setStudentId(locationCommand.studentId());
                location.setLayoutPositionId(positionId);
                locations.add(location);
            }
            allocationValidator.validate(studentIds, positionIds, templatePositions);
        }

        RoomMap roomMap = new RoomMap();
        roomMap.setClassId(command.classId());
        roomMap.setRoomId(command.roomId());
        roomMap.setLayoutTemplateId(template.getId());
        roomMap.setLayoutTemplate(template);

        for (RoomMapLocation location : locations) {
            location.setRoomMap(roomMap);
            roomMap.getLocations().add(location);
        }

        RoomMapHistory history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(context.userId());
        history.setAction(RoomMapHistoryAction.MAP_CREATION);
        history.setDetails("Criação inicial do mapa");
        roomMap.getHistory().add(history);

        RoomMap savedMap = roomMapRepository.save(roomMap);

        List<HubStudent> classStudents = hubClassPort.findStudentsByClassId(command.classId());

        return assembler.assembleFromSavedMap(
                savedMap,
                template.getDimensionY(),
                template.getDimensionX(),
                templatePositions,
                numbering,
                savedMap.getLocations(),
                classStudents
        );
    }

    private UUID resolvePositionId(
            CreateRoomMapInitialAllocationCommand command,
            List<LayoutPosition> positions,
            SeatNumbering numbering) {
        if (command.layoutPositionId() != null) {
            return command.layoutPositionId();
        }
        if (command.seatNumber() != null) {
            return positions.stream()
                    .filter(position -> command.seatNumber().equals(numbering.seatNumberOf(position.getId())))
                    .map(LayoutPosition::getId)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("seatNumber inválido ou não encontrado no template."));
        }
        throw new BadRequestException("É obrigatório informar seatNumber ou layoutPositionId na alocação.");
    }
}