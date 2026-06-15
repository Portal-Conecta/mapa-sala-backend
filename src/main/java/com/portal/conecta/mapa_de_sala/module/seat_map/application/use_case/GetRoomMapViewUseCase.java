package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.RoomMapViewAssembler;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.service.RoomMapViewAuthorizationService;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapLocationRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GetRoomMapViewUseCase {

    private final RoomMapRepository roomMapRepository;
    private final RoomMapLocationRepository roomMapLocationRepository;
    private final RoomLayoutRepository roomLayoutRepository;
    private final LayoutTemplateRepository layoutTemplateRepository;
    private final LayoutPositionRepository layoutPositionRepository;
    private final HubClassPort hubClassPort;
    private final HubRoomPort hubRoomPort;
    private final RequestContextProvider requestContextProvider;
    private final RoomMapViewAuthorizationService authorizationService;
    private final SeatNumberCalculator seatNumberCalculator;
    private final RoomMapViewAssembler assembler;

    public GetRoomMapViewUseCase(
            RoomMapRepository roomMapRepository,
            RoomMapLocationRepository roomMapLocationRepository,
            RoomLayoutRepository roomLayoutRepository,
            LayoutTemplateRepository layoutTemplateRepository,
            LayoutPositionRepository layoutPositionRepository,
            HubClassPort hubClassPort,
            HubRoomPort hubRoomPort,
            RequestContextProvider requestContextProvider,
            RoomMapViewAuthorizationService authorizationService,
            SeatNumberCalculator seatNumberCalculator,
            RoomMapViewAssembler assembler
    ) {
        this.roomMapRepository = roomMapRepository;
        this.roomMapLocationRepository = roomMapLocationRepository;
        this.roomLayoutRepository = roomLayoutRepository;
        this.layoutTemplateRepository = layoutTemplateRepository;
        this.layoutPositionRepository = layoutPositionRepository;
        this.hubClassPort = hubClassPort;
        this.hubRoomPort = hubRoomPort;
        this.requestContextProvider = requestContextProvider;
        this.authorizationService = authorizationService;
        this.seatNumberCalculator = seatNumberCalculator;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    public RoomMapViewResponse execute(UUID roomId, UUID classId) {
        RequestContext context = requestContextProvider.getRequestContext();

        if (!hubClassPort.existsById(classId)) {
            throw new ResourceNotFoundException("Turma", classId);
        }

        if (!hubRoomPort.existsById(roomId)) {
            throw new ResourceNotFoundException("Sala", roomId);
        }

        authorizationService.ensureCanViewClass(context, classId);

        List<HubStudent> classStudents = hubClassPort.findStudentsByClassId(classId);

        Optional<RoomMap> roomMap = roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId);

        if (roomMap.isPresent()) {
            return buildSavedMapView(roomMap.get(), classStudents);
        }

        return buildSuggestion(roomId, classStudents);
    }

    private RoomMapViewResponse buildSavedMapView(RoomMap roomMap, List<HubStudent> classStudents) {
        LayoutTemplate template = roomMap.getLayoutTemplate();

        List<LayoutPosition> positions =
                layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId());

        SeatNumbering numbering = seatNumberCalculator.calculate(positions);

        List<RoomMapLocation> locations = roomMapLocationRepository.findByRoomMapId(roomMap.getId());

        return assembler.assembleFromSavedMap(
                roomMap,
                template.getDimensionY(),
                template.getDimensionX(),
                positions,
                numbering,
                locations,
                classStudents
        );
    }

    private RoomMapViewResponse buildSuggestion(UUID roomId, List<HubStudent> classStudents) {
        RoomLayout roomLayout = roomLayoutRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Layout da sala", roomId));

        LayoutTemplate template = layoutTemplateRepository.findByIdAndActiveTrue(roomLayout.getLayoutTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template de layout", roomLayout.getLayoutTemplateId()));

        List<LayoutPosition> positions =
                layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId());

        SeatNumbering numbering = seatNumberCalculator.calculate(positions);

        List<HubStudent> alphabeticalStudents = classStudents.stream()
                .sorted(Comparator.comparing(HubStudent::name))
                .toList();

        return assembler.assembleFromSuggestion(
                template.getDimensionY(),
                template.getDimensionX(),
                positions,
                numbering,
                alphabeticalStudents
        );
    }
}