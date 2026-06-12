package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Visualização de mapa por sala e turma. Contém um mapa salvo ou uma sugestão alfabética.")
public record RoomMapViewResponse (
        boolean suggested,
        RoomMapResponse map,
        RoomMapGridResponse grid,
        List<RoomMapAllocationResponse> allocations,
        List<UnassignedStudentResponse> unassignedStudent
){
    public RoomMapViewResponse{
        Objects.requireNonNull(grid, "grid can't be null");
        allocations = List.copyOf(
                Objects.requireNonNull(allocations, "unassignedStudent can't be null"));
        unassignedStudent = List.copyOf(
                Objects.requireNonNull(unassignedStudent, "unassignedStudent can't be null"));


        if (suggested && map != null){
            throw new IllegalArgumentException("Suggestion (suggested=true) can't contain a saved map.");
        }

        if (!suggested && map == null){
            throw new IllegalArgumentException("Saved map (suggested=true) require the map of the completed field");
        }
    }
}
