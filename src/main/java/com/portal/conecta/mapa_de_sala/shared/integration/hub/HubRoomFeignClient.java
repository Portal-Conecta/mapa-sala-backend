package com.portal.conecta.mapa_de_sala.shared.integration.hub;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-room", url = "${hub.base-url}")
public interface HubRoomFeignClient {

    @GetMapping("/api/salas/{roomId}")
    ResponseEntity<HubRoomResponse> getRoom(@PathVariable("roomId") UUID roomId);

    @GetMapping("/api/salas/{roomId}/usuarios/{userId}/vinculo")
    ResponseEntity<HubUserRoomLinkResponse> checkUserLink(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("userId") UUID userId
    );
}
