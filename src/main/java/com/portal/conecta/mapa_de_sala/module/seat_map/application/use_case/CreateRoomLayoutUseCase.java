package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomLayoutCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vincula uma sala real do Hub a um layout_template já existente.
 *
 * <p>Salas são criadas dinamicamente no Hub, sem evento de domínio disponível
 * para este serviço, por isso o vínculo é feito sob demanda por um administrador,
 * em vez de via migration.</p>
 *
 * <p>A autorização de escrita (perfil ADMIN/SENAI/WEG) é responsabilidade do
 * controller, via {@code RoomLayoutAuthorizationService.checkWriteAccess}. Este
 * use case concentra apenas as regras de negócio do vínculo.</p>
 *
 * <p>A checagem de conflito via {@code findByRoomId} elimina a maioria dos casos,
 * mas não fecha a janela entre a leitura e a escrita sob concorrência real. A
 * constraint {@code uq_room_layout_room_id} (migration V3) é a garantia definitiva
 * de unicidade; a violação dela na persistência também é tratada como conflito de
 * negócio, não como erro interno.</p>
 */
@Component
@Slf4j
public class CreateRoomLayoutUseCase {

    private static final String ROOM_ALREADY_LINKED_MESSAGE = "Sala já possui layout vinculado.";

    private final HubRoomPort hubRoomPort;
    private final LayoutTemplateRepository layoutTemplateRepository;
    private final RoomLayoutRepository roomLayoutRepository;

    public CreateRoomLayoutUseCase(
            HubRoomPort hubRoomPort,
            LayoutTemplateRepository layoutTemplateRepository,
            RoomLayoutRepository roomLayoutRepository
    ) {
        this.hubRoomPort = hubRoomPort;
        this.layoutTemplateRepository = layoutTemplateRepository;
        this.roomLayoutRepository = roomLayoutRepository;
    }

    /**
     * Executa a criação do vínculo entre sala e template de layout.
     *
     * @param command identificadores da sala (Hub) e do template de layout.
     * @return o vínculo persistido.
     * @throws ResourceNotFoundException se a sala não existir no Hub.
     * @throws ResourceNotFoundException se o template não existir ou estiver inativo.
     * @throws ConflictException         se a sala já possuir um layout vinculado,
     *                                   detectado pela checagem prévia ou pela
     *                                   constraint única do banco sob concorrência.
     */
    @Transactional
    public RoomLayout execute(CreateRoomLayoutCommand command) {

        if (!hubRoomPort.existsById(command.roomId())) {
            throw new ResourceNotFoundException("Sala", command.roomId());
        }

        LayoutTemplate template = layoutTemplateRepository
                .findByIdAndActiveTrue(command.layoutTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template", command.layoutTemplateId()));

        boolean roomAlreadyLinked = roomLayoutRepository.findByRoomId(command.roomId()).isPresent();
        if (roomAlreadyLinked) {
            throw new ConflictException(ROOM_ALREADY_LINKED_MESSAGE);
        }

        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(command.roomId());
        roomLayout.setLayoutTemplate(template);

        RoomLayout saved = persist(roomLayout);
        log.info("Layout vinculado à sala com sucesso.");

        return saved;
    }

    /**
     * Persiste o vínculo, convertendo a violação da constraint única de banco
     * (concorrência entre a checagem prévia e a escrita) em ConflictException.
     */
    private RoomLayout persist(RoomLayout roomLayout) {
        try {
            RoomLayout saved = roomLayoutRepository.save(roomLayout);
            roomLayoutRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ROOM_ALREADY_LINKED_MESSAGE);
        }
    }
}