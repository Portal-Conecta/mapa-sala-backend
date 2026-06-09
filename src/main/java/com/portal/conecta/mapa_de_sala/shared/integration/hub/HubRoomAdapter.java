package com.portal.conecta.mapa_de_sala.shared.integration.hub;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HubRoomAdapter implements HubRoomPort {

    private final HubRoomFeignClient hubRoomFeignClient;

    public HubRoomAdapter(HubRoomFeignClient hubRoomFeignClient) {
        this.hubRoomFeignClient = hubRoomFeignClient;
    }

    @Override
    public boolean existsById(UUID roomId) {
        try {
            return hubRoomFeignClient.getRoom(roomId).getStatusCode().is2xxSuccessful();
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    @Override
    public boolean isUserLinkedToRoom(UUID userId, UUID roomId) {
        try {
            var response = hubRoomFeignClient.checkUserLink(roomId, userId);
            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().linked();
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
}
