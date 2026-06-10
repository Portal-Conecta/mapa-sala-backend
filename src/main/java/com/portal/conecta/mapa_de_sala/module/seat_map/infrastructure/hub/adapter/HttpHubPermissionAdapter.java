package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.exception.HubIntegrationException;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubApiProperties;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "false")
public class HttpHubPermissionAdapter implements HubPermissionPort {

    private final RestClient restClient;

    public HttpHubPermissionAdapter(HubApiProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.url()).build();
    }

    @Override
    public List<UUID> getAccessibleClassIds(UUID userId, TypeUser userType) {
        try {
            UUID[] classIds = restClient.get()
                    .uri("/users/{userId}/accessible-classes?userType={userType}", userId, userType)
                    .retrieve()
                    .body(new ParameterizedTypeReference<UUID[]>() {});

            if (classIds == null) {
                return List.of();
            }

            return Arrays.asList(classIds);
        } catch (HttpClientErrorException.NotFound exception) {
            return List.of();
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Serviço de permissões do Hub indisponível.", exception);
        }
    }
}
