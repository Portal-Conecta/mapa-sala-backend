package com.portal.conecta.mapa_de_sala.shared.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DevDataInitializerTest {

    @Mock private RoomLayoutRepository roomLayoutRepository;
    @Mock private LayoutTemplateRepository layoutTemplateRepository;

    @Test
    void seedsTheCatalogLayoutForEveryCoreDevelopmentRoom() throws Exception {
        Map<UUID, LayoutTemplate> templates = templates();
        when(roomLayoutRepository.findByRoomId(any(UUID.class))).thenReturn(Optional.empty());
        when(layoutTemplateRepository.findByIdAndActiveTrue(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(templates.get(invocation.getArgument(0))));

        new DevDataInitializer().seedSeatMapDevData(roomLayoutRepository, layoutTemplateRepository).run();

        ArgumentCaptor<RoomLayout> layouts = ArgumentCaptor.forClass(RoomLayout.class);
        verify(roomLayoutRepository, times(15)).save(layouts.capture());

        Map<UUID, UUID> roomTemplates = layouts.getAllValues().stream()
                .collect(Collectors.toMap(RoomLayout::getRoomId, layout -> layout.getLayoutTemplate().getId()));

        assertThat(roomTemplates)
                .containsExactlyInAnyOrderEntriesOf(expectedRoomTemplates());
    }

    @Test
    void preservesExistingRoomLayouts() throws Exception {
        when(roomLayoutRepository.findByRoomId(any(UUID.class))).thenReturn(Optional.of(new RoomLayout()));

        new DevDataInitializer().seedSeatMapDevData(roomLayoutRepository, layoutTemplateRepository).run();

        verify(roomLayoutRepository, times(15)).findByRoomId(any(UUID.class));
        verifyNoInteractions(layoutTemplateRepository);
        verify(roomLayoutRepository, org.mockito.Mockito.never()).save(any(RoomLayout.class));
    }

    private Map<UUID, LayoutTemplate> templates() {
        Map<UUID, LayoutTemplate> templates = new LinkedHashMap<>();
        expectedRoomTemplates().values().stream().distinct().forEach(templateId -> {
            LayoutTemplate template = new LayoutTemplate();
            template.setId(templateId);
            template.setName("Template " + templateId);
            templates.put(templateId, template);
        });
        return templates;
    }

    private Map<UUID, UUID> expectedRoomTemplates() {
        UUID templateA = uuid("00000000-0000-0000-0001-000000000001");
        UUID templateB = uuid("00000000-0000-0000-0002-000000000001");
        UUID templateC = uuid("00000000-0000-0000-0003-000000000001");
        UUID templateD = uuid("00000000-0000-0000-0004-000000000001");
        UUID templateE = uuid("00000000-0000-0000-0005-000000000001");
        UUID templateF = uuid("00000000-0000-0000-0006-000000000001");

        return Map.ofEntries(
                Map.entry(room(101), templateE),
                Map.entry(room(102), templateF), Map.entry(room(103), templateF),
                Map.entry(room(109), templateF), Map.entry(room(110), templateF),
                Map.entry(room(201), templateA), Map.entry(room(202), templateB),
                Map.entry(room(203), templateC), Map.entry(room(204), templateA),
                Map.entry(room(205), templateB), Map.entry(room(206), templateB),
                Map.entry(room(207), templateD), Map.entry(room(212), templateA),
                Map.entry(room(213), templateA), Map.entry(room(214), templateB)
        );
    }

    private UUID room(int number) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(number));
    }

    private UUID uuid(String value) {
        return UUID.fromString(value);
    }
}
