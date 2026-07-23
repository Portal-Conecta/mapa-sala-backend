package com.portal.conecta.mapa_de_sala.shared.config;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Vincula, no perfil de desenvolvimento, as salas deterministicas do Hub aos
 * templates de layout definidos na migration do catalogo.
 *
 * <p>O seed nao consulta o Hub durante o boot porque esses endpoints exigem
 * autenticacao. Os IDs das salas sao o contrato estabelecido pelo
 * {@code DevDataInitializer} do Core.</p>
 */
@Configuration
@Profile("dev")
@Slf4j
public class DevDataInitializer {

    private static final UUID TEMPLATE_A_ID = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private static final UUID TEMPLATE_B_ID = UUID.fromString("00000000-0000-0000-0002-000000000001");
    private static final UUID TEMPLATE_C_ID = UUID.fromString("00000000-0000-0000-0003-000000000001");
    private static final UUID TEMPLATE_D_ID = UUID.fromString("00000000-0000-0000-0004-000000000001");
    private static final UUID TEMPLATE_E_ID = UUID.fromString("00000000-0000-0000-0005-000000000001");
    private static final UUID TEMPLATE_F_ID = UUID.fromString("00000000-0000-0000-0006-000000000001");
    private static final UUID TEMPLATE_G_ID = UUID.fromString("00000000-0000-0000-0007-000000000001");

    private static final List<RoomLayoutSeed> ROOM_LAYOUTS = List.of(
            new RoomLayoutSeed(101, TEMPLATE_E_ID),
            new RoomLayoutSeed(102, TEMPLATE_F_ID),
            new RoomLayoutSeed(103, TEMPLATE_F_ID),
            new RoomLayoutSeed(109, TEMPLATE_F_ID),
            new RoomLayoutSeed(110, TEMPLATE_F_ID),
            new RoomLayoutSeed(201, TEMPLATE_A_ID),
            new RoomLayoutSeed(202, TEMPLATE_B_ID),
            new RoomLayoutSeed(203, TEMPLATE_C_ID),
            new RoomLayoutSeed(204, TEMPLATE_A_ID),
            new RoomLayoutSeed(205, TEMPLATE_B_ID),
            new RoomLayoutSeed(206, TEMPLATE_B_ID),
            new RoomLayoutSeed(207, TEMPLATE_D_ID),
            new RoomLayoutSeed(211, TEMPLATE_G_ID),
            new RoomLayoutSeed(212, TEMPLATE_A_ID),
            new RoomLayoutSeed(213, TEMPLATE_A_ID),
            new RoomLayoutSeed(214, TEMPLATE_B_ID)
    );

    @Bean
    public CommandLineRunner seedSeatMapDevData(
            RoomLayoutRepository roomLayoutRepository,
            LayoutTemplateRepository layoutTemplateRepository
    ) {
        return args -> {
            log.info("[DEV SEED][seat_map] Iniciando vinculos RoomLayout para dev...");
            ROOM_LAYOUTS.forEach(seed -> linkRoomToTemplate(roomLayoutRepository, layoutTemplateRepository, seed));
            log.info("[DEV SEED][seat_map] Vinculos RoomLayout concluidos para {} salas.", ROOM_LAYOUTS.size());
        };
    }

    private void linkRoomToTemplate(
            RoomLayoutRepository roomLayoutRepository,
            LayoutTemplateRepository layoutTemplateRepository,
            RoomLayoutSeed seed
    ) {
        UUID roomId = seed.roomId();
        if (roomLayoutRepository.findByRoomId(roomId).isPresent()) {
            log.info("[DEV SEED][seat_map] Sala {} ja possui vinculo. Pulado.", seed.roomNumber());
            return;
        }

        LayoutTemplate template = layoutTemplateRepository.findByIdAndActiveTrue(seed.templateId()).orElse(null);
        if (template == null) {
            log.warn("[DEV SEED][seat_map] Template {} da sala {} nao foi encontrado. Pulado.",
                    seed.templateId(), seed.roomNumber());
            return;
        }

        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(roomId);
        roomLayout.setLayoutTemplate(template);
        roomLayoutRepository.save(roomLayout);
        log.info("[DEV SEED][seat_map] Criado: Sala {} -> template {}.", seed.roomNumber(), template.getName());
    }

    private record RoomLayoutSeed(int roomNumber, UUID templateId) {
        private UUID roomId() {
            return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(roomNumber));
        }
    }
}
