package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapInitialAllocationCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomMapReplicationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRoomMapUseCase {

    private final RequestContextProvider requestContextProvider;
    private final RoomMapRepository roomMapRepository;

    private final LayoutTemplateRepository layoutTemplateRepository;
    private final LayoutPositionRepository layoutPositionRepository;

    private final RoomMapCreationValidator creationValidator;
    private final RoomMapAllocationValidator allocationValidator;
    private final SeatNumberCalculator seatNumberCalculator;
    private final RoomMapPositionResolver positionResolver;
    private final RoomMapReplicationService replicationService;

    @Transactional
    public UUID execute(CreateRoomMapCommand command) {
        RequestContext context = requestContextProvider.getRequestContext();

        creationValidator.validatePreConditions(command.classId(), command.roomId(), context);

        LayoutTemplate template = layoutTemplateRepository.findByIdAndActiveTrue(command.layoutTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template", command.layoutTemplateId()));

        List<LayoutPosition> templatePositions = layoutPositionRepository
                .findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId());

        SeatNumbering numbering = seatNumberCalculator.calculate(templatePositions);

        List<RoomMapLocation> locations = new ArrayList<>();
        if (command.locations() != null && !command.locations().isEmpty()) {
            List<UUID> studentIds = new ArrayList<>();
            List<UUID> positionIds = new ArrayList<>();

            for (CreateRoomMapInitialAllocationCommand locCommand : command.locations()) {
                UUID positionId = positionResolver.resolvePositionId(locCommand, templatePositions, numbering);
                LayoutPosition layoutPosition = templatePositions.stream()
                        .filter(position -> position.getId().equals(positionId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Posicao", positionId));

                studentIds.add(locCommand.studentId());
                positionIds.add(positionId);

                RoomMapLocation location = new RoomMapLocation();
                location.setStudentId(locCommand.studentId());
                location.setLayoutPositionId(positionId);
                location.setLayoutPosition(layoutPosition);
                locations.add(location);
            }

            allocationValidator.validate(studentIds, positionIds, templatePositions);
        }

        RoomMap roomMap = RoomMap.create(
                command.classId(),
                command.roomId(),
                template,
                locations,
                context.userId()
        );

        RoomMap savedMap = roomMapRepository.save(roomMap);
        replicationService.replicateAfterCommit(savedMap, context.userId());

        return savedMap.getId();
    }
}
