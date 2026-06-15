package com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomMapViewAssemblerTest {

    @Mock
    private RoomMapMapper roomMapMapper;

    private RoomMapViewAssembler assembler;
    private final SeatNumberCalculator calculator = new SeatNumberCalculator();

    private UUID seat1;
    private UUID seat2;
    private UUID teacherPos;
    private List<LayoutPosition> positions;
    private SeatNumbering numbering;

    @BeforeEach
    void setUp() {
        assembler = new RoomMapViewAssembler(roomMapMapper);

        seat1 = UUID.randomUUID();
        seat2 = UUID.randomUUID();
        teacherPos = UUID.randomUUID();

        positions = List.of(
                pos(seat1, 0, 0, LayoutPositionType.STUDENT),
                pos(seat2, 1, 0, LayoutPositionType.STUDENT),
                pos(teacherPos, 2, 0, LayoutPositionType.TEACHER)
        );

        numbering = calculator.calculate(positions);
    }

    // -----------------------------------------------------------------------
    // Grid
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("grid traz dimensões corretas")
    void gridHasCorrectDimensions() {
        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                3, 5, positions, numbering, List.of());

        assertThat(response.grid().rows()).isEqualTo(3);
        assertThat(response.grid().columns()).isEqualTo(5);
    }

    @Test
    @DisplayName("grid totalSeats conta apenas posições STUDENT")
    void gridTotalSeatsCountsOnlyStudentPositions() {
        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of());

        assertThat(response.grid().totalSeats()).isEqualTo(2);
    }

    @Test
    @DisplayName("grid traz seatNumber nas posições STUDENT e null nas demais")
    void gridCarriesSeatNumbersOnlyOnStudentPositions() {
        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of());

        assertThat(response.grid().positions())
                .extracting("seatNumber")
                .containsExactly(1, 2, null); // ordem de leitura Y,X; teacher no fim
    }

    @Test
    @DisplayName("grid ordena posições por Y depois X independente da ordem de entrada")
    void gridSortsPositionsByReadingOrder() {
        UUID topLeft = UUID.randomUUID();
        UUID topRight = UUID.randomUUID();
        UUID bottomLeft = UUID.randomUUID();

        // fornecidas fora de ordem
        List<LayoutPosition> unordered = List.of(
                pos(bottomLeft, 0, 1, LayoutPositionType.STUDENT),
                pos(topRight, 1, 0, LayoutPositionType.STUDENT),
                pos(topLeft, 0, 0, LayoutPositionType.STUDENT)
        );
        SeatNumbering unorderedNumbering = calculator.calculate(unordered);

        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                2, 2, unordered, unorderedNumbering, List.of());

        assertThat(response.grid().positions())
                .extracting("layoutPositionId")
                .containsExactly(topLeft, topRight, bottomLeft);
    }

    @Test
    @DisplayName("grid traz layoutPositionId, x, y e type corretos em cada posição")
    void gridPositionCarriesAllFields() {
        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of());

        RoomMapGridPositionResponse first = response.grid().positions().getFirst();
        assertThat(first.layoutPositionId()).isEqualTo(seat1);
        assertThat(first.positionX()).isEqualTo(0);
        assertThat(first.positionY()).isEqualTo(0);
        assertThat(first.type()).isEqualTo(LayoutPositionType.STUDENT);
        assertThat(first.seatNumber()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // assembleFromSuggestion
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sugestão monta alocações em ordem alfabética cruzando com os assentos")
    void suggestionBuildsAllocationsInAlphabeticalOrder() {
        HubStudent ana = new HubStudent(UUID.randomUUID(), "Ana");
        HubStudent bruno = new HubStudent(UUID.randomUUID(), "Bruno");

        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of(ana, bruno));

        assertThat(response.allocations())
                .extracting("studentName", "seatNumber", "layoutPositionId")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(ana.name(), 1, seat1),
                        org.assertj.core.groups.Tuple.tuple(bruno.name(), 2, seat2));

        assertThat(response.unassignedStudent()).isEmpty();
    }

    @Test
    @DisplayName("sugestão deixa alunos excedentes em unassignedStudents quando não há assentos suficientes")
    void suggestionLeavesExceedingStudentsUnassigned() {
        HubStudent ana = new HubStudent(UUID.randomUUID(), "Ana");
        HubStudent bruno = new HubStudent(UUID.randomUUID(), "Bruno");
        HubStudent carlos = new HubStudent(UUID.randomUUID(), "Carlos");

        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of(ana, bruno, carlos));

        assertThat(response.allocations()).hasSize(2);
        assertThat(response.unassignedStudent())
                .extracting("studentName")
                .containsExactly("Carlos");
    }

    @Test
    @DisplayName("sugestão retorna suggested=true e map=null")
    void suggestionHasSuggestedTrueAndNullMap() {
        RoomMapViewResponse response = assembler.assembleFromSuggestion(
                1, 3, positions, numbering, List.of());

        assertThat(response.suggested()).isTrue();
        assertThat(response.map()).isNull();
    }

    // -----------------------------------------------------------------------
    // assembleFromSavedMap
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("mapa salvo retorna suggested=false e map a partir do mapper")
    void savedMapHasSuggestedFalseAndMapFromMapper() {
        RoomMap roomMap = mock(RoomMap.class);
        RoomMapResponse mapResponse = mock(RoomMapResponse.class);
        when(roomMapMapper.toResponse(roomMap)).thenReturn(mapResponse);

        RoomMapViewResponse response = assembler.assembleFromSavedMap(
                roomMap, 1, 3, positions, numbering, List.of(), List.of());

        assertThat(response.suggested()).isFalse();
        assertThat(response.map()).isEqualTo(mapResponse);
    }

    @Test
    @DisplayName("mapa salvo monta alocações a partir das locations, ordenadas por seatNumber")
    void savedMapBuildsAllocationsFromLocationsSortedBySeatNumber() {
        RoomMap roomMap = mock(RoomMap.class);
        when(roomMapMapper.toResponse(roomMap)).thenReturn(mock(RoomMapResponse.class));

        HubStudent ana = new HubStudent(UUID.randomUUID(), "Ana");
        HubStudent bruno = new HubStudent(UUID.randomUUID(), "Bruno");

        RoomMapLocation locationSeat2 = location(bruno.id(), seat2);
        RoomMapLocation locationSeat1 = location(ana.id(), seat1);

        RoomMapViewResponse response = assembler.assembleFromSavedMap(
                roomMap, 1, 3, positions, numbering,
                List.of(locationSeat2, locationSeat1),
                List.of(ana, bruno));

        assertThat(response.allocations())
                .extracting("studentName", "seatNumber", "layoutPositionId")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(ana.name(), 1, seat1),
                        org.assertj.core.groups.Tuple.tuple(bruno.name(), 2, seat2));
    }

    @Test
    @DisplayName("mapa salvo ignora locations cuja posição não é assento de aluno")
    void savedMapIgnoresLocationsOnNonStudentPositions() {
        RoomMap roomMap = mock(RoomMap.class);
        when(roomMapMapper.toResponse(roomMap)).thenReturn(mock(RoomMapResponse.class));

        HubStudent ana = new HubStudent(UUID.randomUUID(), "Ana");
        RoomMapLocation locationOnTeacherSeat = location(ana.id(), teacherPos);

        RoomMapViewResponse response = assembler.assembleFromSavedMap(
                roomMap, 1, 3, positions, numbering,
                List.of(locationOnTeacherSeat),
                List.of(ana));

        assertThat(response.allocations()).isEmpty();
    }

    @Test
    @DisplayName("mapa salvo usa nome padrão quando aluno alocado não está na turma do Hub")
    void savedMapUsesFallbackNameForUnknownStudent() {
        RoomMap roomMap = mock(RoomMap.class);
        when(roomMapMapper.toResponse(roomMap)).thenReturn(mock(RoomMapResponse.class));

        UUID unknownStudentId = UUID.randomUUID();
        RoomMapLocation locationUnknown = location(unknownStudentId, seat1);

        RoomMapViewResponse response = assembler.assembleFromSavedMap(
                roomMap, 1, 3, positions, numbering,
                List.of(locationUnknown),
                List.of());

        assertThat(response.allocations())
                .extracting("studentName")
                .containsExactly("Aluno não encontrado no Hub");
    }

    @Test
    @DisplayName("mapa salvo retorna alunos da turma sem alocação em unassignedStudents")
    void savedMapReturnsClassStudentsWithoutAllocationAsUnassigned() {
        RoomMap roomMap = mock(RoomMap.class);
        when(roomMapMapper.toResponse(roomMap)).thenReturn(mock(RoomMapResponse.class));

        HubStudent ana = new HubStudent(UUID.randomUUID(), "Ana");
        HubStudent bruno = new HubStudent(UUID.randomUUID(), "Bruno");

        RoomMapLocation locationAna = location(ana.id(), seat1);

        RoomMapViewResponse response = assembler.assembleFromSavedMap(
                roomMap, 1, 3, positions, numbering,
                List.of(locationAna),
                List.of(ana, bruno));

        assertThat(response.unassignedStudent())
                .extracting("studentName")
                .containsExactly("Bruno");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private LayoutPosition pos(UUID id, int x, int y, LayoutPositionType type) {
        LayoutPosition position = mock(LayoutPosition.class);
        when(position.getId()).thenReturn(id);
        when(position.getPositionX()).thenReturn(x);
        when(position.getPositionY()).thenReturn(y);
        when(position.getType()).thenReturn(type);
        return position;
    }

    private RoomMapLocation location(UUID studentId, UUID layoutPositionId) {
        RoomMapLocation location = mock(RoomMapLocation.class);
        when(location.getStudentId()).thenReturn(studentId);
        when(location.getLayoutPositionId()).thenReturn(layoutPositionId);
        return location;
    }
}