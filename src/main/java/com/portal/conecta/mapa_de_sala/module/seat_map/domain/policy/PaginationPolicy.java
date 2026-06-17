package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.InvalidPaginationException;

public class PaginationPolicy {

    private static final int MAX_SIZE = 100;
    private PaginationPolicy(){

    }

    public static void validate(int page, int size) {
        if (page < 0){
            throw new InvalidPaginationException("O número da página não pode ser negativo.");
        }
        if (size <= 0){
            throw new InvalidPaginationException("O tamanho da página deve ser maior que zero.");
        }
        if (size > MAX_SIZE){
            throw new InvalidPaginationException("O tamanho máximo da página é "+MAX_SIZE+".");
        }
    }
}
