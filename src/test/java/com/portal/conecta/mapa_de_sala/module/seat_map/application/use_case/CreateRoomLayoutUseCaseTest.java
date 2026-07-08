package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomLayoutCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutTemplateRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRoomLayoutUseCaseTest {

    @Mock private HubRoomPort hubRoomPort;
    @Mock private LayoutTemplateRepository layoutTemplateRepository;
    @Mock private RoomLayoutRepository roomLayoutRepository;

    @InjectMocks
    private CreateRoomLayoutUseCase useCase;

    private UUID roomId;
    private UUID templateId;
    private LayoutTemplate template;
    private CreateRoomLayoutCommand command;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        template = mock(LayoutTemplate.class);
        command = new CreateRoomLayoutCommand(roomId, templateId);
    }

    @Test
    @DisplayName("deve vincular sala ao template quando não houver vínculo prévio")
    void shouldLinkRoomToTemplateWhenNoExistingLink() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
        when(roomLayoutRepository.save(any(RoomLayout.class))).thenAnswer(i -> i.getArgument(0));

        RoomLayout result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getLayoutTemplate()).isEqualTo(template);
        verify(roomLayoutRepository).save(any(RoomLayout.class));
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException quando sala não existe no Hub")
    void shouldThrowWhenRoomDoesNotExistInHub() {
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(layoutTemplateRepository, roomLayoutRepository);
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException quando template não existe ou está inativo")
    void shouldThrowWhenTemplateDoesNotExistOrIsInactive() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(roomLayoutRepository);
    }

    @Test
    @DisplayName("deve lançar ConflictException quando sala já possui layout vinculado")
    void shouldThrowConflictWhenRoomAlreadyHasLayoutLinked() {
        RoomLayout existingLayout = mock(RoomLayout.class);

        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.of(existingLayout));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class);

        verify(roomLayoutRepository, never()).save(any());
    }

    @Test
    @DisplayName("não deve salvar quando qualquer validação falhar")
    void shouldNotSaveWhenAnyValidationFails() {
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roomLayoutRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ConflictException quando a constraint única do banco for violada em corrida de concorrência")
    void shouldThrowConflictWhenUniqueConstraintViolatedDueToRaceCondition() {
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(layoutTemplateRepository.findByIdAndActiveTrue(templateId)).thenReturn(Optional.of(template));
        when(roomLayoutRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
        when(roomLayoutRepository.save(any(RoomLayout.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("uq_room_layout_room_id"));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ConflictException.class);
    }
}