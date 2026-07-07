package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // helper estuba campos nem sempre lidos emtodo teste
class SeatNumberCalculatorTest {

    private final SeatNumberCalculator calculator = new SeatNumberCalculator();

    @Test
    @DisplayName("numera posições STUDENT em sequência na ordem Y, depois X")
    void shouldNumberStudentPositionsInReadingOrder() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        // (y=0,x=0), (y=0,x=1), (y=1,x=0)  ->  1, 2, 3
        List<LayoutPosition> positions = List.of(
                pos(a, 0, 0, LayoutPositionType.STUDENT),
                pos(b, 1, 0, LayoutPositionType.STUDENT),
                pos(c, 0, 1, LayoutPositionType.STUDENT)
        );

        SeatNumbering numbering = calculator.calculate(positions);

        assertThat(numbering.seatNumberOf(a)).isEqualTo(1);
        assertThat(numbering.seatNumberOf(b)).isEqualTo(2);
        assertThat(numbering.seatNumberOf(c)).isEqualTo(3);
        assertThat(numbering.totalSeats()).isEqualTo(3);
    }

    @Test
    @DisplayName("ordena entradas fora de ordem antes de numerar")
    void shouldSortBeforeNumbering() {
        UUID topLeft = UUID.randomUUID();
        UUID topRight = UUID.randomUUID();
        UUID bottomLeft = UUID.randomUUID();

        // fornecidas embaralhadas
        List<LayoutPosition> positions = List.of(
                pos(bottomLeft, 0, 1, LayoutPositionType.STUDENT),
                pos(topRight, 1, 0, LayoutPositionType.STUDENT),
                pos(topLeft, 0, 0, LayoutPositionType.STUDENT)
        );

        SeatNumbering numbering = calculator.calculate(positions);

        assertThat(numbering.seatNumberOf(topLeft)).isEqualTo(1);
        assertThat(numbering.seatNumberOf(topRight)).isEqualTo(2);
        assertThat(numbering.seatNumberOf(bottomLeft)).isEqualTo(3);
    }

    @Test
    @DisplayName("posições não-STUDENT não recebem número e não consomem a sequência")
    void shouldSkipNonStudentPositionsWithoutConsumingNumbers() {
        UUID student1 = UUID.randomUUID();
        UUID teacher = UUID.randomUUID();
        UUID student2 = UUID.randomUUID();
        UUID obstacle = UUID.randomUUID();
        UUID equipment = UUID.randomUUID();

        // teacher fica no meio (y=0,x=1), entre os dois alunos
        List<LayoutPosition> positions = List.of(
                pos(student1, 0, 0, LayoutPositionType.STUDENT),
                pos(teacher, 1, 0, LayoutPositionType.TEACHER),
                pos(student2, 2, 0, LayoutPositionType.STUDENT),
                pos(obstacle, 0, 1, LayoutPositionType.OBSTACLE),
                pos(equipment, 1, 1, LayoutPositionType.EQUIPMENT)
        );

        SeatNumbering numbering = calculator.calculate(positions);

        assertThat(numbering.seatNumberOf(student1)).isEqualTo(1);
        assertThat(numbering.seatNumberOf(student2)).isEqualTo(2); // teacher não virou "2"
        assertThat(numbering.seatNumberOf(teacher)).isNull();
        assertThat(numbering.seatNumberOf(obstacle)).isNull();
        assertThat(numbering.seatNumberOf(equipment)).isNull();
        assertThat(numbering.totalSeats()).isEqualTo(2);
    }

    @Test
    @DisplayName("retorna null para posição desconhecida")
    void shouldReturnNullForUnknownPosition() {
        SeatNumbering numbering = calculator.calculate(
                List.of(pos(UUID.randomUUID(), 0, 0, LayoutPositionType.STUDENT)));

        assertThat(numbering.seatNumberOf(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("lista vazia gera numeração vazia")
    void shouldHandleEmptyList() {
        SeatNumbering numbering = calculator.calculate(List.of());

        assertThat(numbering.totalSeats()).isZero();
    }

    @Test
    @DisplayName("lista nula lança NullPointerException")
    void shouldThrowWhenPositionsIsNull() {
        assertThatThrownBy(() -> calculator.calculate(null))
                .isInstanceOf(NullPointerException.class);
    }

    private LayoutPosition pos(UUID id, int x, int y, LayoutPositionType type) {
        LayoutPosition position = mock(LayoutPosition.class);
        when(position.getId()).thenReturn(id);
        when(position.getPositionX()).thenReturn(x);
        when(position.getPositionY()).thenReturn(y);
        when(position.getType()).thenReturn(type);
        return position;
    }
}
