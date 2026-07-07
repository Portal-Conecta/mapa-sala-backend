package com.portal.conecta.mapa_de_sala.shared.config;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

/**
 * Popula LayoutTemplate/LayoutPosition/RoomLayout com dados padrão para o perfil {@code dev}.
 *
 * <p>Templates e posições representam layouts "de fábrica" do sistema — não editáveis
 * pela instituição. IDs são gerados automaticamente pelo banco (mesmo comportamento de
 * produção); a idempotência do seed é garantida por atributo de negócio (nome do template,
 * coordenada dentro do template, roomId), não por UUID fixo.
 *
 * <p>RoomLayout (vínculo com a sala física do Hub) é opcional. Como este serviço não
 * acessa o banco do Hub, o {@code roomId} real precisa ser informado via propriedade
 * {@code dev-seed.room-ids.*}. Se não for definida, o vínculo é pulado (log de aviso).
 *
 * <p><strong>Ativo apenas no perfil {@code dev}. Não deve ser executado em produção.</strong>
 */
@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private static final String TEMPLATE_L_NAME = "Layout L - Sala Padrão";
    private static final int TEMPLATE_L_DIM_X = 4;
    private static final int TEMPLATE_L_DIM_Y = 4;

    private static final String TEMPLATE_RECT_NAME = "Layout Retangular 3x3";
    private static final int TEMPLATE_RECT_DIM_X = 3;
    private static final int TEMPLATE_RECT_DIM_Y = 3;

    @Bean
    public CommandLineRunner seedSeatMapDevData(
            LayoutTemplateRepository layoutTemplateRepository,
            LayoutPositionRepository layoutPositionRepository,
            RoomLayoutRepository roomLayoutRepository,
            @Value("${dev-seed.room-ids.classroom:}") String classroomRoomId,
            @Value("${dev-seed.room-ids.laboratory:}") String laboratoryRoomId
    ) {
        return args -> {
            log.info("[DEV SEED][seat_map] Iniciando população de layouts...");

            LayoutTemplate templateL = findOrCreateTemplate(
                    layoutTemplateRepository, TEMPLATE_L_NAME, TEMPLATE_L_DIM_X, TEMPLATE_L_DIM_Y
            );
            seedLayoutLPositions(layoutPositionRepository, templateL);

            LayoutTemplate templateRect = findOrCreateTemplate(
                    layoutTemplateRepository, TEMPLATE_RECT_NAME, TEMPLATE_RECT_DIM_X, TEMPLATE_RECT_DIM_Y
            );
            seedRectangularPositions(layoutPositionRepository, templateRect);

            findOrCreateRoomLayout(roomLayoutRepository, classroomRoomId, templateL, "classroom (sala 101)");
            findOrCreateRoomLayout(roomLayoutRepository, laboratoryRoomId, templateRect, "laboratory (sala 201)");

            log.info("[DEV SEED][seat_map] Layouts disponíveis.");
        };
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private LayoutTemplate findOrCreateTemplate(
            LayoutTemplateRepository repo,
            String name,
            int dimensionX,
            int dimensionY
    ) {
        return repo.findAll().stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    LayoutTemplate template = new LayoutTemplate();
                    template.setName(name);
                    template.setDimensionX(dimensionX);
                    template.setDimensionY(dimensionY);
                    template.setActive(true);
                    LayoutTemplate saved = repo.save(template);
                    log.info("[DEV SEED][seat_map] Template criado: {} ({})", name, saved.getId());
                    return saved;
                });
    }

    private void seedLayoutLPositions(LayoutPositionRepository repo, LayoutTemplate template) {
        record Pos(int x, int y, LayoutPositionType type) {}

        List<Pos> positions = List.of(
                new Pos(0, 0, LayoutPositionType.STUDENT),
                new Pos(1, 0, LayoutPositionType.STUDENT),
                new Pos(0, 1, LayoutPositionType.STUDENT),
                new Pos(1, 1, LayoutPositionType.STUDENT),
                new Pos(0, 2, LayoutPositionType.STUDENT),
                new Pos(1, 2, LayoutPositionType.STUDENT),
                new Pos(2, 2, LayoutPositionType.STUDENT),
                new Pos(3, 2, LayoutPositionType.STUDENT),
                new Pos(0, 3, LayoutPositionType.TEACHER),
                new Pos(1, 3, LayoutPositionType.STUDENT),
                new Pos(2, 3, LayoutPositionType.STUDENT),
                new Pos(3, 3, LayoutPositionType.STUDENT)
        );

        positions.forEach(p -> findOrCreatePosition(repo, template, p.x(), p.y(), p.type()));
    }

    private void seedRectangularPositions(LayoutPositionRepository repo, LayoutTemplate template) {
        record Pos(int x, int y, LayoutPositionType type) {}

        List<Pos> positions = List.of(
                new Pos(0, 0, LayoutPositionType.TEACHER),
                new Pos(1, 0, LayoutPositionType.EQUIPMENT),
                new Pos(2, 0, LayoutPositionType.STUDENT),
                new Pos(0, 1, LayoutPositionType.STUDENT),
                new Pos(1, 1, LayoutPositionType.STUDENT),
                new Pos(2, 1, LayoutPositionType.STUDENT),
                new Pos(0, 2, LayoutPositionType.STUDENT),
                new Pos(1, 2, LayoutPositionType.OBSTACLE),
                new Pos(2, 2, LayoutPositionType.STUDENT)
        );

        positions.forEach(p -> findOrCreatePosition(repo, template, p.x(), p.y(), p.type()));
    }

    private void findOrCreatePosition(
            LayoutPositionRepository repo,
            LayoutTemplate template,
            int x,
            int y,
            LayoutPositionType type
    ) {
        boolean alreadyExists = repo.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(template.getId())
                .stream()
                .anyMatch(p -> p.getPositionX() == x && p.getPositionY() == y);

        if (alreadyExists) {
            return;
        }

        LayoutPosition position = new LayoutPosition();
        position.setLayoutTemplate(template);
        position.setPositionX(x);
        position.setPositionY(y);
        position.setType(type);
        repo.save(position);
        log.info("[DEV SEED][seat_map] Posição criada: ({},{}) {} em {}", x, y, type, template.getName());
    }

    private void findOrCreateRoomLayout(
            RoomLayoutRepository repo,
            String rawRoomId,
            LayoutTemplate template,
            String roomLabel
    ) {
        if (rawRoomId == null || rawRoomId.isBlank()) {
            log.warn(
                    "[DEV SEED][seat_map] dev-seed.room-ids não configurado para {}. RoomLayout não será criado.",
                    roomLabel
            );
            return;
        }

        UUID roomId;
        try {
            roomId = UUID.fromString(rawRoomId);
        } catch (IllegalArgumentException ex) {
            log.warn("[DEV SEED][seat_map] dev-seed.room-ids inválido para {}: {}", roomLabel, rawRoomId);
            return;
        }

        if (repo.findByRoomId(roomId).isPresent()) {
            return;
        }

        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(roomId);
        roomLayout.setLayoutTemplate(template);
        repo.save(roomLayout);
        log.info("[DEV SEED][seat_map] RoomLayout criado: sala {} ({}) -> template {}", roomId, roomLabel, template.getName());
    }
}