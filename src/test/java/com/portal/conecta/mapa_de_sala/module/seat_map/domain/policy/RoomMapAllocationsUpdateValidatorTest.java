package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.AllocationEntryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomMapAllocationsUpdateValidatorTest {

    private RoomMapAllocationsUpdateValidator validator;

    private final UUID seat1      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID seat2      = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID teacherPos = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private final UUID student1      = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private final UUID student2      = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private final UUID outsideStudent = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    @BeforeEach
    void setUp() {
        validator = new RoomMapAllocationsUpdateValidator();
    }

    @Test
    void validate_shouldPassWhenAllRulesAreSatisfied() {
        assertThatCode(() ->
                validator.validate(
                        List.of(entry(student1, seat1), entry(student2, seat2)),
                        positionById(),
                        classStudentIds()
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void validate_shouldThrowWhenStudentIdIsDuplicated() {
        // RN-MA03
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(student1, seat1), entry(student1, seat2)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void validate_shouldThrowWhenLayoutPositionIdIsDuplicated() {
        // RN-MA04
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(student1, seat1), entry(student2, seat1)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void validate_shouldThrowWhenPositionDoesNotBelongToTemplate() {
        // RN-MA05
        var unknownPosition = UUID.randomUUID();
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(student1, unknownPosition), entry(student2, seat2)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence ao template");
    }

    @Test
    void validate_shouldThrowWhenPositionIsNotStudentType() {
        // RN-MA06
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(student1, teacherPos), entry(student2, seat2)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não é do tipo STUDENT");
    }

    @Test
    void validate_shouldThrowWhenStudentDoesNotBelongToClass() {
        // RN-MA02
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(outsideStudent, seat1), entry(student2, seat2)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence à turma");
    }

    @Test
    void validate_shouldThrowWhenClassStudentIsMissingFromList() {
        // RN-MA01
        assertThatThrownBy(() ->
                validator.validate(
                        List.of(entry(student1, seat1)),
                        positionById(),
                        classStudentIds()
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ausentes");
    }

    // --- helpers ---

    private AllocationEntryRequest entry(UUID studentId, UUID positionId) {
        return new AllocationEntryRequest(studentId, positionId);
    }

    private Map<UUID, LayoutPosition> positionById() {
        return Map.of(
                seat1, position(seat1, LayoutPositionType.STUDENT),
                seat2, position(seat2, LayoutPositionType.STUDENT),
                teacherPos, position(teacherPos, LayoutPositionType.TEACHER)
        );
    }

    private Set<UUID> classStudentIds() {
        return Set.of(student1, student2);
    }

    private LayoutPosition position(UUID id, LayoutPositionType type) {
        var pos = new LayoutPosition();
        pos.setId(id);
        pos.setPositionX(0);
        pos.setPositionY(0);
        pos.setType(type);
        return pos;
    }
}