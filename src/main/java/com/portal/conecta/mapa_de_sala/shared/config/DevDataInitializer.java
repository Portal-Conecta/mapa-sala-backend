package com.portal.conecta.mapa_de_sala.shared.config;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

/**
 * Cria vinculos RoomLayout (sala do Hub -> template de layout) para o perfil {@code dev}.
 *
 * <p>Os templates e posicoes ja sao populados pela migration Flyway com UUIDs fixos.
 * Este seed apenas vincula salas fisicas (roomIds reais do Mock do Hub) aos templates
 * existentes, permitindo testar os endpoints de mapa de sala com dados realistas sem
 * precisar chamar o POST /api/layouts/salas manualmente a cada subida do ambiente.
 *
 * <p>Usa {@link LayoutTemplateRepository#findByIdAndActiveTrue} de propósito, mesmo
 * metodo usado por {@code CreateRoomLayoutUseCase}, para que o seed falhe cedo caso
 * algum template seja desativado por engano, em vez de vincular uma sala a um
 * template que a API nunca aceitaria via POST.
 *
 * <p><strong>Ativo apenas no perfil {@code dev}. Nao deve ser executado em producao.</strong>
 */
@Configuration
@Profile("dev")
@Slf4j
public class DevDataInitializer {

    private static final UUID TEMPLATE_A_ID = UUID.fromString("00000000-0000-0000-0001-000000000001"); // 9x4, prof x=0
    private static final UUID TEMPLATE_B_ID = UUID.fromString("00000000-0000-0000-0002-000000000001"); // 9x4, prof x=8
    private static final UUID TEMPLATE_C_ID = UUID.fromString("00000000-0000-0000-0003-000000000001"); // 5x7, prof x=0
    private static final UUID TEMPLATE_D_ID = UUID.fromString("00000000-0000-0000-0004-000000000001"); // 10x4, prof x=9
    private static final UUID TEMPLATE_E_ID = UUID.fromString("00000000-0000-0000-0005-000000000001"); // 9x5, Lab 101
    private static final UUID TEMPLATE_F_ID = UUID.fromString("00000000-0000-0000-0006-000000000001"); // 13x3
    private static final UUID TEMPLATE_G_ID = UUID.fromString("00000000-0000-0000-0007-000000000001"); // 7x5

    private static final UUID ROOM_101_ID = UUID.fromString("5fe5c746-4b1a-43c2-8f01-a8a137875c57"); // CLASSROOM
    private static final UUID ROOM_201_ID = UUID.fromString("e7dda250-0987-4b61-aa78-d242e5baaf8c"); // LABORATORY

    @Bean
    public CommandLineRunner seedSeatMapDevData(
            LayoutTemplateRepository layoutTemplateRepository,
            RoomLayoutRepository roomLayoutRepository
    ) {
        return args -> {
            log.info("[DEV SEED][seat_map] Iniciando vinculos RoomLayout para dev...");

            linkRoomToTemplate(roomLayoutRepository, layoutTemplateRepository,
                    ROOM_101_ID, TEMPLATE_E_ID, "Sala 101 -> Template E (Lab Eletrotecnica 9x5)");

            linkRoomToTemplate(roomLayoutRepository, layoutTemplateRepository,
                    ROOM_201_ID, TEMPLATE_A_ID, "Sala 201 -> Template A (Sala de Aula 9x4, prof x=0)");

            log.info("[DEV SEED][seat_map] Vinculos RoomLayout concluidos. "
                    + "Templates B, C, D, F, G ainda sem sala real vinculada "
                    + "(nao ha roomId disponivel no Mock do Hub para elas).");
        };
    }

    private void linkRoomToTemplate(
            RoomLayoutRepository roomLayoutRepository,
            LayoutTemplateRepository layoutTemplateRepository,
            UUID roomId,
            UUID templateId,
            String label
    ) {
        if (roomLayoutRepository.findByRoomId(roomId).isPresent()) {
            log.info("[DEV SEED][seat_map] {} ja possui vinculo. Pulado.", label);
            return;
        }

        LayoutTemplate template = layoutTemplateRepository.findByIdAndActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "[DEV SEED][seat_map] Template nao encontrado ou inativo: " + templateId
                                + ". Verifique se a migration V4 foi executada."));

        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(roomId);
        roomLayout.setLayoutTemplate(template);
        roomLayoutRepository.save(roomLayout);

        log.info("[DEV SEED][seat_map] Criado: {} (roomId={}, layoutTemplateId={})",
                label, roomId, templateId);
    }
}