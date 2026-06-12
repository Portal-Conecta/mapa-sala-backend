package com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.result.AllocationInput;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private final RoomMapViewAssembler assembler = new RoomMapViewAssembler();
    private final SeatNumberCalculator calculator = new SeatNumberCalculator();

    private UUID seat1;
    private UUID seat2;
    private UUID teacherPos;
    private List<LayoutPosition> positions;
    private SeatNumbering numbering;

    @BeforeEach
    void setUp() {
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
        RoomMapViewResponse response = assembler.assemble(
                true, null, 3, 5, positions, numbering,
                List.of(), List.of());

        assertThat(response.grid().rows()).isEqualTo(3);
        assertThat(response.grid().columns()).isEqualTo(5);
    }

    @Test
    @DisplayName("grid totalSeats conta apenas posições STUDENT")
    void gridTotalSeatsCountsOnlyStudentPositions() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

        assertThat(response.grid().totalSeats()).isEqualTo(2);
    }

    @Test
    @DisplayName("grid traz seatNumber nas posições STUDENT e null nas demais")
    void gridCarriesSeatNumbersOnlyOnStudentPositions() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

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

        RoomMapViewResponse response = assembler.assemble(
                true, null, 2, 2, unordered, unorderedNumbering,
                List.of(), List.of());

        assertThat(response.grid().positions())
                .extracting("layoutPositionId")
                .containsExactly(topLeft, topRight, bottomLeft);
    }

    @Test
    @DisplayName("grid traz layoutPositionId, x, y e type corretos em cada posição")
    void gridPositionCarriesAllFields() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

        RoomMapGridPositionResponse first = response.grid().positions().getFirst();
        assertThat(first.layoutPositionId()).isEqualTo(seat1);
        assertThat(first.positionX()).isEqualTo(0);
        assertThat(first.positionY()).isEqualTo(0);
        assertThat(first.type()).isEqualTo(LayoutPositionType.STUDENT);
        assertThat(first.seatNumber()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Allocations
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("alocações chegam ordenadas por seatNumber")
    void allocationsAreSortedBySeatNumber() {
        UUID anaId = UUID.randomUUID();
        UUID brunoId = UUID.randomUUID();

        // fornecidas fora de ordem: Bruno no seat2, Ana no seat1
        List<AllocationInput> allocations = List.of(
                new AllocationInput(brunoId, "Bruno", seat2),
                new AllocationInput(anaId, "Ana", seat1));

        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                allocations, List.of());

        assertThat(response.allocations())
                .extracting("seatNumber")
                .containsExactly(1, 2);

        assertThat(response.allocations())
                .extracting("studentName")
                .containsExactly("Ana", "Bruno");
    }

    @Test
    @DisplayName("alocação carrega studentId, studentName, seatNumber e layoutPositionId")
    void allocationCarriesAllFields() {
        UUID studentId = UUID.randomUUID();
        List<AllocationInput> allocations =
                List.of(new AllocationInput(studentId, "Maria", seat1));

        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                allocations, List.of());

        RoomMapAllocationResponse allocation = response.allocations().getFirst();
        assertThat(allocation.studentId()).isEqualTo(studentId);
        assertThat(allocation.studentName()).isEqualTo("Maria");
        assertThat(allocation.seatNumber()).isEqualTo(1);
        assertThat(allocation.layoutPositionId()).isEqualTo(seat1);
    }

    @Test
    @DisplayName("sem alocações retorna lista vazia")
    void noAllocationsReturnsEmptyList() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

        assertThat(response.allocations()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Unassigned
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("unassigned é passado diretamente para o response sem transformação")
    void unassignedIsPassedThrough() {
        UUID anaId = UUID.randomUUID();
        List<UnassignedStudentResponse> unassigned =
                List.of(new UnassignedStudentResponse(anaId, "Ana"));

        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), unassigned);

        assertThat(response.unassignedStudent())
                .extracting("studentName")
                .containsExactly("Ana");
    }

    @Test
    @DisplayName("unassigned vazio retorna lista vazia")
    void emptyUnassignedReturnsEmptyList() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

        assertThat(response.unassignedStudent()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Flags suggested / map
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("suggested=true e map=null monta corretamente")
    void suggestedTrueWithNullMap() {
        RoomMapViewResponse response = assembler.assemble(
                true, null, 1, 3, positions, numbering,
                List.of(), List.of());

        assertThat(response.suggested()).isTrue();
        assertThat(response.map()).isNull();
    }

    @Test
    @DisplayName("suggested=false e map preenchido monta corretamente")
    void suggestedFalseWithMap() {
        RoomMapResponse map = mock(RoomMapResponse.class);

        RoomMapViewResponse response = assembler.assemble(
                false, map, 1, 3, positions, numbering,
                List.of(), List.of());

        assertThat(response.suggested()).isFalse();
        assertThat(response.map()).isEqualTo(map);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private LayoutPosition pos(UUID id, int x, int y, LayoutPositionType type) {
        LayoutPosition position = mock(LayoutPosition.class);
        when(position.getId()).thenReturn(id);
        when(position.getPositionX()).thenReturn(x);
        when(position.getPositionY()).thenReturn(y);
        when(position.getType()).thenReturn(type);
        return position;
    }
}