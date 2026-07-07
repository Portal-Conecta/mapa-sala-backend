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
 * controller, via {@code RoomLayoutAuthorizationService.checkWriteAccess}, seguindo
 * o mesmo padrão já usado na consulta de leitura deste módulo. Este use case
 * concentra apenas as regras de negócio do vínculo.</p>
 */
@Component
@Slf4j
public class CreateRoomLayoutUseCase {

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
     * @throws ConflictException         se a sala já possuir um layout vinculado.
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
            throw new ConflictException("Sala já possui layout vinculado.");
        }

        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(command.roomId());
        roomLayout.setLayoutTemplate(template);

        RoomLayout saved = roomLayoutRepository.save(roomLayout);
        log.info("Layout vinculado à sala com sucesso.");

        return saved;
    }
}