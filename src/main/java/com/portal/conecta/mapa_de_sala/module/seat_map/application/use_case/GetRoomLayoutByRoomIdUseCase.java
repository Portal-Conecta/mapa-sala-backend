package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.LayoutTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetRoomLayoutByRoomIdUseCase {

    private final HubRoomPort hubRoomPort;
    private final RoomLayoutRepository roomLayoutRepository;
    private final LayoutTemplateRepository layoutTemplateRepository;
    private final LayoutPositionRepository layoutPositionRepository;
    private final LayoutTemplateMapper layoutTemplateMapper;

    public GetRoomLayoutByRoomIdUseCase(
            HubRoomPort hubRoomPort,
            RoomLayoutRepository roomLayoutRepository,
            LayoutTemplateRepository layoutTemplateRepository,
            LayoutPositionRepository layoutPositionRepository,
            LayoutTemplateMapper layoutTemplateMapper
    ) {
        this.hubRoomPort = hubRoomPort;
        this.roomLayoutRepository = roomLayoutRepository;
        this.layoutTemplateRepository = layoutTemplateRepository;
        this.layoutPositionRepository = layoutPositionRepository;
        this.layoutTemplateMapper = layoutTemplateMapper;
    }

    @Transactional(readOnly = true)
    public LayoutTemplateWithPositionsResponse execute(UUID roomId) {
        if (!hubRoomPort.existsById(roomId)) {
            throw new ResourceNotFoundException("Sala", roomId);
        }

        var roomLayout = roomLayoutRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Layout da sala", roomId));

        LayoutTemplate template = layoutTemplateRepository.findByIdAndActiveTrue(roomLayout.getLayoutTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template de layout", roomLayout.getLayoutTemplateId()));

        var positions = layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId());

        return layoutTemplateMapper.toWithPositionsResponse(template, positions);
    }
}
