package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.InvalidPaginationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationPolicyTest {

    @Test
    @DisplayName("aceita página e tamanho válidos sem lançar exceção")
    void shouldAcceptValidPageAndSize() {
        assertThatCode(() -> PaginationPolicy.validate(0, 20))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("aceita tamanho exatamente no limite máximo")
    void shouldAcceptSizeAtMaxLimit() {
        assertThatCode(() -> PaginationPolicy.validate(0, 100))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("lança exceção quando página é negativa")
    void shouldThrowWhenPageIsNegative() {
        assertThatThrownBy(() -> PaginationPolicy.validate(-1, 20))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessageContaining("não pode ser negativo");
    }

    @Test
    @DisplayName("lança exceção quando tamanho é zero")
    void shouldThrowWhenSizeIsZero() {
        assertThatThrownBy(() -> PaginationPolicy.validate(0, 0))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("lança exceção quando tamanho é negativo")
    void shouldThrowWhenSizeIsNegative() {
        assertThatThrownBy(() -> PaginationPolicy.validate(0, -5))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("lança exceção quando tamanho excede o máximo permitido")
    void shouldThrowWhenSizeExceedsMaxLimit() {
        assertThatThrownBy(() -> PaginationPolicy.validate(0, 101))
                .isInstanceOf(InvalidPaginationException.class)
                .hasMessageContaining("tamanho máximo da página é 100");
    }
}